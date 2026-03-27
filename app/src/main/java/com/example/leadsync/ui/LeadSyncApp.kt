package com.example.leadsync.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.leadsync.data.LeadSyncRepository
import com.example.leadsync.ui.screens.DashboardScreen
import com.example.leadsync.ui.screens.MeetingEditorScreen
import com.example.leadsync.ui.screens.PeopleScreen
import com.example.leadsync.ui.screens.PersonDetailScreen

private enum class TopLevelDestination(
    val baseRoute: String,
    val label: String,
    val icon: @Composable () -> Unit,
) {
    DASHBOARD(
        baseRoute = "dashboard",
        label = "Dashboard",
        icon = { Icon(Icons.Outlined.SpaceDashboard, contentDescription = "Dashboard") },
    ),
    PEOPLE(
        baseRoute = "people",
        label = "People",
        icon = { Icon(Icons.Outlined.Groups, contentDescription = "People") },
    ),
    CAPTURE(
        baseRoute = "capture",
        label = "Log 1:1",
        icon = { Icon(Icons.Outlined.AddCircle, contentDescription = "Log 1:1") },
    ),
}

@Composable
fun LeadSyncApp(
    repository: LeadSyncRepository,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val topLevelDestinations = TopLevelDestination.entries

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route?.startsWith(destination.baseRoute) == true
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.baseRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = destination.icon,
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.DASHBOARD.baseRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.DASHBOARD.baseRoute) {
                val viewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.factory(repository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                DashboardScreen(
                    uiState = uiState,
                    onOpenPeople = { navController.navigate(TopLevelDestination.PEOPLE.baseRoute) },
                    onLogMeeting = { personId ->
                        navController.navigate(captureRoute(personId = personId))
                    },
                    onOpenPerson = { personId ->
                        navController.navigate("person/$personId")
                    },
                )
            }

            composable(TopLevelDestination.PEOPLE.baseRoute) {
                val viewModel: PeopleViewModel = viewModel(
                    factory = PeopleViewModel.factory(repository),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                PeopleScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onOpenPerson = { personId -> navController.navigate("person/$personId") },
                )
            }

            composable("${TopLevelDestination.CAPTURE.baseRoute}?personId={personId}&meetingId={meetingId}") { backStack ->
                val preselectedPersonId = backStack.arguments?.getString("personId")?.toLongOrNull()
                val editingMeetingId = backStack.arguments?.getString("meetingId")?.toLongOrNull()
                val viewModel: MeetingEditorViewModel = viewModel(
                    key = "meeting-editor-${editingMeetingId ?: "new"}-${preselectedPersonId ?: "none"}",
                    factory = MeetingEditorViewModel.factory(repository, preselectedPersonId, editingMeetingId),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                MeetingEditorScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                )
            }

            composable("person/{personId}") { backStack ->
                val personId = backStack.arguments?.getString("personId")?.toLongOrNull() ?: return@composable
                val viewModel: PersonDetailViewModel = viewModel(
                    key = "person-$personId",
                    factory = PersonDetailViewModel.factory(repository, personId),
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                PersonDetailScreen(
                    uiState = uiState,
                    onNavigateUp = { navController.popBackStack() },
                    onLogMeeting = {
                        navController.navigate(captureRoute(personId = personId))
                    },
                    onEditMeeting = { meetingId ->
                        navController.navigate(captureRoute(meetingId = meetingId))
                    },
                )
            }
        }
    }
}

private fun captureRoute(
    personId: Long? = null,
    meetingId: Long? = null,
): String {
    val params = buildList {
        personId?.let { add("personId=$it") }
        meetingId?.let { add("meetingId=$it") }
    }
    return if (params.isEmpty()) {
        TopLevelDestination.CAPTURE.baseRoute
    } else {
        "${TopLevelDestination.CAPTURE.baseRoute}?${params.joinToString("&")}"
    }
}

package com.example.leadsync.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.leadsync.R
import com.example.leadsync.data.ActionItemSummaryRow
import com.example.leadsync.data.ActionStatus
import com.example.leadsync.data.MeetingRecord
import com.example.leadsync.data.PersonEntity
import com.example.leadsync.data.PersonOverview
import com.example.leadsync.data.PersonType
import com.example.leadsync.export.InteractionWorkbookExporter
import com.example.leadsync.ui.ActionItemDraftUiState
import com.example.leadsync.ui.DashboardUiState
import com.example.leadsync.ui.MeetingEditorEvent
import com.example.leadsync.ui.MeetingEditorUiState
import com.example.leadsync.ui.PeopleEvent
import com.example.leadsync.ui.PeopleUiState
import com.example.leadsync.ui.PersonDetailUiState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class PeopleTab {
    REPORTEES,
    STAKEHOLDERS,
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    cloudEmail: String,
    cloudStatus: String?,
    isSyncing: Boolean,
    onPushSync: () -> Unit,
    onPullSync: () -> Unit,
    onLogout: () -> Unit,
    onOpenPeople: () -> Unit,
    onLogMeeting: (Long?) -> Unit,
    onOpenPerson: (Long) -> Unit,
) {
    var isProfilePanelOpen by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(end = 52.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.leadsync_logo),
                            contentDescription = "LeadSync logo",
                            modifier = Modifier.size(68.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "LeadSync",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Track recurring 1:1s, stakeholder syncs, feedback, and follow-through in one place.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    IconButton(
                        onClick = { isProfilePanelOpen = true },
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Open account panel",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }

            item {
                SummaryGrid(
                    cards = listOf(
                        SummaryCardData("People", uiState.totalPeople.toString(), "Across reportees and stakeholders"),
                        SummaryCardData("Reportees", uiState.reporteeCount.toString(), "Direct reports with 1:1 history"),
                        SummaryCardData("Stakeholders", uiState.stakeholderCount.toString(), "Cross-functional collaborators"),
                        SummaryCardData("Open actions", uiState.openActionItems.toString(), "Items still in progress or blocked"),
                    ),
                )
            }

            item {
                SectionTitle("Due this week")
            }

            if (uiState.dueThisWeek.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No action items due this week",
                        body = "Use the meeting form to capture owners, deadlines, and progress after every conversation.",
                    )
                }
            } else {
                items(uiState.dueThisWeek) { item ->
                    ActionItemCard(item = item, onOpenPerson = onOpenPerson)
                }
            }

            item {
                SectionTitle("Recent feedback")
            }

            if (uiState.feedbackHighlights.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No feedback logged yet",
                        body = "Capture feedback in the moment so you can reference it during future check-ins.",
                    )
                }
            } else {
                items(uiState.feedbackHighlights) { meeting ->
                    HighlightCard(
                        title = meeting.personName,
                        subtitle = "${meeting.meeting.interactionType} • ${formatDate(meeting.meeting.scheduledAt)}",
                        body = meeting.meeting.feedback,
                        onClick = { onOpenPerson(meeting.meeting.personId) },
                    )
                }
            }

            item {
                SectionTitle("Recent interactions")
            }

            if (uiState.recentMeetings.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No interactions captured yet",
                        body = "Once you log a 1:1 or stakeholder sync, the latest occurrences will show here.",
                    )
                }
            } else {
                items(uiState.recentMeetings) { meeting ->
                    HighlightCard(
                        title = meeting.personName,
                        subtitle = "${meeting.meeting.interactionType} • ${formatDate(meeting.meeting.scheduledAt)}",
                        body = meeting.meeting.progressSummary.ifBlank { meeting.meeting.agenda.ifBlank { "No notes recorded." } },
                        onClick = { onOpenPerson(meeting.meeting.personId) },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isProfilePanelOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .clickable { isProfilePanelOpen = false },
            )
        }

        AnimatedVisibility(
            visible = isProfilePanelOpen,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            DashboardProfilePanel(
                cloudEmail = cloudEmail,
                cloudStatus = cloudStatus,
                isSyncing = isSyncing,
                onDismiss = { isProfilePanelOpen = false },
                onOpenPeople = {
                    isProfilePanelOpen = false
                    onOpenPeople()
                },
                onLogInteraction = {
                    isProfilePanelOpen = false
                    onLogMeeting(null)
                },
                onPullSync = {
                    isProfilePanelOpen = false
                    onPullSync()
                },
                onPushSync = {
                    isProfilePanelOpen = false
                    onPushSync()
                },
                onLogout = {
                    isProfilePanelOpen = false
                    onLogout()
                },
            )
        }
    }
}

@Composable
private fun DashboardProfilePanel(
    cloudEmail: String,
    cloudStatus: String?,
    isSyncing: Boolean,
    onDismiss: () -> Unit,
    onOpenPeople: () -> Unit,
    onLogInteraction: () -> Unit,
    onPullSync: () -> Unit,
    onPushSync: () -> Unit,
    onLogout: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(312.dp),
        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 12.dp,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Close account panel",
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = cloudEmail,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Cloud sync account",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            cloudStatus?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onOpenPeople,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Groups, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Manage people")
                    }

                    Button(
                        onClick = onLogInteraction,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Log interaction")
                    }

                    OutlinedButton(
                        onClick = onPullSync,
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isSyncing) "Working..." else "Pull cloud")
                    }

                    OutlinedButton(
                        onClick = onPushSync,
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isSyncing) "Working..." else "Push cloud")
                    }
                }

            LinkTextButton(
                text = "Logout",
                onClick = onLogout,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    uiState: PeopleUiState,
    onEvent: (PeopleEvent) -> Unit,
    onOpenPerson: (Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(PeopleTab.REPORTEES) }
    val filteredPeople = uiState.people.filter { person ->
        when (selectedTab) {
            PeopleTab.REPORTEES -> person.type == PersonType.REPORTEE
            PeopleTab.STAKEHOLDERS -> person.type == PersonType.STAKEHOLDER
        }
    }

    LaunchedEffect(uiState.form.message) {
        uiState.form.message?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(PeopleEvent.ConsumeMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(PeopleEvent.OpenDialog) },
                icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
                text = { Text("Add person") },
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("People") },
            )
        },
    ) { innerPadding ->
        if (uiState.form.isDialogOpen) {
            AddPersonDialog(
                uiState = uiState,
                onEvent = onEvent,
            )
        }

        if (uiState.people.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateCard(
                    title = "No people added yet",
                    body = "Start with your reportees and key stakeholders, then log each interaction occurrence against them.",
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    PeopleTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    when (tab) {
                                        PeopleTab.REPORTEES -> "Reportees"
                                        PeopleTab.STAKEHOLDERS -> "Stakeholders"
                                    },
                                )
                            },
                        )
                    }
                }

                if (filteredPeople.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyStateCard(
                            title = when (selectedTab) {
                                PeopleTab.REPORTEES -> "No reportees yet"
                                PeopleTab.STAKEHOLDERS -> "No stakeholders yet"
                            },
                            body = when (selectedTab) {
                                PeopleTab.REPORTEES -> "Add the people you manage here so their interactions stay grouped together."
                                PeopleTab.STAKEHOLDERS -> "Add cross-functional partners and leadership stakeholders here for cleaner tracking."
                            },
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(filteredPeople) { person ->
                            PersonCard(
                                person = person,
                                onClick = { onOpenPerson(person.id) },
                                onEdit = { onEvent(PeopleEvent.EditPerson(person)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    uiState: PersonDetailUiState,
    onNavigateUp: () -> Unit,
    onLogMeeting: () -> Unit,
    onEditMeeting: (Long) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingExport by remember { mutableStateOf<Pair<String, ByteArray>?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(personInteractionWorkbookMimeType),
    ) { uri ->
        val exportPayload = pendingExport
        pendingExport = null
        if (uri == null || exportPayload == null) {
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val resultMessage = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(exportPayload.second)
                    } ?: error("Unable to open the selected location.")
                }
                "Excel export saved."
            }.getOrElse { error ->
                "Excel export failed: ${error.message ?: "Unknown error"}"
            }
            snackbarHostState.showSnackbar(resultMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.detail?.person?.name ?: "Person details")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LinkTextButton(
                            text = "Export",
                            enabled = uiState.detail != null,
                            onClick = {
                                val detail = uiState.detail ?: return@LinkTextButton
                                pendingExport = buildPersonInteractionExport(detail.person.name, detail.meetings)
                                exportLauncher.launch(buildInteractionWorkbookFileName(detail.person.name))
                            },
                        )
                        LinkTextButton(text = "Log 1:1", onClick = onLogMeeting)
                    }
                },
            )
        },
    ) { innerPadding ->
        val detail = uiState.detail
        if (detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (uiState.isLoading) "Loading..." else "No details available.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = detail.person.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = listOf(detail.person.roleTitle, detail.person.team)
                                .filter { it.isNotBlank() }
                                .joinToString(" • ")
                                .ifBlank { detail.person.type.label },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(detail.person.type.label) },
                        )
                        if (detail.person.notes.isNotBlank()) {
                            Text(
                                text = detail.person.notes,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle("Interaction history")
            }

            if (detail.meetings.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No interactions yet",
                        body = "This timeline will show every dated occurrence for this person, including notes, feedback, and action items.",
                    )
                }
            } else {
                items(detail.meetings) { meeting ->
                    MeetingTimelineCard(
                        meeting = meeting,
                        onEdit = { onEditMeeting(meeting.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingEditorScreen(
    uiState: MeetingEditorUiState,
    onEvent: (MeetingEditorEvent) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(MeetingEditorEvent.ConsumeMessage)
        }
    }

    LaunchedEffect(uiState.completedEditVersion) {
        if (uiState.completedEditVersion > 0) {
            onEvent(MeetingEditorEvent.ConsumeCompletedEdit)
            onNavigateUp()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.editingMeetingId == null) "Generic interaction log" else "Edit interaction")
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (uiState.editingMeetingId == null) {
                    "Capture each occurrence with the right person, date, progress notes, timely feedback, and follow-up actions."
                } else {
                    "Update the original interaction, refine notes with formatting, and keep action items in sync."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (uiState.isLoading) {
                EmptyStateCard(
                    title = "Loading interaction",
                    body = "Fetching the previously logged interaction for editing.",
                )
                return@Column
            }

            if (uiState.people.isEmpty()) {
                EmptyStateCard(
                    title = "Add people first",
                    body = "Create your reportees and stakeholders from the People tab, then come back here to log interactions.",
                )
                return@Column
            }

            SelectionCard(title = "Who is this for?") {
                PersonSelector(
                    people = uiState.people,
                    selectedPersonId = uiState.selectedPersonId,
                    onSelected = { onEvent(MeetingEditorEvent.SelectPerson(it)) },
                )
            }

            SelectionCard(title = "Interaction basics") {
                LabeledTextField(
                    label = "Interaction type",
                    value = uiState.interactionType,
                    onValueChange = { onEvent(MeetingEditorEvent.UpdateInteractionType(it)) },
                    placeholder = "Examples: 1:1, Stakeholder sync, Escalation review",
                )
                Spacer(modifier = Modifier.height(12.dp))
                DatePickerField(
                    label = "Meeting date",
                    value = uiState.meetingDateText,
                    onValueChange = { onEvent(MeetingEditorEvent.UpdateMeetingDate(it)) },
                    placeholder = "YYYY-MM-DD",
                )
            }

            SelectionCard(title = "Discussion notes") {
                LabeledTextField(
                    label = "Agenda / context",
                    value = uiState.agenda,
                    onValueChange = { onEvent(MeetingEditorEvent.UpdateAgenda(it)) },
                    minLines = 3,
                    placeholder = "What did you discuss?",
                    enableRichText = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                LabeledTextField(
                    label = "Progress update",
                    value = uiState.progressSummary,
                    onValueChange = { onEvent(MeetingEditorEvent.UpdateProgress(it)) },
                    minLines = 3,
                    placeholder = "Current progress, wins, blockers, changes since last check-in",
                    enableRichText = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                LabeledTextField(
                    label = "Feedback delivered",
                    value = uiState.feedback,
                    onValueChange = { onEvent(MeetingEditorEvent.UpdateFeedback(it)) },
                    minLines = 3,
                    placeholder = "Capture in-the-moment feedback while it is fresh",
                    enableRichText = true,
                )
            }

            SelectionCard(title = "Action items") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.actionItems.forEachIndexed { index, draft ->
                        ActionItemEditor(
                            index = index,
                            draft = draft,
                            onUpdateTitle = { onEvent(MeetingEditorEvent.UpdateActionTitle(index, it)) },
                            onUpdateOwner = { onEvent(MeetingEditorEvent.UpdateActionOwner(index, it)) },
                            onUpdateDueDate = { onEvent(MeetingEditorEvent.UpdateActionDueDate(index, it)) },
                            onUpdateStatus = { onEvent(MeetingEditorEvent.UpdateActionStatus(index, it)) },
                            onUpdateNotes = { onEvent(MeetingEditorEvent.UpdateActionNotes(index, it)) },
                            onRemove = { onEvent(MeetingEditorEvent.RemoveActionItem(index)) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { onEvent(MeetingEditorEvent.AddActionItem) }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Add action item")
                }
            }

            Button(
                onClick = { onEvent(MeetingEditorEvent.Save) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.editingMeetingId == null) "Save interaction" else "Update interaction")
            }
        }
    }
}

@Composable
private fun AddPersonDialog(
    uiState: PeopleUiState,
    onEvent: (PeopleEvent) -> Unit,
) {
    val isEditing = uiState.form.editingPersonId != null

    AlertDialog(
        onDismissRequest = { onEvent(PeopleEvent.DismissDialog) },
        confirmButton = {
            Button(onClick = { onEvent(PeopleEvent.Save) }) {
                Text(if (isEditing) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(PeopleEvent.DismissDialog) }) {
                Text("Cancel")
            }
        },
        title = {
            Text(if (isEditing) "Edit person" else "Add person")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledTextField(
                    label = "Name",
                    value = uiState.form.name,
                    onValueChange = { onEvent(PeopleEvent.UpdateName(it)) },
                    placeholder = "Person name",
                )
                LabeledTextField(
                    label = "Role / title",
                    value = uiState.form.roleTitle,
                    onValueChange = { onEvent(PeopleEvent.UpdateRoleTitle(it)) },
                    placeholder = "Engineering Manager, Product Partner, etc.",
                )
                LabeledTextField(
                    label = "Team",
                    value = uiState.form.team,
                    onValueChange = { onEvent(PeopleEvent.UpdateTeam(it)) },
                    placeholder = "Org or function",
                )
                TypeSelector(
                    selected = uiState.form.type,
                    onSelected = { onEvent(PeopleEvent.UpdateType(it)) },
                )
                LabeledTextField(
                    label = "Notes",
                    value = uiState.form.notes,
                    onValueChange = { onEvent(PeopleEvent.UpdateNotes(it)) },
                    placeholder = "Working style, priorities, stakeholder context",
                    minLines = 3,
                )
            }
        },
    )
}

@Composable
private fun PersonCard(
    person: PersonOverview,
    onClick: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AssistChip(onClick = {}, label = { Text(person.type.label) })
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit person",
                        )
                    }
                }
            }

            Text(
                text = listOf(person.roleTitle, person.team).filter { it.isNotBlank() }.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = person.notes.ifBlank { "No notes added yet." },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = person.lastInteractionAt?.let { "Last interaction: ${formatDate(it)}" }
                    ?: "No interactions logged yet",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MeetingTimelineCard(
    meeting: MeetingRecord,
    onEdit: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = meeting.interactionType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = formatDate(meeting.scheduledAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    LinkTextButton(text = "Edit", onClick = onEdit)
                }
            }

            if (meeting.agenda.isNotBlank()) {
                NoteBlock(title = "Agenda", body = meeting.agenda)
            }
            if (meeting.progressSummary.isNotBlank()) {
                NoteBlock(title = "Progress", body = meeting.progressSummary)
            }
            if (meeting.feedback.isNotBlank()) {
                NoteBlock(title = "Feedback", body = meeting.feedback)
            }

            if (meeting.actionItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Action items",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    meeting.actionItems.forEach { action ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = action.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = listOf(
                                        action.owner.ifBlank { "Owner not set" },
                                        action.dueAt?.let(::formatDate),
                                        action.status.label,
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (action.notes.isNotBlank()) {
                                    RichTextText(
                                        text = action.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionItemCard(
    item: ActionItemSummaryRow,
    onOpenPerson: (Long) -> Unit,
) {
    HighlightCard(
        title = item.actionItem.title,
        subtitle = "${item.personName} • due ${item.actionItem.dueAt?.let(::formatDate) ?: "No due date"}",
        body = listOf(
            ActionStatus.fromStorage(item.actionItem.status).label,
            item.actionItem.owner.ifBlank { "Owner not set" },
            item.actionItem.notes,
        ).filter { it.isNotBlank() }.joinToString(" • "),
        onClick = { onOpenPerson(item.personId) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryGrid(cards: List<SummaryCardData>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        cards.forEach { card ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = card.metric,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = card.caption,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightCard(
    title: String,
    subtitle: String,
    body: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            RichTextText(
                text = body.ifBlank { "No notes recorded." },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RichTextText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = Color.Unspecified,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant
    val heading1Style = MaterialTheme.typography.titleLarge.toSpanStyle().copy(
        color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color,
        fontWeight = FontWeight.Bold,
    )
    val heading2Style = MaterialTheme.typography.titleMedium.toSpanStyle().copy(
        color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color,
        fontWeight = FontWeight.Bold,
    )
    val resolvedColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    val quoteStyle = style.toSpanStyle().copy(
        color = primaryColor,
        fontStyle = FontStyle.Italic,
    )
    val codeStyle = style.toSpanStyle().copy(
        fontFamily = FontFamily.Monospace,
        background = codeBackground,
    )
    val annotated = remember(text, style, resolvedColor, primaryColor, codeBackground, heading1Style, heading2Style, quoteStyle, codeStyle) {
        buildRichTextAnnotatedString(
            input = text,
            baseStyle = style.toSpanStyle(),
            heading1Style = heading1Style,
            heading2Style = heading2Style,
            quoteStyle = quoteStyle,
            codeStyle = codeStyle,
        )
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = style,
        color = resolvedColor,
        maxLines = maxLines,
        overflow = overflow,
    )
}

private fun buildRichTextAnnotatedString(
    input: String,
    baseStyle: SpanStyle,
    heading1Style: SpanStyle,
    heading2Style: SpanStyle,
    quoteStyle: SpanStyle,
    codeStyle: SpanStyle,
): AnnotatedString {
    return buildAnnotatedString {
        input.lines().forEachIndexed { index, line ->
            when {
                line.startsWith("# ") -> withStyle(heading1Style) {
                    appendInlineRichText(line.removePrefix("# "), codeStyle)
                }
                line.startsWith("## ") -> withStyle(heading2Style) {
                    appendInlineRichText(line.removePrefix("## "), codeStyle)
                }
                line.startsWith("> ") -> withStyle(quoteStyle) {
                    append("▎ ")
                    appendInlineRichText(line.removePrefix("> "), codeStyle)
                }
                line.startsWith("- ") || line.startsWith("\u2022 ") -> {
                    withStyle(baseStyle.copy(fontWeight = FontWeight.SemiBold)) { append("• ") }
                    appendInlineRichText(line.drop(2), codeStyle)
                }
                line.matches(Regex("""\d+\.\s.*""")) -> {
                    val separator = line.indexOf(' ')
                    withStyle(baseStyle.copy(fontWeight = FontWeight.SemiBold)) {
                        append(line.substring(0, separator))
                        append(' ')
                    }
                    appendInlineRichText(line.substring(separator + 1), codeStyle)
                }
                else -> appendInlineRichText(line, codeStyle)
            }
            if (index != input.lines().lastIndex) {
                append('\n')
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineRichText(
    text: String,
    codeStyle: SpanStyle,
) {
    var index = 0
    while (index < text.length) {
        when {
            text.startsWith("**", index) -> {
                val end = text.indexOf("**", index + 2)
                if (end == -1) {
                    append(text[index])
                    index += 1
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendInlineRichText(text.substring(index + 2, end), codeStyle)
                    }
                    index = end + 2
                }
            }
            text.startsWith("~~", index) -> {
                val end = text.indexOf("~~", index + 2)
                if (end == -1) {
                    append(text[index])
                    index += 1
                } else {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        appendInlineRichText(text.substring(index + 2, end), codeStyle)
                    }
                    index = end + 2
                }
            }
            text.startsWith("<u>", index) -> {
                val end = text.indexOf("</u>", index + 3)
                if (end == -1) {
                    append(text[index])
                    index += 1
                } else {
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        appendInlineRichText(text.substring(index + 3, end), codeStyle)
                    }
                    index = end + 4
                }
            }
            text.startsWith("`", index) -> {
                val end = text.indexOf("`", index + 1)
                if (end == -1) {
                    append(text[index])
                    index += 1
                } else {
                    withStyle(codeStyle) {
                        append(text.substring(index + 1, end))
                    }
                    index = end + 1
                }
            }
            text.startsWith("_", index) -> {
                val end = text.indexOf("_", index + 1)
                if (end == -1) {
                    append(text[index])
                    index += 1
                } else {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        appendInlineRichText(text.substring(index + 1, end), codeStyle)
                    }
                    index = end + 1
                }
            }
            else -> {
                append(text[index])
                index += 1
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    body: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            RichTextText(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SelectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    allowClear: Boolean = false,
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isDialogOpen = true },
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (allowClear && value.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Clear date",
                        )
                    }
                }
                IconButton(onClick = { isDialogOpen = true }) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = "Pick date",
                    )
                }
            }
        },
    )

    if (isDialogOpen) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = value.toIsoDateOrNull()?.toPickerUtcMillis(),
        )

        DatePickerDialog(
            onDismissRequest = { isDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            onValueChange(selectedDate.toIsoDateString())
                        }
                        isDialogOpen = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
    enableRichText: Boolean = false,
) {
    if (enableRichText) {
        RichMarkdownEditorField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            minLines = minLines,
        )
        return
    }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = value,
                selection = TextRange(value.length),
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { updated ->
                val normalized = if (enableRichText) {
                    continueRichTextList(previous = textFieldValue, current = updated)
                } else {
                    updated
                }
                textFieldValue = normalized
                onValueChange(normalized.text)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            minLines = minLines,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RichMarkdownEditorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int,
) {
    val richTextState = rememberRichTextState()
    val interactionSource = remember { MutableInteractionSource() }
    val isEditorFocused by interactionSource.collectIsFocusedAsState()
    val latestValue by rememberUpdatedState(value)
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = if (isEditorFocused) {
        primaryColor.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
    }
    val editorShape = RoundedCornerShape(18.dp)
    var isApplyingExternalValue by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (richTextState.toMarkdown() != value) {
            isApplyingExternalValue = true
            richTextState.setMarkdown(value)
            isApplyingExternalValue = false
        }
    }

    LaunchedEffect(richTextState) {
        snapshotFlow { richTextState.toMarkdown() }
            .collect { markdown ->
                if (!isApplyingExternalValue && markdown != latestValue) {
                    latestOnValueChange(markdown)
                }
            }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        AnimatedVisibility(visible = isEditorFocused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RichToolbarButton(
                    icon = Icons.Outlined.FormatBold,
                    contentDescription = "Bold",
                    selected = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                    onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                )
                RichToolbarButton(
                    icon = Icons.Outlined.FormatItalic,
                    contentDescription = "Italic",
                    selected = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic,
                    onClick = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                )
                RichToolbarButton(
                    icon = Icons.Outlined.FormatUnderlined,
                    contentDescription = "Underline",
                    selected = richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                    onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                )
                RichToolbarButton(
                    icon = Icons.Outlined.FormatStrikethrough,
                    contentDescription = "Strikethrough",
                    selected = richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                    onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) },
                )
                RichToolbarButton(
                    icon = Icons.Outlined.Code,
                    contentDescription = "Code",
                    selected = false,
                    onClick = { richTextState.toggleCodeSpan() },
                )
                RichToolbarButton(
                    icon = Icons.Outlined.FormatListBulleted,
                    contentDescription = "Bulleted list",
                    selected = false,
                    onClick = { richTextState.toggleUnorderedList() },
                )
                RichToolbarButton(
                    icon = Icons.Outlined.FormatListNumbered,
                    contentDescription = "Numbered list",
                    selected = false,
                    onClick = { richTextState.toggleOrderedList() },
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = editorShape,
            color = surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, outlineColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = (minLines * 24 + 24).dp)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = onSurfaceColor),
                    minLines = minLines,
                    interactionSource = interactionSource,
                    colors = RichTextEditorDefaults.richTextEditorColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                    ),
                )
            }
        }

        Text(
            text = "Formatting is saved as Markdown and synced with your existing notes.",
            style = MaterialTheme.typography.bodySmall,
            color = primaryColor,
        )
    }
}

@Composable
private fun RichToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

private val LinkButtonColor = Color(0xFF0178A4)

@Composable
private fun LinkTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(
            text = text,
            color = LinkButtonColor,
            textDecoration = TextDecoration.Underline,
        )
    }
}

@Composable
private fun NoteBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        RichTextText(text = body, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    selected: PersonType,
    onSelected: (PersonType) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        PersonType.entries.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = PersonType.entries.size,
                ),
            ) {
                Text(type.label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonSelector(
    people: List<PersonEntity>,
    selectedPersonId: Long?,
    onSelected: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = people.firstOrNull { it.id == selectedPersonId }?.name.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Person") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            people.forEach { person ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("${person.name} • ${PersonType.fromStorage(person.personType).label}") },
                    onClick = {
                        onSelected(person.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionStatusSelector(
    selected: ActionStatus,
    onSelected: (ActionStatus) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ActionStatus.entries.forEach { status ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(status.label) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ActionItemEditor(
    index: Int,
    draft: ActionItemDraftUiState,
    onUpdateTitle: (String) -> Unit,
    onUpdateOwner: (String) -> Unit,
    onUpdateDueDate: (String) -> Unit,
    onUpdateStatus: (ActionStatus) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Action item ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (index > 0) {
                    LinkTextButton(text = "Remove", onClick = onRemove)
                }
            }
            LabeledTextField(
                label = "Title",
                value = draft.title,
                onValueChange = onUpdateTitle,
                placeholder = "What needs to happen next?",
            )
            LabeledTextField(
                label = "Owner",
                value = draft.owner,
                onValueChange = onUpdateOwner,
                placeholder = "Who is responsible?",
            )
            DatePickerField(
                label = "Due date",
                value = draft.dueDateText,
                onValueChange = onUpdateDueDate,
                placeholder = "YYYY-MM-DD",
                allowClear = true,
            )
            ActionStatusSelector(
                selected = draft.status,
                onSelected = onUpdateStatus,
            )
            LabeledTextField(
                label = "Notes",
                value = draft.notes,
                onValueChange = onUpdateNotes,
                placeholder = "Dependencies, blockers, or extra context",
                minLines = 2,
                enableRichText = true,
            )
        }
    }
}

@Composable
private fun RichTextActionChip(
    label: String,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
    )
}

private fun applyWrappedStyle(
    value: TextFieldValue,
    prefix: String,
    suffix: String,
): TextFieldValue {
    val start = minOf(value.selection.start, value.selection.end)
    val end = maxOf(value.selection.start, value.selection.end)
    val replacement = if (start == end) "$prefix$suffix" else prefix + value.text.substring(start, end) + suffix
    val updatedText = value.text.replaceRange(start, end, replacement)
    val cursor = if (start == end) start + prefix.length else start + replacement.length
    return value.copy(
        text = updatedText,
        selection = TextRange(cursor),
    )
}

private fun applyLinePrefix(
    value: TextFieldValue,
    prefix: String,
): TextFieldValue {
    val start = minOf(value.selection.start, value.selection.end)
    val lineStart = value.text.lastIndexOf('\n', start - 1).let { if (it == -1) 0 else it + 1 }
    val updatedText = value.text.substring(0, lineStart) + prefix + value.text.substring(lineStart)
    val cursor = value.selection.end + prefix.length
    return value.copy(
        text = updatedText,
        selection = TextRange(cursor),
    )
}

private fun applyNumberedLinePrefix(value: TextFieldValue): TextFieldValue {
    val start = minOf(value.selection.start, value.selection.end)
    val lineStart = value.text.lastIndexOf('\n', start - 1).let { if (it == -1) 0 else it + 1 }
    val currentLine = value.text.substring(lineStart).substringBefore('\n')
    val number = currentLine.takeWhile { it.isDigit() }.toIntOrNull()?.plus(1) ?: 1
    val prefix = "$number. "
    val updatedText = value.text.substring(0, lineStart) + prefix + value.text.substring(lineStart)
    val cursor = value.selection.end + prefix.length
    return value.copy(
        text = updatedText,
        selection = TextRange(cursor),
    )
}

private fun continueRichTextList(previous: TextFieldValue, current: TextFieldValue): TextFieldValue {
    val cursor = current.selection.start
    if (!current.selection.collapsed) return current
    if (current.text.length != previous.text.length + 1) return current
    if (cursor == 0 || current.text.getOrNull(cursor - 1) != '\n') return current

    val lineEnd = cursor - 1
    val lineStart = current.text.lastIndexOf('\n', startIndex = lineEnd - 1).let { index ->
        if (index == -1) 0 else index + 1
    }
    val previousLine = current.text.substring(lineStart, lineEnd)
    val prefix = when {
        previousLine.startsWith("- ") -> "- "
        previousLine.startsWith("\u2022 ") -> "\u2022 "
        previousLine.startsWith("> ") -> "> "
        previousLine.matches(Regex("""\d+\.\s.*""")) -> {
            val currentNumber = previousLine.substringBefore('.').toIntOrNull() ?: return current
            "${currentNumber + 1}. "
        }
        else -> return current
    }

    val isEmptyMarker = previousLine == prefix
    if (isEmptyMarker) {
        val updatedText = current.text.removeRange(lineStart, cursor)
        return current.copy(
            text = updatedText,
            selection = TextRange(lineStart),
        )
    }

    val updatedText = current.text.substring(0, cursor) + prefix + current.text.substring(cursor)
    return current.copy(
        text = updatedText,
        selection = TextRange(cursor + prefix.length),
    )
}

private data class SummaryCardData(
    val title: String,
    val metric: String,
    val caption: String,
)

private fun formatDate(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

private fun buildPersonInteractionExport(
    personName: String,
    meetings: List<MeetingRecord>,
): Pair<String, ByteArray> {
    return buildInteractionWorkbookFileName(personName) to InteractionWorkbookExporter.export(
        personName = personName,
        meetings = meetings.sortedByDescending { it.scheduledAt },
    )
}

private fun buildInteractionWorkbookFileName(personName: String): String {
    val safeName = personName
        .trim()
        .replace(Regex("""[^A-Za-z0-9_-]+"""), "_")
        .trim('_')
        .ifBlank { "person" }
    return "${safeName}_interactions.xlsx"
}

private const val personInteractionWorkbookMimeType =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

private fun String.toIsoDateOrNull(): LocalDate? {
    return runCatching { LocalDate.parse(trim()) }.getOrNull()
}

private fun LocalDate.toPickerUtcMillis(): Long {
    return atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
}

private fun Long.toIsoDateString(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.of("UTC"))
        .toLocalDate()
        .toString()
}

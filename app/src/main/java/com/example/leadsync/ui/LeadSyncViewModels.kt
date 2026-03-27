package com.example.leadsync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.leadsync.data.ActionItemDraft
import com.example.leadsync.data.ActionItemSummaryRow
import com.example.leadsync.data.ActionStatus
import com.example.leadsync.data.DashboardSummary
import com.example.leadsync.data.LeadSyncRepository
import com.example.leadsync.data.MeetingDraft
import com.example.leadsync.data.MeetingSummaryRow
import com.example.leadsync.data.PersonDetail
import com.example.leadsync.data.PersonDraft
import com.example.leadsync.data.PersonEntity
import com.example.leadsync.data.PersonOverview
import com.example.leadsync.data.PersonType
import com.example.leadsync.sync.AutoSyncResult
import com.example.leadsync.sync.CloudSyncCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val totalPeople: Int = 0,
    val reporteeCount: Int = 0,
    val stakeholderCount: Int = 0,
    val openActionItems: Int = 0,
    val dueThisWeek: List<ActionItemSummaryRow> = emptyList(),
    val recentMeetings: List<MeetingSummaryRow> = emptyList(),
    val feedbackHighlights: List<MeetingSummaryRow> = emptyList(),
)

data class PeopleFormState(
    val isDialogOpen: Boolean = false,
    val editingPersonId: Long? = null,
    val name: String = "",
    val roleTitle: String = "",
    val team: String = "",
    val type: PersonType = PersonType.REPORTEE,
    val notes: String = "",
    val message: String? = null,
)

data class PeopleUiState(
    val people: List<PersonOverview> = emptyList(),
    val form: PeopleFormState = PeopleFormState(),
)

data class ActionItemDraftUiState(
    val title: String = "",
    val owner: String = "",
    val dueDateText: String = "",
    val status: ActionStatus = ActionStatus.NOT_STARTED,
    val notes: String = "",
)

data class MeetingEditorUiState(
    val isLoading: Boolean = false,
    val editingMeetingId: Long? = null,
    val people: List<PersonEntity> = emptyList(),
    val selectedPersonId: Long? = null,
    val interactionType: String = "1:1",
    val meetingDateText: String = LocalDate.now().toString(),
    val agenda: String = "",
    val progressSummary: String = "",
    val feedback: String = "",
    val actionItems: List<ActionItemDraftUiState> = listOf(ActionItemDraftUiState()),
    val message: String? = null,
    val completedEditVersion: Int = 0,
)

data class PersonDetailUiState(
    val isLoading: Boolean = true,
    val detail: PersonDetail? = null,
)

sealed interface PeopleEvent {
    data object OpenDialog : PeopleEvent
    data class EditPerson(val person: PersonOverview) : PeopleEvent
    data object DismissDialog : PeopleEvent
    data class UpdateName(val value: String) : PeopleEvent
    data class UpdateRoleTitle(val value: String) : PeopleEvent
    data class UpdateTeam(val value: String) : PeopleEvent
    data class UpdateType(val value: PersonType) : PeopleEvent
    data class UpdateNotes(val value: String) : PeopleEvent
    data object Save : PeopleEvent
    data object ConsumeMessage : PeopleEvent
}

sealed interface MeetingEditorEvent {
    data class SelectPerson(val personId: Long) : MeetingEditorEvent
    data class UpdateInteractionType(val value: String) : MeetingEditorEvent
    data class UpdateMeetingDate(val value: String) : MeetingEditorEvent
    data class UpdateAgenda(val value: String) : MeetingEditorEvent
    data class UpdateProgress(val value: String) : MeetingEditorEvent
    data class UpdateFeedback(val value: String) : MeetingEditorEvent
    data object AddActionItem : MeetingEditorEvent
    data class RemoveActionItem(val index: Int) : MeetingEditorEvent
    data class UpdateActionTitle(val index: Int, val value: String) : MeetingEditorEvent
    data class UpdateActionOwner(val index: Int, val value: String) : MeetingEditorEvent
    data class UpdateActionDueDate(val index: Int, val value: String) : MeetingEditorEvent
    data class UpdateActionStatus(val index: Int, val value: ActionStatus) : MeetingEditorEvent
    data class UpdateActionNotes(val index: Int, val value: String) : MeetingEditorEvent
    data object Save : MeetingEditorEvent
    data object ConsumeMessage : MeetingEditorEvent
    data object ConsumeCompletedEdit : MeetingEditorEvent
}

class DashboardViewModel(
    repository: LeadSyncRepository,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = repository.observeDashboard()
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(),
        )

    companion object {
        fun factory(repository: LeadSyncRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { DashboardViewModel(repository) }
        }
    }
}

class PeopleViewModel(
    private val repository: LeadSyncRepository,
    private val cloudSyncCoordinator: CloudSyncCoordinator,
) : ViewModel() {
    private val formState = MutableStateFlow(PeopleFormState())

    val uiState: StateFlow<PeopleUiState> = combine(
        repository.observePeople(),
        formState,
    ) { people, form ->
        PeopleUiState(
            people = people,
            form = form,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PeopleUiState(),
    )

    fun onEvent(event: PeopleEvent) {
        when (event) {
            PeopleEvent.OpenDialog -> formState.update {
                PeopleFormState(isDialogOpen = true)
            }
            is PeopleEvent.EditPerson -> formState.update {
                PeopleFormState(
                    isDialogOpen = true,
                    editingPersonId = event.person.id,
                    name = event.person.name,
                    roleTitle = event.person.roleTitle,
                    team = event.person.team,
                    type = event.person.type,
                    notes = event.person.notes,
                )
            }
            PeopleEvent.DismissDialog -> formState.update { PeopleFormState() }
            is PeopleEvent.UpdateName -> formState.update { it.copy(name = event.value) }
            is PeopleEvent.UpdateRoleTitle -> formState.update { it.copy(roleTitle = event.value) }
            is PeopleEvent.UpdateTeam -> formState.update { it.copy(team = event.value) }
            is PeopleEvent.UpdateType -> formState.update { it.copy(type = event.value) }
            is PeopleEvent.UpdateNotes -> formState.update { it.copy(notes = event.value) }
            PeopleEvent.Save -> savePerson()
            PeopleEvent.ConsumeMessage -> formState.update { it.copy(message = null) }
        }
    }

    private fun savePerson() {
        val snapshot = formState.value
        if (snapshot.name.isBlank()) {
            formState.update { it.copy(message = "Name is required.") }
            return
        }

        viewModelScope.launch {
            val draft = PersonDraft(
                name = snapshot.name,
                roleTitle = snapshot.roleTitle,
                team = snapshot.team,
                type = snapshot.type,
                notes = snapshot.notes,
            )
            val editingPersonId = snapshot.editingPersonId
            if (editingPersonId == null) {
                repository.addPerson(draft)
            } else {
                repository.updatePerson(editingPersonId, draft)
            }
            val message = buildAutoSyncMessage(
                localSuccess = if (editingPersonId == null) "Person added." else "Person updated.",
                autoSyncResult = cloudSyncCoordinator.pushLatestSnapshot(),
            )
            formState.value = PeopleFormState(
                message = message,
            )
        }
    }

    companion object {
        fun factory(
            repository: LeadSyncRepository,
            cloudSyncCoordinator: CloudSyncCoordinator,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { PeopleViewModel(repository, cloudSyncCoordinator) }
        }
    }
}

class MeetingEditorViewModel(
    private val repository: LeadSyncRepository,
    private val cloudSyncCoordinator: CloudSyncCoordinator,
    preselectedPersonId: Long?,
    editingMeetingId: Long?,
) : ViewModel() {
    private val editorState = MutableStateFlow(
        MeetingEditorUiState(
            isLoading = editingMeetingId != null,
            editingMeetingId = editingMeetingId,
            selectedPersonId = preselectedPersonId,
        ),
    )

    val uiState: StateFlow<MeetingEditorUiState> = combine(
        repository.observeDirectory(),
        editorState,
    ) { people, state ->
        state.copy(
            people = people,
            selectedPersonId = state.selectedPersonId ?: people.firstOrNull()?.id,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MeetingEditorUiState(
            isLoading = editingMeetingId != null,
            editingMeetingId = editingMeetingId,
            selectedPersonId = preselectedPersonId,
        ),
    )

    init {
        if (editingMeetingId != null) {
            loadMeetingForEdit(editingMeetingId)
        }
    }

    fun onEvent(event: MeetingEditorEvent) {
        when (event) {
            is MeetingEditorEvent.SelectPerson -> editorState.update { it.copy(selectedPersonId = event.personId) }
            is MeetingEditorEvent.UpdateInteractionType -> editorState.update { it.copy(interactionType = event.value) }
            is MeetingEditorEvent.UpdateMeetingDate -> editorState.update { it.copy(meetingDateText = event.value) }
            is MeetingEditorEvent.UpdateAgenda -> editorState.update { it.copy(agenda = event.value) }
            is MeetingEditorEvent.UpdateProgress -> editorState.update { it.copy(progressSummary = event.value) }
            is MeetingEditorEvent.UpdateFeedback -> editorState.update { it.copy(feedback = event.value) }
            MeetingEditorEvent.AddActionItem -> editorState.update {
                it.copy(actionItems = it.actionItems + ActionItemDraftUiState())
            }
            is MeetingEditorEvent.RemoveActionItem -> editorState.update {
                val updated = it.actionItems.toMutableList().also { list ->
                    if (event.index in list.indices) list.removeAt(event.index)
                    if (list.isEmpty()) list.add(ActionItemDraftUiState())
                }
                it.copy(actionItems = updated)
            }
            is MeetingEditorEvent.UpdateActionTitle -> updateAction(event.index) { copy(title = event.value) }
            is MeetingEditorEvent.UpdateActionOwner -> updateAction(event.index) { copy(owner = event.value) }
            is MeetingEditorEvent.UpdateActionDueDate -> updateAction(event.index) { copy(dueDateText = event.value) }
            is MeetingEditorEvent.UpdateActionStatus -> updateAction(event.index) { copy(status = event.value) }
            is MeetingEditorEvent.UpdateActionNotes -> updateAction(event.index) { copy(notes = event.value) }
            MeetingEditorEvent.Save -> saveMeeting()
            MeetingEditorEvent.ConsumeMessage -> editorState.update { it.copy(message = null) }
            MeetingEditorEvent.ConsumeCompletedEdit -> editorState.update { it.copy(completedEditVersion = 0) }
        }
    }

    private fun saveMeeting() {
        val snapshot = uiState.value
        if (snapshot.people.isEmpty()) {
            editorState.update { it.copy(message = "Add at least one person before logging a meeting.") }
            return
        }

        if (snapshot.isLoading) {
            editorState.update { it.copy(message = "Interaction is still loading.") }
            return
        }

        val personId = snapshot.selectedPersonId
        if (personId == null) {
            editorState.update { it.copy(message = "Pick a person for this interaction.") }
            return
        }

        try {
            LocalDate.parse(snapshot.meetingDateText.trim())
        } catch (_: Exception) {
            editorState.update { it.copy(message = "Use YYYY-MM-DD for the meeting date.") }
            return
        }

        val actionItems = snapshot.actionItems.mapIndexedNotNull { index, item ->
            if (item.title.isBlank() && item.owner.isBlank() && item.dueDateText.isBlank() && item.notes.isBlank()) {
                null
            } else {
                if (item.dueDateText.isNotBlank()) {
                    try {
                        LocalDate.parse(item.dueDateText.trim())
                    } catch (_: Exception) {
                        editorState.update { it.copy(message = "Action item ${index + 1} has an invalid due date.") }
                        return
                    }
                }
                ActionItemDraft(
                    title = item.title,
                    owner = item.owner,
                    dueDateText = item.dueDateText,
                    status = item.status,
                    notes = item.notes,
                )
            }
        }

        viewModelScope.launch {
            val draft = MeetingDraft(
                personId = personId,
                interactionType = snapshot.interactionType,
                meetingDateText = snapshot.meetingDateText,
                agenda = snapshot.agenda,
                progressSummary = snapshot.progressSummary,
                feedback = snapshot.feedback,
                actionItems = actionItems,
            )
            val editingMeetingId = snapshot.editingMeetingId
            if (editingMeetingId == null) {
                repository.addMeeting(draft)
                val message = buildAutoSyncMessage(
                    localSuccess = "Interaction saved.",
                    autoSyncResult = cloudSyncCoordinator.pushLatestSnapshot(),
                )
                editorState.value = MeetingEditorUiState(
                    selectedPersonId = personId,
                    meetingDateText = LocalDate.now().toString(),
                    message = message,
                )
            } else {
                repository.updateMeeting(editingMeetingId, draft)
                val message = buildAutoSyncMessage(
                    localSuccess = "Interaction updated.",
                    autoSyncResult = cloudSyncCoordinator.pushLatestSnapshot(),
                )
                editorState.value = snapshot.copy(
                    isLoading = false,
                    message = message,
                    actionItems = if (actionItems.isEmpty()) listOf(ActionItemDraftUiState()) else snapshot.actionItems,
                    completedEditVersion = snapshot.completedEditVersion + 1,
                )
            }
        }
    }

    private fun loadMeetingForEdit(meetingId: Long) {
        viewModelScope.launch {
            val editableMeeting = repository.getEditableMeeting(meetingId)
            if (editableMeeting == null) {
                editorState.update {
                    it.copy(
                        isLoading = false,
                        message = "Unable to load this interaction.",
                    )
                }
                return@launch
            }

            editorState.update {
                it.copy(
                    isLoading = false,
                    editingMeetingId = editableMeeting.meetingId,
                    selectedPersonId = editableMeeting.draft.personId,
                    interactionType = editableMeeting.draft.interactionType,
                    meetingDateText = editableMeeting.draft.meetingDateText,
                    agenda = editableMeeting.draft.agenda,
                    progressSummary = editableMeeting.draft.progressSummary,
                    feedback = editableMeeting.draft.feedback,
                    actionItems = editableMeeting.draft.actionItems.map { item ->
                        ActionItemDraftUiState(
                            title = item.title,
                            owner = item.owner,
                            dueDateText = item.dueDateText,
                            status = item.status,
                            notes = item.notes,
                        )
                    },
                )
            }
        }
    }

    private fun updateAction(
        index: Int,
        transform: ActionItemDraftUiState.() -> ActionItemDraftUiState,
    ) {
        editorState.update { state ->
            val updated = state.actionItems.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].transform()
            }
            state.copy(actionItems = updated)
        }
    }

    companion object {
        fun factory(
            repository: LeadSyncRepository,
            cloudSyncCoordinator: CloudSyncCoordinator,
            preselectedPersonId: Long?,
            editingMeetingId: Long?,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeetingEditorViewModel(
                    repository = repository,
                    cloudSyncCoordinator = cloudSyncCoordinator,
                    preselectedPersonId = preselectedPersonId,
                    editingMeetingId = editingMeetingId,
                )
            }
        }
    }
}

class PersonDetailViewModel(
    repository: LeadSyncRepository,
    personId: Long,
) : ViewModel() {
    val uiState: StateFlow<PersonDetailUiState> = repository.observePersonDetail(personId)
        .map { detail ->
            PersonDetailUiState(
                isLoading = false,
                detail = detail,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PersonDetailUiState(),
        )

    companion object {
        fun factory(
            repository: LeadSyncRepository,
            personId: Long,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { PersonDetailViewModel(repository, personId) }
        }
    }
}

private fun DashboardSummary.toUiState(): DashboardUiState {
    return DashboardUiState(
        totalPeople = totalPeople,
        reporteeCount = reporteeCount,
        stakeholderCount = stakeholderCount,
        openActionItems = openActionItems,
        dueThisWeek = dueThisWeek,
        recentMeetings = recentMeetings,
        feedbackHighlights = feedbackHighlights,
    )
}

private fun buildAutoSyncMessage(
    localSuccess: String,
    autoSyncResult: AutoSyncResult,
): String {
    return when (autoSyncResult) {
        AutoSyncResult.SkippedNoSession -> localSuccess
        AutoSyncResult.Synced -> "$localSuccess Synced to cloud."
        is AutoSyncResult.Failed -> "$localSuccess Saved locally, but cloud sync failed: ${autoSyncResult.message}"
    }
}

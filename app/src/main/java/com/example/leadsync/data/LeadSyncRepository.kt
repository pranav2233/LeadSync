package com.example.leadsync.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PersonOverview(
    val id: Long,
    val name: String,
    val roleTitle: String,
    val team: String,
    val type: PersonType,
    val notes: String,
    val lastInteractionAt: Long?,
)

data class MeetingRecord(
    val id: Long,
    val personId: Long,
    val interactionType: String,
    val scheduledAt: Long,
    val agenda: String,
    val progressSummary: String,
    val feedback: String,
    val actionItems: List<ActionItemRecord>,
)

data class ActionItemRecord(
    val id: Long,
    val title: String,
    val owner: String,
    val dueAt: Long?,
    val status: ActionStatus,
    val notes: String,
)

data class PersonDetail(
    val person: PersonOverview,
    val meetings: List<MeetingRecord>,
)

data class DashboardSummary(
    val totalPeople: Int,
    val reporteeCount: Int,
    val stakeholderCount: Int,
    val openActionItems: Int,
    val dueThisWeek: List<ActionItemSummaryRow>,
    val recentMeetings: List<MeetingSummaryRow>,
    val feedbackHighlights: List<MeetingSummaryRow>,
)

data class PersonDraft(
    val name: String,
    val roleTitle: String,
    val team: String,
    val type: PersonType,
    val notes: String,
)

data class ActionItemDraft(
    val title: String,
    val owner: String,
    val dueDateText: String,
    val status: ActionStatus,
    val notes: String,
)

data class MeetingDraft(
    val personId: Long,
    val interactionType: String,
    val meetingDateText: String,
    val agenda: String,
    val progressSummary: String,
    val feedback: String,
    val actionItems: List<ActionItemDraft>,
)

data class EditableMeeting(
    val meetingId: Long,
    val draft: MeetingDraft,
)

class LeadSyncRepository(
    private val personDao: PersonDao,
    private val meetingDao: MeetingDao,
    private val actionItemDao: ActionItemDao,
) {
    fun observeDashboard(): Flow<DashboardSummary> {
        return combine(
            personDao.observePeople(),
            meetingDao.observeMeetingSummaries(),
            actionItemDao.observeActionSummaries(),
        ) { people, meetings, actionItems ->
            val today = LocalDate.now()
            val weekEnd = today.plusDays(7)
            DashboardSummary(
                totalPeople = people.size,
                reporteeCount = people.count { PersonType.fromStorage(it.personType) == PersonType.REPORTEE },
                stakeholderCount = people.count { PersonType.fromStorage(it.personType) == PersonType.STAKEHOLDER },
                openActionItems = actionItems.count { ActionStatus.fromStorage(it.actionItem.status) != ActionStatus.DONE },
                dueThisWeek = actionItems.filter { row ->
                    val status = ActionStatus.fromStorage(row.actionItem.status)
                    val dueDate = row.actionItem.dueAt?.let { millisToLocalDate(it) }
                    status != ActionStatus.DONE && dueDate != null && !dueDate.isBefore(today) && !dueDate.isAfter(weekEnd)
                }.take(5),
                recentMeetings = meetings.take(6),
                feedbackHighlights = meetings.filter { it.meeting.feedback.isNotBlank() }.take(4),
            )
        }
    }

    fun observePeople(): Flow<List<PersonOverview>> {
        return combine(
            personDao.observePeople(),
            meetingDao.observeMeetingSummaries(),
        ) { people, meetings ->
            people.map { person ->
                PersonOverview(
                    id = person.id,
                    name = person.name,
                    roleTitle = person.roleTitle,
                    team = person.team,
                    type = PersonType.fromStorage(person.personType),
                    notes = person.notes,
                    lastInteractionAt = meetings.firstOrNull { it.meeting.personId == person.id }?.meeting?.scheduledAt,
                )
            }
        }
    }

    fun observePersonDetail(personId: Long): Flow<PersonDetail?> {
        return combine(
            personDao.observePerson(personId),
            meetingDao.observeMeetingsForPerson(personId),
        ) { person, meetings ->
            person?.let {
                PersonDetail(
                    person = PersonOverview(
                        id = it.id,
                        name = it.name,
                        roleTitle = it.roleTitle,
                        team = it.team,
                        type = PersonType.fromStorage(it.personType),
                        notes = it.notes,
                        lastInteractionAt = meetings.firstOrNull()?.meeting?.scheduledAt,
                    ),
                    meetings = meetings.map { meeting ->
                        MeetingRecord(
                            id = meeting.meeting.id,
                            personId = meeting.meeting.personId,
                            interactionType = meeting.meeting.interactionType,
                            scheduledAt = meeting.meeting.scheduledAt,
                            agenda = meeting.meeting.agenda,
                            progressSummary = meeting.meeting.progressSummary,
                            feedback = meeting.meeting.feedback,
                            actionItems = meeting.actionItems.map { item ->
                                ActionItemRecord(
                                    id = item.id,
                                    title = item.title,
                                    owner = item.owner,
                                    dueAt = item.dueAt,
                                    status = ActionStatus.fromStorage(item.status),
                                    notes = item.notes,
                                )
                            },
                        )
                    },
                )
            }
        }
    }

    fun observeDirectory(): Flow<List<PersonEntity>> = personDao.observePeople()

    suspend fun addPerson(draft: PersonDraft) {
        personDao.insertPerson(
            PersonEntity(
                name = draft.name.trim(),
                roleTitle = draft.roleTitle.trim(),
                team = draft.team.trim(),
                personType = draft.type.storageValue,
                notes = draft.notes.trim(),
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun updatePerson(personId: Long, draft: PersonDraft) {
        personDao.updatePerson(
            personId = personId,
            name = draft.name.trim(),
            roleTitle = draft.roleTitle.trim(),
            team = draft.team.trim(),
            personType = draft.type.storageValue,
            notes = draft.notes.trim(),
        )
    }

    suspend fun addMeeting(draft: MeetingDraft) {
        val scheduledAt = parseDateToEpochMillis(draft.meetingDateText)
        val meetingId = meetingDao.insertMeeting(
            MeetingEntity(
                personId = draft.personId,
                interactionType = draft.interactionType.trim(),
                scheduledAt = scheduledAt,
                agenda = draft.agenda.trim(),
                progressSummary = draft.progressSummary.trim(),
                feedback = draft.feedback.trim(),
                createdAt = System.currentTimeMillis(),
            ),
        )

        val actionItems = draft.actionItems
            .filter { it.title.isNotBlank() }
            .map { item ->
                ActionItemEntity(
                    meetingId = meetingId,
                    title = item.title.trim(),
                    owner = item.owner.trim(),
                    dueAt = item.dueDateText.takeIf { text -> text.isNotBlank() }?.let(::parseDateToEpochMillis),
                    status = item.status.storageValue,
                    notes = item.notes.trim(),
                    createdAt = System.currentTimeMillis(),
                )
            }

        if (actionItems.isNotEmpty()) {
            actionItemDao.insertActionItems(actionItems)
        }
    }

    suspend fun getEditableMeeting(meetingId: Long): EditableMeeting? {
        return meetingDao.getMeetingWithActions(meetingId)?.let { meeting ->
            EditableMeeting(
                meetingId = meeting.meeting.id,
                draft = MeetingDraft(
                    personId = meeting.meeting.personId,
                    interactionType = meeting.meeting.interactionType,
                    meetingDateText = millisToLocalDate(meeting.meeting.scheduledAt).toString(),
                    agenda = meeting.meeting.agenda,
                    progressSummary = meeting.meeting.progressSummary,
                    feedback = meeting.meeting.feedback,
                    actionItems = meeting.actionItems.map { item ->
                        ActionItemDraft(
                            title = item.title,
                            owner = item.owner,
                            dueDateText = item.dueAt?.let { millisToLocalDate(it).toString() }.orEmpty(),
                            status = ActionStatus.fromStorage(item.status),
                            notes = item.notes,
                        )
                    }.ifEmpty { listOf(ActionItemDraft("", "", "", ActionStatus.NOT_STARTED, "")) },
                ),
            )
        }
    }

    suspend fun updateMeeting(meetingId: Long, draft: MeetingDraft) {
        val scheduledAt = parseDateToEpochMillis(draft.meetingDateText)
        meetingDao.updateMeeting(
            meetingId = meetingId,
            personId = draft.personId,
            interactionType = draft.interactionType.trim(),
            scheduledAt = scheduledAt,
            agenda = draft.agenda.trim(),
            progressSummary = draft.progressSummary.trim(),
            feedback = draft.feedback.trim(),
        )

        actionItemDao.deleteForMeeting(meetingId)

        val actionItems = draft.actionItems
            .filter { it.title.isNotBlank() }
            .map { item ->
                ActionItemEntity(
                    meetingId = meetingId,
                    title = item.title.trim(),
                    owner = item.owner.trim(),
                    dueAt = item.dueDateText.takeIf { text -> text.isNotBlank() }?.let(::parseDateToEpochMillis),
                    status = item.status.storageValue,
                    notes = item.notes.trim(),
                    createdAt = System.currentTimeMillis(),
                )
            }

        if (actionItems.isNotEmpty()) {
            actionItemDao.insertActionItems(actionItems)
        }
    }

    private fun parseDateToEpochMillis(input: String): Long {
        val date = LocalDate.parse(input.trim())
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun millisToLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}

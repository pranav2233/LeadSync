package com.example.leadsync.sync

import com.example.leadsync.data.ActionItemEntity
import com.example.leadsync.data.LeadSyncSnapshot
import com.example.leadsync.data.MeetingEntity
import com.example.leadsync.data.PersonEntity
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val email: String,
)

@Serializable
data class SyncSnapshotRequest(
    val snapshot: CloudSnapshot,
)

@Serializable
data class SyncSnapshotResponse(
    val snapshot: CloudSnapshot? = null,
    val updatedAt: Long? = null,
)

@Serializable
data class CloudSnapshot(
    val people: List<CloudPerson>,
    val meetings: List<CloudMeeting>,
    val actionItems: List<CloudActionItem>,
)

@Serializable
data class CloudPerson(
    val id: Long,
    val name: String,
    val roleTitle: String,
    val team: String,
    val personType: String,
    val notes: String,
    val createdAt: Long,
)

@Serializable
data class CloudMeeting(
    val id: Long,
    val personId: Long,
    val interactionType: String,
    val scheduledAt: Long,
    val agenda: String,
    val progressSummary: String,
    val feedback: String,
    val createdAt: Long,
)

@Serializable
data class CloudActionItem(
    val id: Long,
    val meetingId: Long,
    val title: String,
    val owner: String,
    val dueAt: Long? = null,
    val status: String,
    val notes: String,
    val createdAt: Long,
)

data class StoredSession(
    val baseUrl: String,
    val email: String,
    val accessToken: String,
)

fun LeadSyncSnapshot.toCloudSnapshot(): CloudSnapshot {
    return CloudSnapshot(
        people = people.map {
            CloudPerson(
                id = it.id,
                name = it.name,
                roleTitle = it.roleTitle,
                team = it.team,
                personType = it.personType,
                notes = it.notes,
                createdAt = it.createdAt,
            )
        },
        meetings = meetings.map {
            CloudMeeting(
                id = it.id,
                personId = it.personId,
                interactionType = it.interactionType,
                scheduledAt = it.scheduledAt,
                agenda = it.agenda,
                progressSummary = it.progressSummary,
                feedback = it.feedback,
                createdAt = it.createdAt,
            )
        },
        actionItems = actionItems.map {
            CloudActionItem(
                id = it.id,
                meetingId = it.meetingId,
                title = it.title,
                owner = it.owner,
                dueAt = it.dueAt,
                status = it.status,
                notes = it.notes,
                createdAt = it.createdAt,
            )
        },
    )
}

fun CloudSnapshot.toLocalSnapshot(): LeadSyncSnapshot {
    return LeadSyncSnapshot(
        people = people.map {
            PersonEntity(
                id = it.id,
                name = it.name,
                roleTitle = it.roleTitle,
                team = it.team,
                personType = it.personType,
                notes = it.notes,
                createdAt = it.createdAt,
            )
        },
        meetings = meetings.map {
            MeetingEntity(
                id = it.id,
                personId = it.personId,
                interactionType = it.interactionType,
                scheduledAt = it.scheduledAt,
                agenda = it.agenda,
                progressSummary = it.progressSummary,
                feedback = it.feedback,
                createdAt = it.createdAt,
            )
        },
        actionItems = actionItems.map {
            ActionItemEntity(
                id = it.id,
                meetingId = it.meetingId,
                title = it.title,
                owner = it.owner,
                dueAt = it.dueAt,
                status = it.status,
                notes = it.notes,
                createdAt = it.createdAt,
            )
        },
    )
}

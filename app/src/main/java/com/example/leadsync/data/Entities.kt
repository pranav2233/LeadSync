package com.example.leadsync.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class PersonType(val storageValue: String, val label: String) {
    REPORTEE("REPORTEE", "Reportee"),
    STAKEHOLDER("STAKEHOLDER", "Stakeholder");

    companion object {
        fun fromStorage(value: String): PersonType {
            return entries.firstOrNull { it.storageValue == value } ?: REPORTEE
        }
    }
}

enum class ActionStatus(val storageValue: String, val label: String) {
    NOT_STARTED("NOT_STARTED", "Not started"),
    IN_PROGRESS("IN_PROGRESS", "In progress"),
    BLOCKED("BLOCKED", "Blocked"),
    DONE("DONE", "Done");

    companion object {
        fun fromStorage(value: String): ActionStatus {
            return entries.firstOrNull { it.storageValue == value } ?: NOT_STARTED
        }
    }
}

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roleTitle: String,
    val team: String,
    val personType: String,
    val notes: String,
    val createdAt: Long,
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val interactionType: String,
    val scheduledAt: Long,
    val agenda: String,
    val progressSummary: String,
    val feedback: String,
    val createdAt: Long,
)

@Entity(tableName = "action_items")
data class ActionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val meetingId: Long,
    val title: String,
    val owner: String,
    val dueAt: Long?,
    val status: String,
    val notes: String,
    val createdAt: Long,
)

data class MeetingWithActions(
    @Embedded val meeting: MeetingEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "meetingId",
    )
    val actionItems: List<ActionItemEntity>,
)

data class MeetingSummaryRow(
    @Embedded val meeting: MeetingEntity,
    val personName: String,
    val personType: String,
)

data class ActionItemSummaryRow(
    @Embedded val actionItem: ActionItemEntity,
    val meetingDate: Long,
    val personId: Long,
    val personName: String,
    val personType: String,
)

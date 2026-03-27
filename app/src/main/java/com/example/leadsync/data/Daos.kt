package com.example.leadsync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY name COLLATE NOCASE")
    fun observePeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people ORDER BY id ASC")
    suspend fun getAllPeople(): List<PersonEntity>

    @Query("SELECT * FROM people WHERE id = :personId")
    fun observePerson(personId: Long): Flow<PersonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(people: List<PersonEntity>)

    @Query(
        """
        UPDATE people
        SET name = :name,
            roleTitle = :roleTitle,
            team = :team,
            personType = :personType,
            notes = :notes
        WHERE id = :personId
        """,
    )
    suspend fun updatePerson(
        personId: Long,
        name: String,
        roleTitle: String,
        team: String,
        personType: String,
        notes: String,
    )

    @Query("DELETE FROM people")
    suspend fun deleteAll()
}

@Dao
interface MeetingDao {
    @Query(
        """
        SELECT meetings.*, people.name AS personName, people.personType AS personType
        FROM meetings
        INNER JOIN people ON people.id = meetings.personId
        ORDER BY meetings.scheduledAt DESC
        """,
    )
    fun observeMeetingSummaries(): Flow<List<MeetingSummaryRow>>

    @Transaction
    @Query(
        """
        SELECT * FROM meetings
        WHERE personId = :personId
        ORDER BY scheduledAt DESC
        """,
    )
    fun observeMeetingsForPerson(personId: Long): Flow<List<MeetingWithActions>>

    @Query("SELECT * FROM meetings ORDER BY id ASC")
    suspend fun getAllMeetings(): List<MeetingEntity>

    @Transaction
    @Query("SELECT * FROM meetings WHERE id = :meetingId")
    suspend fun getMeetingWithActions(meetingId: Long): MeetingWithActions?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeetings(meetings: List<MeetingEntity>)

    @Query(
        """
        UPDATE meetings
        SET personId = :personId,
            interactionType = :interactionType,
            scheduledAt = :scheduledAt,
            agenda = :agenda,
            progressSummary = :progressSummary,
            feedback = :feedback
        WHERE id = :meetingId
        """,
    )
    suspend fun updateMeeting(
        meetingId: Long,
        personId: Long,
        interactionType: String,
        scheduledAt: Long,
        agenda: String,
        progressSummary: String,
        feedback: String,
    )

    @Query("DELETE FROM meetings")
    suspend fun deleteAll()
}

@Dao
interface ActionItemDao {
    @Query(
        """
        SELECT action_items.*, meetings.scheduledAt AS meetingDate, meetings.personId AS personId,
               people.name AS personName, people.personType AS personType
        FROM action_items
        INNER JOIN meetings ON meetings.id = action_items.meetingId
        INNER JOIN people ON people.id = meetings.personId
        ORDER BY
            CASE action_items.status
                WHEN 'NOT_STARTED' THEN 0
                WHEN 'IN_PROGRESS' THEN 1
                WHEN 'BLOCKED' THEN 2
                ELSE 3
            END,
            COALESCE(action_items.dueAt, 9223372036854775807) ASC
        """,
    )
    fun observeActionSummaries(): Flow<List<ActionItemSummaryRow>>

    @Query("SELECT * FROM action_items ORDER BY id ASC")
    suspend fun getAllActionItems(): List<ActionItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionItems(actionItems: List<ActionItemEntity>)

    @Query("DELETE FROM action_items WHERE meetingId = :meetingId")
    suspend fun deleteForMeeting(meetingId: Long)

    @Query("DELETE FROM action_items")
    suspend fun deleteAll()
}

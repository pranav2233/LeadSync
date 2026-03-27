package com.example.leadsync.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PersonEntity::class,
        MeetingEntity::class,
        ActionItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class LeadSyncDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun meetingDao(): MeetingDao
    abstract fun actionItemDao(): ActionItemDao

    companion object {
        @Volatile
        private var instance: LeadSyncDatabase? = null

        fun getInstance(context: Context): LeadSyncDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context = context,
                    klass = LeadSyncDatabase::class.java,
                    name = "lead_sync.db",
                ).build().also { instance = it }
            }
        }
    }
}

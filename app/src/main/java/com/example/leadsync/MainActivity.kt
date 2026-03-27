package com.example.leadsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.leadsync.data.LeadSyncDatabase
import com.example.leadsync.data.LeadSyncRepository
import com.example.leadsync.sync.CloudApiService
import com.example.leadsync.sync.SessionStore
import com.example.leadsync.ui.LeadSyncApp
import com.example.leadsync.ui.theme.LeadSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = LeadSyncDatabase.getInstance(applicationContext)
        val repository = LeadSyncRepository(
            database = database,
            personDao = database.personDao(),
            meetingDao = database.meetingDao(),
            actionItemDao = database.actionItemDao(),
        )
        val sessionStore = SessionStore(applicationContext)
        val cloudApiService = CloudApiService()

        setContent {
            LeadSyncTheme {
                LeadSyncApp(
                    repository = repository,
                    sessionStore = sessionStore,
                    cloudApiService = cloudApiService,
                )
            }
        }
    }
}

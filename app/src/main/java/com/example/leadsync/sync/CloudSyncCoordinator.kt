package com.example.leadsync.sync

import com.example.leadsync.data.LeadSyncRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

const val DEFAULT_BACKEND_BASE_URL = "https://leadsync-backend-f4cj.onrender.com"
const val LEGACY_LOCAL_BACKEND_BASE_URL = "http://10.0.2.2:8000"

sealed interface AutoSyncResult {
    data object Synced : AutoSyncResult
    data object SkippedNoSession : AutoSyncResult
    data class Failed(val message: String) : AutoSyncResult
}

class CloudSyncCoordinator(
    private val repository: LeadSyncRepository,
    private val sessionStore: SessionStore,
    private val cloudApiService: CloudApiService,
) {
    private val syncMutex = Mutex()

    suspend fun pushLatestSnapshot(): AutoSyncResult {
        val session = sessionStore.session.value ?: return AutoSyncResult.SkippedNoSession

        return syncMutex.withLock {
            try {
                val snapshot = repository.exportSnapshot().toCloudSnapshot()
                cloudApiService.pushSnapshot(session, snapshot)
                AutoSyncResult.Synced
            } catch (error: Exception) {
                AutoSyncResult.Failed(error.message ?: "Cloud sync failed.")
            }
        }
    }
}

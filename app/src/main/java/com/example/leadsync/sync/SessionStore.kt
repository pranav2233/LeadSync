package com.example.leadsync.sync

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val sessionState = MutableStateFlow(readSession())

    init {
        migrateLegacyBaseUrl()
    }

    val session: StateFlow<StoredSession?> = sessionState

    fun save(session: StoredSession) {
        preferences.edit()
            .putString(KEY_BASE_URL, session.baseUrl)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_TOKEN, session.accessToken)
            .apply()
        sessionState.value = session
    }

    fun clear() {
        preferences.edit().clear().apply()
        sessionState.value = null
    }

    private fun readSession(): StoredSession? {
        val baseUrl = preferences.getString(KEY_BASE_URL, null)
        val email = preferences.getString(KEY_EMAIL, null)
        val token = preferences.getString(KEY_TOKEN, null)
        return if (baseUrl != null && email != null && token != null) {
            StoredSession(
                baseUrl = baseUrl,
                email = email,
                accessToken = token,
            )
        } else {
            null
        }
    }

    private fun migrateLegacyBaseUrl() {
        val session = sessionState.value ?: return
        if (session.baseUrl != LEGACY_LOCAL_BACKEND_BASE_URL) return
        save(session.copy(baseUrl = DEFAULT_BACKEND_BASE_URL))
    }

    private companion object {
        const val PREFS_NAME = "lead_sync_session"
        const val KEY_BASE_URL = "base_url"
        const val KEY_EMAIL = "email"
        const val KEY_TOKEN = "token"
    }
}

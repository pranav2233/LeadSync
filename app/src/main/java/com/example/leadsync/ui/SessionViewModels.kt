package com.example.leadsync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.leadsync.data.LeadSyncRepository
import com.example.leadsync.sync.CloudApiService
import com.example.leadsync.sync.SessionStore
import com.example.leadsync.sync.StoredSession
import com.example.leadsync.sync.toCloudSnapshot
import com.example.leadsync.sync.toLocalSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionFormState(
    val baseUrl: String = "http://10.0.2.2:8000",
    val email: String = "",
    val password: String = "",
    val isWorking: Boolean = false,
    val message: String? = null,
)

data class SessionUiState(
    val session: StoredSession? = null,
    val baseUrl: String = "http://10.0.2.2:8000",
    val email: String = "",
    val password: String = "",
    val isWorking: Boolean = false,
    val message: String? = null,
) {
    val isAuthenticated: Boolean = session != null
}

sealed interface SessionEvent {
    data class UpdateBaseUrl(val value: String) : SessionEvent
    data class UpdateEmail(val value: String) : SessionEvent
    data class UpdatePassword(val value: String) : SessionEvent
    data object Login : SessionEvent
    data object Register : SessionEvent
    data object Logout : SessionEvent
    data object PushSync : SessionEvent
    data object PullSync : SessionEvent
    data object ConsumeMessage : SessionEvent
}

class SessionViewModel(
    private val repository: LeadSyncRepository,
    private val sessionStore: SessionStore,
    private val cloudApiService: CloudApiService,
) : ViewModel() {
    private val formState = MutableStateFlow(SessionFormState())

    val uiState: StateFlow<SessionUiState> = combine(
        sessionStore.session,
        formState,
    ) { session, form ->
        SessionUiState(
            session = session,
            baseUrl = form.baseUrl,
            email = form.email.ifBlank { session?.email.orEmpty() },
            password = form.password,
            isWorking = form.isWorking,
            message = form.message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SessionUiState(session = sessionStore.session.value),
    )

    fun onEvent(event: SessionEvent) {
        when (event) {
            is SessionEvent.UpdateBaseUrl -> formState.update { it.copy(baseUrl = event.value, message = null) }
            is SessionEvent.UpdateEmail -> formState.update { it.copy(email = event.value, message = null) }
            is SessionEvent.UpdatePassword -> formState.update { it.copy(password = event.value, message = null) }
            SessionEvent.Login -> authenticate(register = false)
            SessionEvent.Register -> authenticate(register = true)
            SessionEvent.Logout -> {
                sessionStore.clear()
                formState.update { SessionFormState(baseUrl = it.baseUrl, message = "Signed out.") }
            }
            SessionEvent.PushSync -> pushSync()
            SessionEvent.PullSync -> pullSync()
            SessionEvent.ConsumeMessage -> formState.update { it.copy(message = null) }
        }
    }

    private fun authenticate(register: Boolean) {
        val form = formState.value
        if (form.baseUrl.isBlank() || form.email.isBlank() || form.password.isBlank()) {
            formState.update { it.copy(message = "Base URL, email, and password are required.") }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isWorking = true, message = null) }
            try {
                val session = if (register) {
                    cloudApiService.register(form.baseUrl, form.email, form.password)
                } else {
                    cloudApiService.login(form.baseUrl, form.email, form.password)
                }
                sessionStore.save(session)
                formState.value = SessionFormState(
                    baseUrl = session.baseUrl,
                    email = session.email,
                    message = if (register) {
                        "Account created. Cloud sync is ready."
                    } else {
                        "Signed in. Use Pull cloud to restore or Push cloud to back up this device."
                    },
                )
            } catch (error: Exception) {
                formState.update {
                    it.copy(
                        isWorking = false,
                        message = error.message ?: "Unable to reach the backend.",
                    )
                }
            }
        }
    }

    private fun pushSync() {
        val session = uiState.value.session ?: run {
            formState.update { it.copy(message = "Sign in before pushing a cloud backup.") }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isWorking = true, message = null) }
            try {
                val snapshot = repository.exportSnapshot().toCloudSnapshot()
                cloudApiService.pushSnapshot(session, snapshot)
                formState.update { it.copy(isWorking = false, message = "Cloud backup updated.") }
            } catch (error: Exception) {
                formState.update {
                    it.copy(
                        isWorking = false,
                        message = error.message ?: "Cloud push failed.",
                    )
                }
            }
        }
    }

    private fun pullSync() {
        val session = uiState.value.session ?: run {
            formState.update { it.copy(message = "Sign in before pulling from the cloud.") }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isWorking = true, message = null) }
            try {
                val snapshot = cloudApiService.pullSnapshot(session)
                if (snapshot == null) {
                    formState.update { it.copy(isWorking = false, message = "No cloud backup found for this account.") }
                } else {
                    repository.replaceSnapshot(snapshot.toLocalSnapshot())
                    formState.update { it.copy(isWorking = false, message = "Cloud backup restored to this device.") }
                }
            } catch (error: Exception) {
                formState.update {
                    it.copy(
                        isWorking = false,
                        message = error.message ?: "Cloud pull failed.",
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            repository: LeadSyncRepository,
            sessionStore: SessionStore,
            cloudApiService: CloudApiService,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { SessionViewModel(repository, sessionStore, cloudApiService) }
        }
    }
}

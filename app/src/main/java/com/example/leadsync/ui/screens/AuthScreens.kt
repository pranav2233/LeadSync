package com.example.leadsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.leadsync.ui.SessionEvent
import com.example.leadsync.ui.SessionUiState

@Composable
fun LoginScreen(
    uiState: SessionUiState,
    onEvent: (SessionEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "LeadSync Cloud",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in to keep one account-backed snapshot of your people and interaction history. For the Android emulator, localhost should usually be `http://10.0.2.2:8000`.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = uiState.baseUrl,
            onValueChange = { onEvent(SessionEvent.UpdateBaseUrl(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Backend URL") },
            placeholder = { Text("http://10.0.2.2:8000") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { onEvent(SessionEvent.UpdateEmail(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onEvent(SessionEvent.UpdatePassword(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { onEvent(SessionEvent.Login) },
            enabled = !uiState.isWorking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (uiState.isWorking) "Working..." else "Sign in")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onEvent(SessionEvent.Register) },
            enabled = !uiState.isWorking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create account")
        }
        uiState.message?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

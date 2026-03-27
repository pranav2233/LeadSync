package com.example.leadsync.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentBlue,
    onPrimary = White,
    primaryContainer = AccentBlueSoft,
    onPrimaryContainer = AccentBlue,
    secondary = AccentBlue,
    onSecondary = White,
    secondaryContainer = ContentBackground,
    onSecondaryContainer = Ink,
    background = White,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = ContentBackground,
    onSurfaceVariant = InkMuted,
    outline = AccentBlue.copy(alpha = 0.32f),
)

@Composable
fun LeadSyncTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content,
    )
}

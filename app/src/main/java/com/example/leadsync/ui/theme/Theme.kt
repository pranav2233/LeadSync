package com.example.leadsync.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Sand,
    primaryContainer = Sage,
    onPrimaryContainer = Ink,
    secondary = Clay,
    onSecondary = Sand,
    secondaryContainer = Sand,
    onSecondaryContainer = Ink,
    background = Sand,
    onBackground = Ink,
    surface = Sand,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Ink,
)

private val DarkColors = darkColorScheme(
    primary = Sage,
    onPrimary = Ink,
    primaryContainer = Forest,
    onPrimaryContainer = Sand,
    secondary = Clay,
    onSecondary = Ink,
    secondaryContainer = Ink,
    onSecondaryContainer = Sand,
    background = Ink,
    onBackground = Sand,
    surface = Ink,
    onSurface = Sand,
    surfaceVariant = Forest,
    onSurfaceVariant = Mist,
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

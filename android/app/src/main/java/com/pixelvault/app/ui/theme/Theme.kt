package com.pixelvault.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightShadcnColors = ShadcnColors(
    background = Slate50,
    foreground = Slate950,
    card = Color.White,
    cardForeground = Slate950,
    muted = Slate100,
    mutedForeground = Slate500,
    accent = Violet100,
    accentForeground = Violet900,
    border = Slate200,
    ring = Violet500,
    radius = 8.dp
)

private val DarkShadcnColors = ShadcnColors(
    background = Slate950,
    foreground = Slate50,
    card = Slate900,
    cardForeground = Slate50,
    muted = Slate800,
    mutedForeground = Slate400,
    accent = Violet900,
    accentForeground = Violet50,
    border = Slate700,
    ring = Violet400,
    radius = 8.dp
)

private val LightColorScheme = lightColorScheme(
    primary = Violet500,
    onPrimary = Color.White,
    primaryContainer = Violet100,
    onPrimaryContainer = Violet900,
    secondary = Slate500,
    onSecondary = Color.White,
    secondaryContainer = Slate200,
    onSecondaryContainer = Slate800,
    background = Slate50,
    onBackground = Slate950,
    surface = Color.White,
    onSurface = Slate950,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Violet200,
    outlineVariant = Slate200
)

private val DarkColorScheme = darkColorScheme(
    primary = Violet400,
    onPrimary = Slate950,
    primaryContainer = Violet900,
    onPrimaryContainer = Violet50,
    secondary = Slate400,
    onSecondary = Slate950,
    secondaryContainer = Slate700,
    onSecondaryContainer = Slate200,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Violet700,
    outlineVariant = Slate700
)

@Composable
fun PixelVaultTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val shadcnColors = if (darkTheme) DarkShadcnColors else LightShadcnColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalShadcnColors provides shadcnColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ShadcnTypography,
            shapes = ShadcnShapes,
            content = content
        )
    }
}

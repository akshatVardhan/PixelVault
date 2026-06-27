package com.pixelvault.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ShadcnColors(
    val background: Color,
    val foreground: Color,
    val card: Color,
    val cardForeground: Color,
    val muted: Color,
    val mutedForeground: Color,
    val accent: Color,
    val accentForeground: Color,
    val border: Color,
    val ring: Color,
    val radius: Dp = 8.dp
)

val LocalShadcnColors = staticCompositionLocalOf {
    ShadcnColors(
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
}

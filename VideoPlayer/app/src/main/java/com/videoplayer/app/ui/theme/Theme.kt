package com.videoplayer.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Palette: light blue, white, black.
val VpBlack = Color(0xFF000000)
val VpSurface = Color(0xFF0B0F14)
val VpCard = Color(0xFF121821)
val VpLightBlue = Color(0xFF4FC3F7)
val VpWhite = Color(0xFFFFFFFF)
val VpMuted = Color(0xFF9AA7B4)

private val Scheme = darkColorScheme(
    primary = VpLightBlue, onPrimary = VpBlack,
    background = VpBlack, onBackground = VpWhite,
    surface = VpSurface, onSurface = VpWhite,
    surfaceVariant = VpCard, onSurfaceVariant = VpMuted
)

private val VpTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 13.sp),
    labelSmall = TextStyle(fontSize = 11.sp, color = VpMuted)
)

/** Always-dark premium theme (cinematic base). */
@Composable
fun VideoPlayerTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = Scheme, typography = VpTypography, content = content)

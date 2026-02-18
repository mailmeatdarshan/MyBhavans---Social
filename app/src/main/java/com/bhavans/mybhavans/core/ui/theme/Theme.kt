package com.bhavans.mybhavans.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BhavansPrimary,
    onPrimary = Color.White,
    primaryContainer = BhavansPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = BhavansSecondary,
    onSecondary = Color.White,
    secondaryContainer = BhavansSecondary.copy(alpha = 0.3f),
    onSecondaryContainer = Color.White,
    tertiary = BhavansAccent,
    onTertiary = Color.White,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = Color(0xFF252525),
    surfaceContainerHighest = Color(0xFF303030),
    outline = DividerDark,
    outlineVariant = DividerDark,
    error = ErrorColor,
    onError = Color.White,
    inverseSurface = SurfaceLight,
    inverseOnSurface = TextPrimaryLight
)

private val LightColorScheme = lightColorScheme(
    primary = BhavansPrimary,
    onPrimary = Color.White,
    primaryContainer = BhavansPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = BhavansPrimaryDark,
    secondary = BhavansSecondary,
    onSecondary = Color.White,
    secondaryContainer = BhavansSecondary.copy(alpha = 0.12f),
    onSecondaryContainer = BhavansSecondary,
    tertiary = BhavansAccent,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = Color(0xFFF5F5F5),
    surfaceContainerHighest = Color(0xFFEEEEEE),
    outline = DividerLight,
    outlineVariant = DividerLight,
    error = ErrorColor,
    onError = Color.White,
    inverseSurface = SurfaceDark,
    inverseOnSurface = TextPrimaryDark
)

@Composable
fun MyBhavansTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

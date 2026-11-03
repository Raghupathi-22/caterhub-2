package com.daily.cetaring.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB7D2C2),
    onPrimary = CaterHubColors.Green,
    primaryContainer = Color(0xFF285447),
    onPrimaryContainer = Color(0xFFE2EFE7),
    secondary = Color(0xFFE5D29B),
    onSecondary = Color(0xFF302916),
    secondaryContainer = Color(0xFF504323),
    onSecondaryContainer = Color(0xFFF4E8C6),
    tertiary = CaterHubColors.Gold,
    background = Color(0xFF1D1A18),
    onBackground = Color(0xFFF2EDE7),
    surface = Color(0xFF211E1B),
    onSurface = Color(0xFFF2EDE7),
    surfaceVariant = Color(0xFF39332E),
    onSurfaceVariant = Color(0xFFD2C7BC),
    outline = Color(0xFF8E8175),
    outlineVariant = Color(0xFF50473F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = CaterHubColors.Green,
    onPrimary = Color.White,
    primaryContainer = CaterHubColors.GreenSoft,
    onPrimaryContainer = CaterHubColors.Green,
    secondary = CaterHubColors.Gold,
    onSecondary = CaterHubColors.Ink,
    secondaryContainer = Color(0xFFF3EEDC),
    onSecondaryContainer = CaterHubColors.Ink,
    tertiary = CaterHubColors.Gold,
    background = CaterHubColors.Background,
    onBackground = CaterHubColors.Ink,
    surface = CaterHubColors.Surface,
    onSurface = CaterHubColors.Ink,
    surfaceVariant = Color(0xFFF4EEE5),
    onSurfaceVariant = CaterHubColors.Muted,
    outline = Color(0xFFCDBFAC),
    outlineVariant = CaterHubColors.Border,
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun CetaringTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun CaterHubLightSystemBars() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = true
        controller?.isAppearanceLightNavigationBars = true
        onDispose {
            if (controller != null) {
                previousStatus?.let { controller.isAppearanceLightStatusBars = it }
                previousNavigation?.let { controller.isAppearanceLightNavigationBars = it }
            }
        }
    }
}
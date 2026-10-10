package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedTealPrimaryDark,
    onPrimary = Color(0xFF00363A),
    primaryContainer = MedTealContainerDark,
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = MedCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF01579B),
    onSecondaryContainer = Color(0xFFE1F5FE),
    tertiary = MedCoralTertiary,
    background = MedBackgroundDark,
    surface = MedSurfaceDark,
    surfaceVariant = MedSurfaceVariantDark,
    onBackground = MedOnSurfaceDark,
    onSurface = MedOnSurfaceDark,
    outline = Color(0xFF455A64)
)

private val LightColorScheme = lightColorScheme(
    primary = MedTealPrimary,
    onPrimary = Color.White,
    primaryContainer = MedTealContainer,
    onPrimaryContainer = MedOnTealContainer,
    secondary = MedCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = MedCyanContainer,
    onSecondaryContainer = MedOnCyanContainer,
    tertiary = MedCoralTertiary,
    onTertiary = Color.White,
    background = MedBackgroundLight,
    surface = MedSurfaceLight,
    surfaceVariant = MedSurfaceVariantLight,
    onBackground = MedOnSurfaceLight,
    onSurface = MedOnSurfaceLight,
    onSurfaceVariant = MedOnSurfaceVariantLight,
    outline = MedOutlineLight
)

@Composable
fun MedConnectProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep medical brand colors consistent
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

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = MedConnectProTheme(darkTheme, dynamicColor, content)

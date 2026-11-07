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
import com.example.pinjambalik.ui.theme.AccentTeal
import com.example.pinjambalik.ui.theme.PrimaryBlue
import com.example.pinjambalik.ui.theme.PrimaryBlueDark
import com.example.pinjambalik.ui.theme.PrimaryBlueLight
import com.example.pinjambalik.ui.theme.RoseError
import com.example.pinjambalik.ui.theme.Slate100
import com.example.pinjambalik.ui.theme.Slate50
import com.example.pinjambalik.ui.theme.Slate700
import com.example.pinjambalik.ui.theme.Slate800
import com.example.pinjambalik.ui.theme.Slate900

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = Color.White,
    background = Slate900,
    surface = Slate800,
    onBackground = Slate50,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    error = RoseError
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = PrimaryBlueDark,
    secondary = AccentTeal,
    onSecondary = Color.White,
    background = Slate50,
    surface = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    error = RoseError
)

@Composable
fun PinjamBalikTheme(
    themeMode: String = "LIGHT", // Default Terang (Light) as requested
    dynamicColor: Boolean = false, // Keep consistent branding colors by default
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode.uppercase()) {
        "DARK" -> true
        "SYSTEM" -> isSystemInDarkTheme()
        else -> false // "LIGHT" or default
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

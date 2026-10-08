package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.data.AppThemeConfig

private val MinimalistDarkColorScheme = darkColorScheme(
    primary = AccentMint,
    onPrimary = DarkBackground,
    primaryContainer = AccentMintDark,
    onPrimaryContainer = AccentMint,
    secondary = TextSecondary,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentCoral,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle
)

private val MinimalistLightColorScheme = lightColorScheme(
    primary = AccentMintDark,
    onPrimary = LightSurface,
    primaryContainer = AccentMint,
    onPrimaryContainer = LightBackground,
    secondary = LightTextSecondary,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = AccentCoral,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle
)

@Composable
fun MyApplicationTheme(
    themeConfig: AppThemeConfig = AppThemeConfig.SYSTEM_DEFAULT,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeConfig) {
        AppThemeConfig.SYSTEM_DEFAULT -> isSystemInDarkTheme()
        AppThemeConfig.LIGHT -> false
        AppThemeConfig.DARK -> true
    }

    val colorScheme = if (darkTheme) MinimalistDarkColorScheme else MinimalistLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

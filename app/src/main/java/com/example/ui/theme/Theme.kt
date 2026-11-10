package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalCompactMode = staticCompositionLocalOf { false }

@Composable
fun OpporrsTheme(
    themeMode: String = "SYSTEM",
    accentHex: String = "#285B4B",
    fontFamilyName: String = "Playfair Display",
    fontScalePreset: String = "NORMAL",
    compactMode: Boolean = false,
    languageCode: String = "en",
    content: @Composable () -> Unit
) {
    val pastelPalette = PastelThemePalettes[themeMode]
    val useDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> if (pastelPalette != null) false else isSystemInDarkTheme()
    }

    val colorScheme = when {
        pastelPalette != null -> {
            val customAccent = if (accentHex != "#285B4B") {
                parseHexColor(accentHex, pastelPalette.primary)
            } else {
                pastelPalette.primary
            }
            lightColorScheme(
                primary = customAccent,
                onPrimary = Color.White,
                primaryContainer = pastelPalette.secondarySurface,
                onPrimaryContainer = pastelPalette.mainText,
                secondary = pastelPalette.secondary,
                onSecondary = Color.White,
                secondaryContainer = pastelPalette.secondarySurface,
                onSecondaryContainer = pastelPalette.mainText,
                tertiary = pastelPalette.secondary,
                background = pastelPalette.background,
                onBackground = pastelPalette.mainText,
                surface = pastelPalette.surface,
                onSurface = pastelPalette.mainText,
                surfaceVariant = pastelPalette.secondarySurface,
                onSurfaceVariant = pastelPalette.secondaryText,
                outline = pastelPalette.border,
                outlineVariant = pastelPalette.border.copy(alpha = 0.75f)
            )
        }
        useDark -> {
            val customPrimary = parseHexColor(accentHex, DarkPrimaryGreen)
            val effectivePrimary = if (accentHex == "#285B4B") DarkPrimaryGreen else customPrimary
            darkColorScheme(
                primary = effectivePrimary,
                onPrimary = Color(0xFF10221B),
                primaryContainer = Color(0xFF28473B),
                onPrimaryContainer = DarkMainText,
                secondary = DarkSecondaryGreen,
                onSecondary = DarkMainBackground,
                secondaryContainer = DarkSecondarySurface,
                onSecondaryContainer = DarkMainText,
                tertiary = DarkSecondaryGreen,
                background = DarkMainBackground,
                onBackground = DarkMainText,
                surface = DarkSurface,
                onSurface = DarkMainText,
                surfaceVariant = DarkSecondarySurface,
                onSurfaceVariant = DarkSecondaryText,
                outline = DarkBorder,
                outlineVariant = DarkBorder.copy(alpha = 0.65f)
            )
        }
        else -> {
            val effectivePrimary = parseHexColor(accentHex, LightPrimaryGreen)
            lightColorScheme(
                primary = effectivePrimary,
                onPrimary = Color.White,
                primaryContainer = LightSecondarySurface,
                onPrimaryContainer = LightMainText,
                secondary = LightSecondaryGreen,
                onSecondary = Color.White,
                secondaryContainer = LightSecondarySurface,
                onSecondaryContainer = LightMainText,
                tertiary = LightSecondaryGreen,
                background = LightMainBackground,
                onBackground = LightMainText,
                surface = LightSurface,
                onSurface = LightMainText,
                surfaceVariant = LightSecondarySurface,
                onSurfaceVariant = LightSecondaryText,
                outline = LightBorder,
                outlineVariant = LightBorder.copy(alpha = 0.75f)
            )
        }
    }

    val typography = buildOpporrsTypography(
        selectedFontName = fontFamilyName,
        fontScalePreset = fontScalePreset,
        compactMode = compactMode
    )

    val appStrings = stringsForLanguage(languageCode)

    CompositionLocalProvider(
        LocalCompactMode provides compactMode,
        LocalAppStrings provides appStrings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

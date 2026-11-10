package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val PlayfairDisplayFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.SemiBold),
    Font(R.font.playfair_display, FontWeight.Bold)
)

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val LoraFamily = FontFamily(
    Font(R.font.lora, FontWeight.Normal),
    Font(R.font.lora, FontWeight.Medium),
    Font(R.font.lora, FontWeight.SemiBold),
    Font(R.font.lora, FontWeight.Bold)
)

val MerriweatherFamily = FontFamily(
    Font(R.font.merriweather, FontWeight.Normal),
    Font(R.font.merriweather, FontWeight.SemiBold),
    Font(R.font.merriweather, FontWeight.Bold)
)

val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val CormorantGaramondFamily = FontFamily(
    Font(R.font.cormorant_garamond, FontWeight.Normal),
    Font(R.font.cormorant_garamond, FontWeight.SemiBold),
    Font(R.font.cormorant_garamond, FontWeight.Bold)
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium)
)

val AvailableFontFamilies = listOf(
    "Playfair Display",
    "Plus Jakarta Sans",
    "Lora",
    "Merriweather",
    "Space Grotesk",
    "Cormorant Garamond",
    "JetBrains Mono",
    "System Sans",
    "System Serif"
)

fun resolveFontFamily(fontName: String): FontFamily {
    return when (fontName) {
        "Plus Jakarta Sans" -> PlusJakartaSansFamily
        "Lora" -> LoraFamily
        "Merriweather" -> MerriweatherFamily
        "Space Grotesk" -> SpaceGroteskFamily
        "Cormorant Garamond" -> CormorantGaramondFamily
        "JetBrains Mono" -> JetBrainsMonoFamily
        "System Sans" -> FontFamily.SansSerif
        "System Serif" -> FontFamily.Serif
        else -> PlayfairDisplayFamily
    }
}

fun buildOpporrsTypography(
    selectedFontName: String = "Playfair Display",
    fontScalePreset: String = "NORMAL",
    compactMode: Boolean = false
): Typography {
    val baseMultiplier = when (fontScalePreset) {
        "SMALL" -> 0.90f
        "LARGE" -> 1.10f
        "XLARGE" -> 1.20f
        else -> 1.0f
    }
    val scaleMultiplier = if (compactMode) baseMultiplier * 0.93f else baseMultiplier

    val headingFamily = resolveFontFamily(selectedFontName)

    val bodyFamily = when (selectedFontName) {
        "System Sans" -> FontFamily.SansSerif
        "System Serif" -> FontFamily.Serif
        "Lora" -> LoraFamily
        "Merriweather" -> MerriweatherFamily
        "Space Grotesk" -> SpaceGroteskFamily
        "JetBrains Mono" -> JetBrainsMonoFamily
        else -> PlusJakartaSansFamily
    }

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (26 * scaleMultiplier).sp,
            lineHeight = (32 * scaleMultiplier).sp,
            letterSpacing = (-0.3).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (22 * scaleMultiplier).sp,
            lineHeight = (28 * scaleMultiplier).sp,
            letterSpacing = (-0.2).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * scaleMultiplier).sp,
            lineHeight = (24 * scaleMultiplier).sp
        ),
        titleLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (18 * scaleMultiplier).sp,
            lineHeight = (24 * scaleMultiplier).sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15.5f * scaleMultiplier).sp,
            lineHeight = (21 * scaleMultiplier).sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13.5f * scaleMultiplier).sp,
            lineHeight = (19 * scaleMultiplier).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * scaleMultiplier).sp,
            lineHeight = (22 * scaleMultiplier).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (13.5f * scaleMultiplier).sp,
            lineHeight = (19.5f * scaleMultiplier).sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scaleMultiplier).sp,
            lineHeight = (17 * scaleMultiplier).sp
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13 * scaleMultiplier).sp,
            lineHeight = (18 * scaleMultiplier).sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scaleMultiplier).sp,
            lineHeight = (16 * scaleMultiplier).sp
        ),
        labelSmall = TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scaleMultiplier).sp,
            lineHeight = (15 * scaleMultiplier).sp
        )
    )
}

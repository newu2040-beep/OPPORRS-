package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Palette
val LightMainBackground = Color(0xFFF7F7F2)
val LightSurface = Color(0xFFFFFFFF)
val LightSecondarySurface = Color(0xFFF0F1EB)
val LightPrimaryGreen = Color(0xFF285B4B)
val LightSecondaryGreen = Color(0xFF719080)
val LightMainText = Color(0xFF202A25)
val LightSecondaryText = Color(0xFF78827B)
val LightBorder = Color(0xFFE5E8E1)

// Dark Theme Palette
val DarkMainBackground = Color(0xFF151A18)
val DarkSurface = Color(0xFF202824)
val DarkSecondarySurface = Color(0xFF29332D)
val DarkPrimaryGreen = Color(0xFFA1C7AF)
val DarkSecondaryGreen = Color(0xFF719080)
val DarkMainText = Color(0xFFF2F4F0)
val DarkSecondaryText = Color(0xFFA7B2AA)
val DarkBorder = Color(0xFF354139)

data class PastelThemePalette(
    val code: String,
    val label: String,
    val background: Color,
    val surface: Color,
    val secondarySurface: Color,
    val primary: Color,
    val secondary: Color,
    val mainText: Color,
    val secondaryText: Color,
    val border: Color,
    val swatchHex: String
)

val ThemeModePresets = listOf(
    "SYSTEM" to "System Auto",
    "LIGHT" to "Editorial Light",
    "DARK" to "Editorial Dark",
    "PASTEL_SAGE" to "Pastel Matcha Sage",
    "PASTEL_LAVENDER" to "Pastel Lavender Mist",
    "PASTEL_PEACH" to "Pastel Peach Terracotta",
    "PASTEL_SKY" to "Pastel Sky Blue",
    "PASTEL_ROSE" to "Pastel Blush Rose",
    "PASTEL_BUTTER" to "Pastel Honey Cream",
    "PASTEL_MINT" to "Pastel Mint Glacier"
)

val PastelThemePalettes = mapOf(
    "PASTEL_SAGE" to PastelThemePalette(
        code = "PASTEL_SAGE",
        label = "Pastel Matcha Sage",
        background = Color(0xFFF1F7F3),
        surface = Color(0xFFFFFFFF),
        secondarySurface = Color(0xFFE2EFE7),
        primary = Color(0xFF2D6A4F),
        secondary = Color(0xFF52796F),
        mainText = Color(0xFF1B2E25),
        secondaryText = Color(0xFF5C7468),
        border = Color(0xFFD2E5DA),
        swatchHex = "#B7E4C7"
    ),
    "PASTEL_LAVENDER" to PastelThemePalette(
        code = "PASTEL_LAVENDER",
        label = "Pastel Lavender Mist",
        background = Color(0xFFF6F4FB),
        surface = Color(0xFFFFFFFF),
        secondarySurface = Color(0xFFEAE4F7),
        primary = Color(0xFF5E4B8B),
        secondary = Color(0xFF7D6B9E),
        mainText = Color(0xFF261F38),
        secondaryText = Color(0xFF695E82),
        border = Color(0xFFDCD3F0),
        swatchHex = "#D8CCFA"
    ),
    "PASTEL_PEACH" to PastelThemePalette(
        code = "PASTEL_PEACH",
        label = "Pastel Peach Terracotta",
        background = Color(0xFFFDF5F0),
        surface = Color(0xFFFFFFFF),
        secondarySurface = Color(0xFFF9E5D9),
        primary = Color(0xFFB55435),
        secondary = Color(0xFFD07A5C),
        mainText = Color(0xFF362119),
        secondaryText = Color(0xFF7D5D51),
        border = Color(0xFFF1D4C4),
        swatchHex = "#FCD5CE"
    ),
    "PASTEL_SKY" to PastelThemePalette(
        code = "PASTEL_SKY",
        label = "Pastel Sky Blue",
        background = Color(0xFFF1F7FC),
        surface = Color(0xFFFFFFFF),
        secondarySurface = Color(0xFFE1EEF8),
        primary = Color(0xFF265985),
        secondary = Color(0xFF527FA6),
        mainText = Color(0xFF192A3B),
        secondaryText = Color(0xFF587087),
        border = Color(0xFFCFE2F2),
        swatchHex = "#BDE0FE"
    ),
    "PASTEL_ROSE" to PastelThemePalette(
        code = "PASTEL_ROSE",
        label = "Pastel Blush Rose",
        background = Color(0xFFFCF3F6),
        surface = Color(0xFFFFFFFF),
        secondarySurface = Color(0xFFF7E2E9),
        primary = Color(0xFF9E3F63),
        secondary = Color(0xFFBF6988),
        mainText = Color(0xFF381C27),
        secondaryText = Color(0xFF7D5665),
        border = Color(0xFFF0D1DC),
        swatchHex = "#FFC8DD"
    ),
    "PASTEL_BUTTER" to PastelThemePalette(
        code = "PASTEL_BUTTER",
        label = "Pastel Honey Cream",
        background = Color(0xFFFCFBF0),
        surface = Color(0xFFFFFFFF),
        secondarySurface = Color(0xFFF4EFE0),
        primary = Color(0xFF8A6724),
        secondary = Color(0xFFAD8A47),
        mainText = Color(0xFF332914),
        secondaryText = Color(0xFF786949),
        border = Color(0xFFE8DFC5),
        swatchHex = "#FAEDCD"
    ),
    "PASTEL_MINT" to PastelThemePalette(
        code = "PASTEL_MINT",
        label = "Pastel Mint Glacier",
        background = Color(0xFFF0FAF8),
        surface = Color(0xFFFFFFFF),
        secondarySurface = Color(0xFFDDF2EE),
        primary = Color(0xFF1F6E64),
        secondary = Color(0xFF499187),
        mainText = Color(0xFF162E2B),
        secondaryText = Color(0xFF547873),
        border = Color(0xFFC9E8E2),
        swatchHex = "#A8DADC"
    )
)

// Curated Brand Accent Options for Theme & Template Customization
val BrandAccentPresets = listOf(
    "#285B4B" to "Editorial Forest",
    "#1E4E5D" to "Deep Fjord",
    "#5E4B8B" to "Royal Lavender",
    "#B55435" to "Warm Terracotta",
    "#9E3F63" to "Velvet Rose",
    "#265985" to "Cobalt Harbor",
    "#5B3E28" to "Espresso Umber",
    "#3B4254" to "Nordic Slate",
    "#2D6A4F" to "Botanical Emerald",
    "#8A6724" to "Antique Gold",
    "#1F6E64" to "Ocean Teal",
    "#4B385B" to "Muted Plum"
)

// Document Paper Canvas Color Presets (for Editor & Templates)
val PaperStylePresets = listOf(
    "#FAF8F2" to "Editorial Ivory",
    "#FFFFFF" to "Pure Crisp White",
    "#F1F7F4" to "Pastel Matcha Paper",
    "#FDF4F6" to "Pastel Rose Paper",
    "#F2F7FC" to "Pastel Sky Paper",
    "#F6F4FB" to "Pastel Lavender Paper",
    "#F7F1E3" to "Vintage Parchment",
    "#FDF6F0" to "Pastel Apricot Cream"
)

fun parseHexColor(hex: String, fallback: Color = LightPrimaryGreen): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

package com.example

import com.example.data.model.DocumentSection
import com.example.data.model.DocumentSerializer
import com.example.data.model.SectionType
import com.example.data.templates.DocumentTemplate
import com.example.data.templates.TemplateCatalog
import com.example.data.templates.TemplateLayoutStyle
import com.example.ui.theme.AvailableFontFamilies
import com.example.ui.theme.BrandAccentPresets
import com.example.ui.theme.PaperStylePresets
import com.example.ui.theme.PastelThemePalettes
import com.example.ui.theme.SupportedLanguages
import com.example.ui.theme.ThemeModePresets
import com.example.ui.theme.stringsForLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun templateCatalog_hasAtLeast125TemplatesAcrossAllCategoriesAndSupportsCustomBuilder() {
        val all = TemplateCatalog.allTemplates
        assertTrue("Expected at least 125 templates, found ${all.size}", all.size >= 125)

        val expectedCategories = TemplateCatalog.categories.filter { it != "All" }
        val categoriesPresent = all.map { it.category }.toSet()
        for (category in expectedCategories) {
            assertTrue("Category $category should have templates", categoriesPresent.contains(category))
        }

        val uniqueIds = all.map { it.id }.toSet()
        assertEquals("All template IDs must be unique", all.size, uniqueIds.size)

        for (template in all) {
            val builtSections = template.buildSections()
            assertFalse("Template ${template.id} must build non-empty sections", builtSections.isEmpty())
            assertTrue("Template ${template.id} name should not be blank", template.name.isNotBlank())
            assertTrue("Template ${template.id} paper color should start with #", template.paperColorHex.startsWith("#"))
        }

        val customId = "custom_test_unit_template"
        TemplateCatalog.registerCustomTemplate(
            DocumentTemplate(
                id = customId,
                name = "Unit Test Custom Template",
                category = "Career",
                description = "Custom Builder Verification",
                accentHex = "#285B4B",
                fontFamily = "Playfair Display",
                layoutStyle = TemplateLayoutStyle.MODERN_SIDEBAR,
                paperColorHex = "#F1F7F4",
                isCustom = true,
                buildSections = {
                    listOf(
                        DocumentSection(
                            id = "sec_custom",
                            type = SectionType.HEADING,
                            title = "Custom Heading",
                            content = "Custom content"
                        )
                    )
                }
            )
        )
        assertTrue(
            "Registered custom template should appear in TemplateCatalog.allTemplates",
            TemplateCatalog.allTemplates.any { it.id == customId && it.isCustom }
        )
    }

    @Test
    fun pastelThemesFontsPaperStylesAndLanguages_areConfiguredCorrectly() {
        assertTrue("Expected at least 10 theme modes (including 7 pastel themes)", ThemeModePresets.size >= 10)
        assertEquals(7, PastelThemePalettes.size)
        assertTrue("Expected at least 12 brand accent presets", BrandAccentPresets.size >= 12)
        assertEquals(8, PaperStylePresets.size)
        assertEquals(9, AvailableFontFamilies.size)
        assertEquals(8, SupportedLanguages.size)

        val nepaliStrings = stringsForLanguage("ne")
        assertEquals("गृहपृष्ठ", nepaliStrings.navHome)
        val hindiStrings = stringsForLanguage("hi")
        assertEquals("होम", hindiStrings.navHome)
        val spanishStrings = stringsForLanguage("es")
        assertEquals("Inicio", spanishStrings.navHome)
    }

    @Test
    fun documentMetrics_calculatesWordsAndPagesAccurately() {
        val sections = listOf(
            DocumentSection(
                id = "sec_1",
                type = SectionType.HEADING,
                title = "Executive Summary",
                content = "Opporrs organizes professional documents with clarity and speed."
            ),
            DocumentSection(
                id = "sec_2",
                type = SectionType.TABLE,
                title = "Deliverables",
                tableRows = listOf(
                    listOf("Item", "Owner", "Status"),
                    listOf("Q1 Report", "Rahul Shah", "Completed")
                )
            )
        )

        val wordCount = DocumentSerializer.calculateWordCount(sections)
        assertTrue("Word count should be positive", wordCount >= 10)
        val pageEstimate = DocumentSerializer.calculatePageCount(sections, scannedPagesCount = 0)
        assertEquals(1, pageEstimate)
    }
}

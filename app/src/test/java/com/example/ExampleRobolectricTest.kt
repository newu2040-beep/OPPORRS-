package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentSection
import com.example.data.model.DocumentSerializer
import com.example.data.model.SectionType
import com.example.domain.export.DocumentExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Opporrs", appName)
    }

    @Test
    fun `documentSerializer round-trip and documentExporter PDF TXT CSV validation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sections = listOf(
            DocumentSection(
                id = "s1",
                type = SectionType.PARAGRAPH,
                title = "Executive Overview",
                content = "Prepared by Rahul Shah for Q4 review."
            ),
            DocumentSection(
                id = "s2",
                type = SectionType.TABLE,
                title = "Budget Breakdown",
                tableRows = listOf(
                    listOf("Department", "Allocated", "Actual"),
                    listOf("Engineering", "50000", "48500"),
                    listOf("Design", "25000", "24900")
                )
            )
        )
        val encodedJson = DocumentSerializer.encodeSections(sections)
        val decodedSections = DocumentSerializer.decodeSections(encodedJson)
        assertEquals(2, decodedSections.size)
        assertEquals("Executive Overview", decodedSections[0].title)
        assertEquals(SectionType.TABLE, decodedSections[1].type)

        val sampleDoc = DocumentItem(
            id = "doc_test_01",
            userId = "user_test_01",
            title = "Quarterly Financial & Career Overview",
            category = "Business",
            sectionsData = encodedJson,
            wordCount = 24,
            pageCount = 1
        )

        val pdfResult = DocumentExporter.exportToPdf(context, sampleDoc, decodedSections)
        assertTrue("PDF export should succeed: ${pdfResult.exceptionOrNull()?.message}", pdfResult.isSuccess)
        val pdfExport = pdfResult.getOrThrow()
        assertTrue("PDF file should be non-empty", pdfExport.file.length() > 100L)

        val txtResult = DocumentExporter.exportToTxt(context, sampleDoc, decodedSections)
        assertTrue("TXT export should succeed", txtResult.isSuccess)
        val txtContent = txtResult.getOrThrow().file.readText()
        assertTrue(txtContent.contains("QUARTERLY FINANCIAL & CAREER OVERVIEW"))
        assertTrue(txtContent.contains("Rahul Shah"))

        val csvResult = DocumentExporter.exportToCsv(context, sampleDoc, decodedSections)
        assertTrue("CSV export should succeed", csvResult.isSuccess)
        val csvContent = csvResult.getOrThrow().file.readText()
        assertTrue(csvContent.contains("\"Department\",\"Allocated\",\"Actual\""))
    }
}

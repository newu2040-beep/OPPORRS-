package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject

@IgnoreExtraProperties
data class UserSettings(
    @DocumentId val id: String = "",
    val userId: String = "",
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val accentHex: String = "#285B4B",
    val fontFamily: String = "Playfair Display",
    val fontScale: String = "NORMAL", // SMALL, NORMAL, LARGE, XLARGE
    val compactMode: Boolean = false,
    val languageCode: String = "en",
    val appLockEnabled: Boolean = false,
    val appLockPin: String = "",
    val notificationsEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val website: String = "",
    val summary: String = "",
    val experienceText: String = "",
    val educationText: String = "",
    val skillsText: String = "",
    val referencesText: String = "",
    val avatarUri: String = "",
    val defaultSignatureSvg: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val completionPercentage: Int
        get() {
            val fields = listOf(
                fullName, jobTitle, email, phone, location,
                summary, experienceText, educationText, skillsText
            )
            val filled = fields.count { it.isNotBlank() }
            return ((filled.toFloat() / fields.size) * 100).toInt()
        }
}

@IgnoreExtraProperties
data class DocumentItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "Untitled Document",
    val category: String = "Career",
    val templateId: String = "",
    val folderId: String = "",
    val status: String = "draft", // draft, completed, archived
    val isFavorite: Boolean = false,
    val pageFormat: String = "A4", // A4, Letter
    val orientation: String = "Portrait", // Portrait, Landscape
    val marginMm: Int = 20,
    val accentHex: String = "#285B4B",
    val fontFamily: String = "Playfair Display",
    val sectionsData: String = "[]",
    val signatureSvg: String = "",
    val scannedPagesData: String = "[]",
    val wordCount: Int = 0,
    val pageCount: Int = 1,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FolderItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val colorHex: String = "#285B4B",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SignatureItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val label: String = "Primary Signature",
    val svgPathData: String = "",
    val strokeColorHex: String = "#202A25",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ExportRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val documentId: String = "",
    val documentTitle: String = "",
    val format: String = "PDF", // PDF, TXT, CSV
    val pageSize: String = "A4 Portrait",
    val fileSizeBytes: Int = 0,
    val fileUri: String = "",
    val status: String = "completed", // completed, failed
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class SectionType {
    HEADING,
    PARAGRAPH,
    BULLET_LIST,
    NUMBERED_LIST,
    TABLE,
    IMAGE,
    SIGNATURE,
    PAGE_BREAK
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}

data class DocumentSection(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: SectionType = SectionType.PARAGRAPH,
    val title: String = "",
    val content: String = "",
    val fontSizeSp: Int = 12,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val alignment: TextAlignment = TextAlignment.LEFT,
    val tableRows: List<List<String>> = emptyList(),
    val imageUri: String = ""
)

enum class ScanFilterMode {
    COLOR,
    GRAYSCALE,
    BW
}

data class ScannedPage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val imageUri: String = "",
    val rotationDegrees: Int = 0,
    val filterMode: ScanFilterMode = ScanFilterMode.COLOR,
    val brightness: Float = 0f, // -100f..100f
    val contrast: Float = 1f,   // 0.5f..2.0f
    val cropLeft: Float = 0.05f,
    val cropTop: Float = 0.05f,
    val cropRight: Float = 0.95f,
    val cropBottom: Float = 0.95f,
    val extractedText: String = ""
)

object DocumentSerializer {
    fun encodeSections(sections: List<DocumentSection>): String {
        val arr = JSONArray()
        for (section in sections) {
            val obj = JSONObject()
            obj.put("id", section.id)
            obj.put("type", section.type.name)
            obj.put("title", section.title)
            obj.put("content", section.content)
            obj.put("fontSizeSp", section.fontSizeSp)
            obj.put("isBold", section.isBold)
            obj.put("isItalic", section.isItalic)
            obj.put("isUnderline", section.isUnderline)
            obj.put("alignment", section.alignment.name)
            obj.put("imageUri", section.imageUri)
            val rowsArr = JSONArray()
            for (row in section.tableRows) {
                val colsArr = JSONArray()
                for (cell in row) {
                    colsArr.put(cell)
                }
                rowsArr.put(colsArr)
            }
            obj.put("tableRows", rowsArr)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decodeSections(jsonString: String): List<DocumentSection> {
        if (jsonString.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonString)
            val list = mutableListOf<DocumentSection>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val rows = mutableListOf<List<String>>()
                val rowsArr = obj.optJSONArray("tableRows")
                if (rowsArr != null) {
                    for (r in 0 until rowsArr.length()) {
                        val colsArr = rowsArr.optJSONArray(r)
                        val rowList = mutableListOf<String>()
                        if (colsArr != null) {
                            for (c in 0 until colsArr.length()) {
                                rowList.add(colsArr.optString(c, ""))
                            }
                        }
                        rows.add(rowList)
                    }
                }
                val typeStr = obj.optString("type", SectionType.PARAGRAPH.name)
                val type = runCatching { SectionType.valueOf(typeStr) }.getOrDefault(SectionType.PARAGRAPH)
                val alignStr = obj.optString("alignment", TextAlignment.LEFT.name)
                val alignment = runCatching { TextAlignment.valueOf(alignStr) }.getOrDefault(TextAlignment.LEFT)
                list.add(
                    DocumentSection(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        type = type,
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        fontSizeSp = obj.optInt("fontSizeSp", 12),
                        isBold = obj.optBoolean("isBold", false),
                        isItalic = obj.optBoolean("isItalic", false),
                        isUnderline = obj.optBoolean("isUnderline", false),
                        alignment = alignment,
                        tableRows = rows,
                        imageUri = obj.optString("imageUri", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun encodeScannedPages(pages: List<ScannedPage>): String {
        val arr = JSONArray()
        for (p in pages) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("imageUri", p.imageUri)
            obj.put("rotationDegrees", p.rotationDegrees)
            obj.put("filterMode", p.filterMode.name)
            obj.put("brightness", p.brightness.toDouble())
            obj.put("contrast", p.contrast.toDouble())
            obj.put("cropLeft", p.cropLeft.toDouble())
            obj.put("cropTop", p.cropTop.toDouble())
            obj.put("cropRight", p.cropRight.toDouble())
            obj.put("cropBottom", p.cropBottom.toDouble())
            obj.put("extractedText", p.extractedText)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decodeScannedPages(jsonString: String): List<ScannedPage> {
        if (jsonString.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonString)
            val list = mutableListOf<ScannedPage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val filterStr = obj.optString("filterMode", ScanFilterMode.COLOR.name)
                val filter = runCatching { ScanFilterMode.valueOf(filterStr) }.getOrDefault(ScanFilterMode.COLOR)
                list.add(
                    ScannedPage(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        imageUri = obj.optString("imageUri", ""),
                        rotationDegrees = obj.optInt("rotationDegrees", 0),
                        filterMode = filter,
                        brightness = obj.optDouble("brightness", 0.0).toFloat(),
                        contrast = obj.optDouble("contrast", 1.0).toFloat(),
                        cropLeft = obj.optDouble("cropLeft", 0.05).toFloat(),
                        cropTop = obj.optDouble("cropTop", 0.05).toFloat(),
                        cropRight = obj.optDouble("cropRight", 0.95).toFloat(),
                        cropBottom = obj.optDouble("cropBottom", 0.95).toFloat(),
                        extractedText = obj.optString("extractedText", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun calculateWordCount(sections: List<DocumentSection>): Int {
        var total = 0
        for (s in sections) {
            if (s.title.isNotBlank()) {
                total += s.title.trim().split(Regex("\\s+")).size
            }
            if (s.content.isNotBlank()) {
                total += s.content.trim().split(Regex("\\s+")).size
            }
            for (row in s.tableRows) {
                for (cell in row) {
                    if (cell.isNotBlank()) {
                        total += cell.trim().split(Regex("\\s+")).size
                    }
                }
            }
        }
        return total
    }

    fun calculatePageCount(sections: List<DocumentSection>, scannedPagesCount: Int): Int {
        val explicitBreaks = sections.count { it.type == SectionType.PAGE_BREAK }
        val words = calculateWordCount(sections)
        val estimatedTextPages = ((words / 320) + 1).coerceAtLeast(1)
        return (estimatedTextPages + explicitBreaks + scannedPagesCount).coerceAtLeast(1)
    }
}

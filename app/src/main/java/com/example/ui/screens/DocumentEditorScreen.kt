package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatAlignRight
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.InsertPageBreak
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentSection
import com.example.data.model.SectionType
import com.example.data.model.TextAlignment
import com.example.domain.ai.AiAssistState
import com.example.ui.theme.AvailableFontFamilies
import com.example.ui.theme.BrandAccentPresets
import com.example.ui.theme.LocalCompactMode
import com.example.ui.theme.PaperStylePresets
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.resolveFontFamily

@Composable
fun DocumentEditorScreen(
    document: DocumentItem,
    sections: List<DocumentSection>,
    selectedSectionId: String?,
    saveStatusText: String,
    canUndo: Boolean,
    canRedo: Boolean,
    aiState: AiAssistState,
    onBack: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onSelectSection: (String) -> Unit,
    onUpdateSection: (String, (DocumentSection) -> DocumentSection) -> Unit,
    onAddSection: (SectionType) -> Unit,
    onRemoveSection: (String) -> Unit,
    onMoveSection: (String, Int) -> Unit,
    onFormatSelected: (
        toggleBold: Boolean,
        toggleItalic: Boolean,
        toggleUnderline: Boolean,
        newAlignment: TextAlignment?,
        fontSizeDelta: Int,
        convertToType: SectionType?
    ) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSaveNow: () -> Unit,
    onRequestInsertProfile: () -> Unit,
    onOpenSignaturePad: () -> Unit,
    onOpenExportScreen: () -> Unit,
    onPolishWithAi: (String, Boolean) -> Unit,
    onExecutiveAuditWithAi: () -> Unit,
    onDismissAiState: () -> Unit,
    onUpdateDocumentStyle: (accentHex: String, fontFamily: String) -> Unit = { _, _ -> }
) {
    BackHandler { onBack() }

    val compactMode = LocalCompactMode.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showTitleEditDialog by remember { mutableStateOf(false) }
    var showProfileConfirmDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showAiSheetDialog by remember { mutableStateOf(false) }
    var showPaperStyleDialog by remember { mutableStateOf(false) }

    var selectedPaperColorHex by rememberSaveable(document.id) { mutableStateOf("#FAF8F2") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAddSection(SectionType.IMAGE)
        }
    }

    val paperAccent = parseHexColor(document.accentHex)
    val paperBackgroundColor = parseHexColor(selectedPaperColorHex, Color(0xFFFAF8F2))
    val docFontFamily = resolveFontFamily(document.fontFamily)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // 1. Top Toolbar: Back button, Editable document title, Save status, Undo/Redo, Three-dot menu
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTitleEditDialog = true }
                        .padding(horizontal = 6.dp)
                        .testTag("editor_title_area")
                ) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$saveStatusText • ${document.fontFamily} • ${document.pageCount}p • ${document.wordCount}w",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onUndo, enabled = canUndo) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Undo,
                            contentDescription = "Undo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onRedo, enabled = canRedo) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Redo,
                            contentDescription = "Redo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { showPaperStyleDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.ColorLens,
                            contentDescription = "Paper & Font Style",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { showPreviewDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "Document Preview",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("editor_more_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More editor options"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Paper Color, Accent & Font Studio") },
                                onClick = {
                                    menuExpanded = false
                                    showPaperStyleDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save to Cloud Now") },
                                onClick = {
                                    menuExpanded = false
                                    onSaveNow()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Insert My Profile Details...") },
                                onClick = {
                                    menuExpanded = false
                                    showProfileConfirmDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gemini AI Executive Audit") },
                                onClick = {
                                    menuExpanded = false
                                    onExecutiveAuditWithAi()
                                    showAiSheetDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export PDF / TXT / CSV") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenExportScreen()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. Horizontally Scrollable Compact Formatting Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { showAiSheetDialog = true },
                    label = { Text("Gemini AI") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Gemini Polish",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                AssistChip(
                    onClick = { showPaperStyleDialog = true },
                    label = { Text("Paper & Font") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.ColorLens,
                            contentDescription = "Paper & Font",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                AssistChip(
                    onClick = {
                        onFormatSelected(false, false, false, null, 0, SectionType.HEADING)
                    },
                    label = { Text("Heading") }
                )
                AssistChip(
                    onClick = {
                        onFormatSelected(false, false, false, null, 0, SectionType.PARAGRAPH)
                    },
                    label = { Text("Body") }
                )
                IconButton(
                    onClick = { onFormatSelected(false, false, false, null, 2, null) }
                ) {
                    Icon(Icons.Outlined.FormatSize, contentDescription = "Increase Font Size")
                }
                IconButton(
                    onClick = { onFormatSelected(true, false, false, null, 0, null) },
                    modifier = Modifier.testTag("format_bold_button")
                ) {
                    Icon(Icons.Outlined.FormatBold, contentDescription = "Bold")
                }
                IconButton(
                    onClick = { onFormatSelected(false, true, false, null, 0, null) },
                    modifier = Modifier.testTag("format_italic_button")
                ) {
                    Icon(Icons.Outlined.FormatItalic, contentDescription = "Italic")
                }
                IconButton(
                    onClick = { onFormatSelected(false, false, true, null, 0, null) },
                    modifier = Modifier.testTag("format_underline_button")
                ) {
                    Icon(Icons.Outlined.FormatUnderlined, contentDescription = "Underline")
                }
                IconButton(
                    onClick = { onFormatSelected(false, false, false, TextAlignment.LEFT, 0, null) }
                ) {
                    Icon(Icons.AutoMirrored.Outlined.FormatAlignLeft, contentDescription = "Align Left")
                }
                IconButton(
                    onClick = { onFormatSelected(false, false, false, TextAlignment.CENTER, 0, null) }
                ) {
                    Icon(Icons.Outlined.FormatAlignCenter, contentDescription = "Align Center")
                }
                IconButton(
                    onClick = { onFormatSelected(false, false, false, TextAlignment.RIGHT, 0, null) }
                ) {
                    Icon(Icons.AutoMirrored.Outlined.FormatAlignRight, contentDescription = "Align Right")
                }
                IconButton(
                    onClick = { onFormatSelected(false, false, false, null, 0, SectionType.BULLET_LIST) }
                ) {
                    Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = "Bullet List")
                }
                IconButton(
                    onClick = { onFormatSelected(false, false, false, null, 0, SectionType.NUMBERED_LIST) }
                ) {
                    Icon(Icons.Outlined.FormatListNumbered, contentDescription = "Numbered List")
                }
            }
        }

        // 3. Document Canvas: Realistic customizable pastel/editorial paper document preview
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = if (compactMode) 10.dp else 16.dp,
                vertical = if (compactMode) 10.dp else 16.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .testTag("document_paper_canvas"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = paperBackgroundColor
                    ),
                    border = BorderStroke(1.2.dp, paperAccent.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (compactMode) 14.dp else 20.dp),
                        verticalArrangement = Arrangement.spacedBy(if (compactMode) 10.dp else 14.dp)
                    ) {
                        // Top Editorial Accent Rule on the Paper
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .background(paperAccent, RoundedCornerShape(3.dp))
                        )

                        sections.forEachIndexed { index, section ->
                            val isSelected = section.id == selectedSectionId
                            EditablePaperSection(
                                section = section,
                                index = index,
                                totalCount = sections.size,
                                isSelected = isSelected,
                                accentColor = paperAccent,
                                paperSurfaceColor = paperBackgroundColor,
                                documentFontFamily = docFontFamily,
                                onSelect = { onSelectSection(section.id) },
                                onUpdate = { transform -> onUpdateSection(section.id, transform) },
                                onMoveUp = { onMoveSection(section.id, -1) },
                                onMoveDown = { onMoveSection(section.id, 1) },
                                onRemove = { onRemoveSection(section.id) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Bottom Editing Actions Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddSection(SectionType.PARAGRAPH) },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("editor_add_text_button")
                ) {
                    Icon(Icons.Outlined.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add text", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onAddSection(SectionType.HEADING) },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("editor_add_heading_button")
                ) {
                    Icon(Icons.Outlined.Title, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add heading", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("editor_insert_image_button")
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert image", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onAddSection(SectionType.TABLE) },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("editor_insert_table_button")
                ) {
                    Icon(Icons.Outlined.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert table", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onOpenSignaturePad,
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("editor_add_signature_button")
                ) {
                    Icon(Icons.Outlined.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add signature", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onAddSection(SectionType.PAGE_BREAK) },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.InsertPageBreak, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Page break", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { showProfileConfirmDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Outlined.PersonPin, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert profile", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onOpenExportScreen,
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("editor_export_pdf_button")
                ) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    // Paper Style, Accent Color & Font Studio Dialog
    if (showPaperStyleDialog) {
        AlertDialog(
            onDismissRequest = { showPaperStyleDialog = false },
            title = { Text("Paper Surface, Accent & Font") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Paper Sheet Style", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PaperStylePresets, key = { it.first }) { (hex, label) ->
                            FilterChip(
                                selected = selectedPaperColorHex.equals(hex, ignoreCase = true),
                                onClick = { selectedPaperColorHex = hex },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(parseHexColor(hex), CircleShape)
                                    )
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Document Accent Color", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BrandAccentPresets, key = { it.first }) { (hex, label) ->
                            FilterChip(
                                selected = document.accentHex.equals(hex, ignoreCase = true),
                                onClick = {
                                    onUpdateDocumentStyle(hex, document.fontFamily)
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(parseHexColor(hex), CircleShape)
                                    )
                                },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Document Font Family", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AvailableFontFamilies, key = { it }) { fontName ->
                            FilterChip(
                                selected = document.fontFamily == fontName,
                                onClick = {
                                    onUpdateDocumentStyle(document.accentHex, fontName)
                                },
                                label = { Text(fontName) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPaperStyleDialog = false }) {
                    Text("Apply Style")
                }
            }
        )
    }

    // Edit Document Title Dialog
    if (showTitleEditDialog) {
        var tempTitle by remember(document.title) { mutableStateOf(document.title) }
        AlertDialog(
            onDismissRequest = { showTitleEditDialog = false },
            title = { Text("Document Title") },
            text = {
                OutlinedTextField(
                    value = tempTitle,
                    onValueChange = { tempTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onTitleChanged(tempTitle.ifBlank { "Untitled Document" })
                        showTitleEditDialog = false
                    }
                ) {
                    Text("Save Title")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTitleEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Explicit User Confirmation Before Inserting Personal Profile
    if (showProfileConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showProfileConfirmDialog = false },
            title = { Text("Insert Personal Profile Details?") },
            text = {
                Text(
                    "Your saved name, contact info, summary, experience, education, skills, and signature will be inserted into this document only with your permission."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProfileConfirmDialog = false
                        onRequestInsertProfile()
                    }
                ) {
                    Text("Approve & Insert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clean Read-Only Document Preview Modal
    if (showPreviewDialog) {
        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = { Text("${document.title} — Print Preview") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState())
                        .background(paperBackgroundColor, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(paperAccent, RoundedCornerShape(2.dp))
                    )
                    sections.forEach { sec ->
                        if (sec.type == SectionType.PAGE_BREAK) {
                            HorizontalDivider(color = Color.LightGray)
                        } else {
                            if (sec.title.isNotBlank()) {
                                Text(
                                    text = sec.title,
                                    fontFamily = docFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = paperAccent
                                )
                            }
                            if (sec.content.isNotBlank()) {
                                Text(
                                    text = sec.content,
                                    fontFamily = docFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF202A25)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPreviewDialog = false
                        onOpenExportScreen()
                    }
                ) {
                    Text("Export Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreviewDialog = false }) {
                    Text("Back to Editor")
                }
            }
        )
    }

    // Gemini AI Polish & Executive Audit Dialog
    if (showAiSheetDialog) {
        AlertDialog(
            onDismissRequest = {
                showAiSheetDialog = false
                onDismissAiState()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini Editorial Studio")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enhance the selected section or run a full executive quality review:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                onPolishWithAi("Rewrite in a crisp, persuasive executive tone", false)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Executive Tone", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = {
                                onPolishWithAi("Convert into concise high-impact action bullet points", true)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Action Bullets", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                onPolishWithAi("Expand with detailed professional deliverables and metrics", false)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Expand Detail", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = {
                                onExecutiveAuditWithAi()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Executive Audit", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    when (aiState) {
                        is AiAssistState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text("Gemini is analyzing your document...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is AiAssistState.Streaming -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = aiState.partialText,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        is AiAssistState.Success -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = aiState.resultText,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        is AiAssistState.Error -> {
                            Text(
                                text = aiState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        AiAssistState.Idle -> Unit
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAiSheetDialog = false
                        onDismissAiState()
                    }
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun EditablePaperSection(
    section: DocumentSection,
    index: Int,
    totalCount: Int,
    isSelected: Boolean,
    accentColor: Color,
    paperSurfaceColor: Color,
    documentFontFamily: FontFamily,
    onSelect: () -> Unit,
    onUpdate: ((DocumentSection) -> DocumentSection) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val borderColor = if (isSelected) accentColor else Color(0xFFDADCD5)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color.White else paperSurfaceColor,
        border = BorderStroke(if (isSelected) 1.4.dp else 0.7.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (isSelected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = section.type.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                    Row {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = index > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowUpward,
                                contentDescription = "Move section up",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF202A25)
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = index < totalCount - 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowDownward,
                                contentDescription = "Move section down",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF202A25)
                            )
                        }
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete section",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF8B2626)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (section.type == SectionType.PAGE_BREAK) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(
                        text = "  PAGE BREAK  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF78827B)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }
            } else {
                OutlinedTextField(
                    value = section.title,
                    onValueChange = { newTitle -> onUpdate { it.copy(title = newTitle) } },
                    label = { Text("Section Heading") },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = documentFontFamily,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (section.type == SectionType.HEADING) 16.sp else 13.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                val composeAlign = when (section.alignment) {
                    TextAlignment.CENTER -> TextAlign.Center
                    TextAlignment.RIGHT -> TextAlign.End
                    TextAlignment.LEFT -> TextAlign.Start
                }

                when (section.type) {
                    SectionType.TABLE -> {
                        val rows = section.tableRows.ifEmpty {
                            listOf(listOf("Header 1", "Header 2"), listOf("Value 1", "Value 2"))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            rows.forEachIndexed { rIdx, row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    row.forEachIndexed { cIdx, cellVal ->
                                        OutlinedTextField(
                                            value = cellVal,
                                            onValueChange = { updatedCell ->
                                                val mutableRows = rows.map { it.toMutableList() }.toMutableList()
                                                mutableRows[rIdx][cIdx] = updatedCell
                                                onUpdate { it.copy(tableRows = mutableRows) }
                                            },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                fontFamily = documentFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = if (rIdx == 0) FontWeight.Bold else FontWeight.Normal,
                                                color = Color(0xFF202A25)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SectionType.SIGNATURE -> {
                        OutlinedTextField(
                            value = section.content,
                            onValueChange = { newSigner -> onUpdate { it.copy(content = newSigner) } },
                            label = { Text("Signatory Name / Title") },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 16.sp,
                                color = accentColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    else -> {
                        OutlinedTextField(
                            value = section.content,
                            onValueChange = { newBody -> onUpdate { it.copy(content = newBody) } },
                            label = {
                                Text(
                                    when (section.type) {
                                        SectionType.BULLET_LIST -> "Bullet items (one per line)"
                                        SectionType.NUMBERED_LIST -> "Numbered items (one per line)"
                                        else -> "Content"
                                    }
                                )
                            },
                            textStyle = TextStyle(
                                fontFamily = documentFontFamily,
                                color = Color(0xFF202A25),
                                fontSize = section.fontSizeSp.sp,
                                fontWeight = if (section.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (section.isItalic) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = if (section.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                textAlign = composeAlign
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

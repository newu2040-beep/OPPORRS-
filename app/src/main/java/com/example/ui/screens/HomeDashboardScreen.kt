package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DocumentItem
import com.example.data.model.UserProfile
import com.example.data.templates.DocumentTemplate
import com.example.data.templates.TemplateCatalog
import com.example.ui.components.OpporrsBrandMark
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.resolveFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeDashboardScreen(
    userProfile: UserProfile,
    userDisplayName: String,
    recentDocuments: List<DocumentItem>,
    compactMode: Boolean,
    onSearchSubmitted: (String) -> Unit,
    onCreateDocument: () -> Unit,
    onScanDocument: () -> Unit,
    onCreateCv: () -> Unit,
    onImportFile: () -> Unit,
    onOpenAiGenerator: () -> Unit,
    onSelectTemplate: (DocumentTemplate) -> Unit,
    onExploreAllTemplates: () -> Unit,
    onOpenDocument: (DocumentItem) -> Unit,
    onDuplicateDocument: (DocumentItem) -> Unit,
    onExportDocument: (DocumentItem) -> Unit,
    onDeleteDocument: (DocumentItem) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val strings = LocalAppStrings.current
    var searchText by remember { mutableStateOf("") }
    val featuredTemplates = remember { TemplateCatalog.featuredTemplates }
    val totalTemplateCount = remember { TemplateCatalog.allTemplates.size }

    val greetingPrefix = remember(strings) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> strings.goodMorning
            hour < 17 -> strings.goodAfternoon
            else -> strings.goodEvening
        }
    }

    val firstName = remember(userProfile.fullName, userDisplayName) {
        val source = userProfile.fullName.ifBlank { userDisplayName }
        source.trim().split(" ").firstOrNull()?.takeIf { it.isNotBlank() } ?: ""
    }

    val screenPadH = if (compactMode) 12.dp else 18.dp
    val screenPadV = if (compactMode) 10.dp else 16.dp
    val verticalSpacing = if (compactMode) 12.dp else 20.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = screenPadH, vertical = screenPadV),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        // 1. Top Navigation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OpporrsBrandMark(size = if (compactMode) 30.dp else 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Opporrs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (compactMode) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = "COMPACT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (firstName.isNotBlank()) "Workspace • $firstName" else "Editorial Workspace",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("home_notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onOpenProfile() }
                            .testTag("home_profile_avatar"),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val initial = (firstName.firstOrNull() ?: 'O').uppercaseChar()
                            Text(
                                text = initial.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Welcome Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (firstName.isNotBlank()) "$greetingPrefix, $firstName" else greetingPrefix,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.homeSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(if (compactMode) 8.dp else 12.dp))

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        if (it.length >= 2) {
                            onSearchSubmitted(it)
                        }
                    },
                    placeholder = {
                        Text(
                            text = strings.searchPlaceholder,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotBlank()) {
                            TextButton(onClick = { onSearchSubmitted(searchText) }) {
                                Text("Go")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_field")
                )
            }
        }

        // 3. Quick Actions: Balanced 2 x 2 grid (adapts to Compact Mode)
        item {
            val gridGap = if (compactMode) 8.dp else 12.dp
            Column(verticalArrangement = Arrangement.spacedBy(gridGap)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gridGap)
                ) {
                    QuickActionCard(
                        icon = Icons.Outlined.NoteAdd,
                        label = strings.createDocument,
                        subtitle = "Blank editorial page",
                        compactMode = compactMode,
                        onClick = onCreateDocument,
                        testTag = "quick_action_create_doc",
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.DocumentScanner,
                        label = strings.scanDocument,
                        subtitle = "System & Live Camera",
                        compactMode = compactMode,
                        onClick = onScanDocument,
                        testTag = "quick_action_scan_doc",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gridGap)
                ) {
                    QuickActionCard(
                        icon = Icons.Outlined.Badge,
                        label = strings.createCv,
                        subtitle = "Resume & CV Studio",
                        compactMode = compactMode,
                        onClick = onCreateCv,
                        testTag = "quick_action_create_cv",
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Outlined.UploadFile,
                        label = strings.importFile,
                        subtitle = "TXT, CSV or PDF",
                        compactMode = compactMode,
                        onClick = onImportFile,
                        testTag = "quick_action_import_file",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Gemini AI Studio Assistant Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiGenerator() }
                    .testTag("home_ai_generator_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = if (compactMode) 10.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (compactMode) 34.dp else 40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = "Gemini AI",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.draftWithGemini,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.draftWithGeminiSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Open AI drafter",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 5. Featured Section: "Start with a template"
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.startWithTemplate,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onExploreAllTemplates) {
                        Text("${strings.seeAllTemplates} ($totalTemplateCount+)")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(featuredTemplates, key = { it.id }) { template ->
                        FeaturedTemplateCard(
                            template = template,
                            compactMode = compactMode,
                            onClick = { onSelectTemplate(template) }
                        )
                    }
                }
            }
        }

        // 6. Recent Documents
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.recentDocuments,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (recentDocuments.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No saved documents yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Choose a template above or tap Create document to begin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val maxRecent = if (compactMode) 6 else 4
                    Column(verticalArrangement = Arrangement.spacedBy(if (compactMode) 8.dp else 10.dp)) {
                        recentDocuments.take(maxRecent).forEach { doc ->
                            CompactDocumentRow(
                                document = doc,
                                compactMode = compactMode,
                                onOpen = { onOpenDocument(doc) },
                                onDuplicate = { onDuplicateDocument(doc) },
                                onExport = { onExportDocument(doc) },
                                onDelete = { onDeleteDocument(doc) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    compactMode: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        if (compactMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FeaturedTemplateCard(
    template: DocumentTemplate,
    compactMode: Boolean,
    onClick: () -> Unit
) {
    val accent = parseHexColor(template.accentHex)
    val paperBg = parseHexColor(template.paperColorHex, Color(0xFFFAF8F2))
    val fontFamily = resolveFontFamily(template.fontFamily)

    Card(
        modifier = Modifier
            .width(if (compactMode) 160.dp else 188.dp)
            .clickable { onClick() }
            .testTag("featured_template_${template.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compactMode) 86.dp else 108.dp),
                shape = RoundedCornerShape(10.dp),
                color = paperBg,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(4.dp)
                            .background(accent, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    template.previewLines.take(3).forEachIndexed { idx, line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = fontFamily,
                            color = if (idx == 0) accent else Color(0xFF3E4A43),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${template.category.uppercase()} • ${template.fontFamily.split(" ").firstOrNull().orEmpty()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CompactDocumentRow(
    document: DocumentItem,
    compactMode: Boolean = false,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val editedDate = remember(document.updatedAt, document.createdAt) {
        val ts = document.updatedAt ?: document.createdAt
        if (ts != null) dateFormat.format(ts.toDate()) else "Just now"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("recent_doc_${document.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = if (compactMode) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(if (compactMode) 36.dp else 42.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "Document",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${document.category} • ${document.fontFamily} • $editedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Document options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open in Editor") },
                        onClick = {
                            menuExpanded = false
                            onOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export PDF / TXT / CSV") },
                        onClick = {
                            menuExpanded = false
                            onExport()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

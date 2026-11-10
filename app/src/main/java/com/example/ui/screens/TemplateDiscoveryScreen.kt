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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DocumentSection
import com.example.data.model.SectionType
import com.example.data.templates.DocumentTemplate
import com.example.data.templates.TemplateCatalog
import com.example.data.templates.TemplateLayoutStyle
import com.example.ui.components.EditorialDocumentIllustration
import com.example.ui.theme.AvailableFontFamilies
import com.example.ui.theme.BrandAccentPresets
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalCompactMode
import com.example.ui.theme.PaperStylePresets
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.resolveFontFamily

@Composable
fun TemplateDiscoveryScreen(
    selectedCategory: String,
    searchQuery: String,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onUseTemplate: (DocumentTemplate) -> Unit
) {
    val strings = LocalAppStrings.current
    val compactMode = LocalCompactMode.current

    var catalogVersion by remember { mutableIntStateOf(0) }
    val allTemplates = remember(catalogVersion) { TemplateCatalog.allTemplates }
    var selectedLayoutFilter by remember { mutableStateOf<TemplateLayoutStyle?>(null) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var previewingTemplate by remember { mutableStateOf<DocumentTemplate?>(null) }
    var showCustomBuilderDialog by remember { mutableStateOf(false) }

    val filteredTemplates = remember(allTemplates, selectedCategory, searchQuery, selectedLayoutFilter) {
        allTemplates.filter { tmpl ->
            val matchesCat = selectedCategory == "All" || tmpl.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                tmpl.name.contains(searchQuery, ignoreCase = true) ||
                tmpl.description.contains(searchQuery, ignoreCase = true) ||
                tmpl.category.contains(searchQuery, ignoreCase = true) ||
                tmpl.fontFamily.contains(searchQuery, ignoreCase = true)
            val matchesLayout = selectedLayoutFilter == null || tmpl.layoutStyle == selectedLayoutFilter
            matchesCat && matchesQuery && matchesLayout
        }
    }

    val minGridWidth = if (compactMode) 136.dp else 156.dp
    val gridPad = if (compactMode) 12.dp else 16.dp
    val gridSpacing = if (compactMode) 10.dp else 14.dp

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minGridWidth),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = gridPad, vertical = gridPad),
        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        verticalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        // 1. Top Section: Bold Heading, Custom Template Builder CTA, Search + Filter
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.exploreTemplatesTitle,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${strings.exploreTemplatesSubtitle} (${filteredTemplates.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showCustomBuilderDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("build_custom_template_button")
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.customTemplateBuilder, style = MaterialTheme.typography.labelMedium)
                        }

                        Box {
                            IconButton(
                                onClick = { filterMenuExpanded = true },
                                modifier = Modifier.testTag("template_filter_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterList,
                                    contentDescription = "Filter templates",
                                    tint = if (selectedLayoutFilter != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onBackground
                                    }
                                )
                            }
                            DropdownMenu(
                                expanded = filterMenuExpanded,
                                onDismissRequest = { filterMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Layout Styles") },
                                    onClick = {
                                        selectedLayoutFilter = null
                                        filterMenuExpanded = false
                                    }
                                )
                                TemplateLayoutStyle.values().forEach { style ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                style.name.replace("_", " ").lowercase()
                                                    .replaceFirstChar { it.uppercase() }
                                            )
                                        },
                                        onClick = {
                                            selectedLayoutFilter = style
                                            filterMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search ${allTemplates.size}+ templates, fonts, or styles...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search templates"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "Clear search"
                                )
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
                        .testTag("template_search_field")
                )
            }
        }

        // 2. Horizontally scrollable category chips
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TemplateCatalog.categories, key = { it }) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("category_chip_${category.lowercase()}")
                    )
                }
            }
        }

        // 3. Featured Editorial & Custom Builder Banner
        if (!compactMode) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("featured_opportunity_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PASTEL & EDITORIAL STUDIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Build your next opportunity.",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "130+ templates with pastel paper styles, custom fonts, and custom template builder.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onCategorySelected("Career") },
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.testTag("explore_career_templates_button")
                                ) {
                                    Text("Career Collection")
                                }
                                OutlinedButton(
                                    onClick = { showCustomBuilderDialog = true },
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Icon(Icons.Outlined.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Custom Builder")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        EditorialDocumentIllustration(
                            modifier = Modifier.size(84.dp)
                        )
                    }
                }
            }
        }

        // 4. Adaptive Grid of Template Cards
        items(filteredTemplates, key = { it.id }) { template ->
            TemplateGridCard(
                template = template,
                compactMode = compactMode,
                onPreview = { previewingTemplate = template },
                onUse = { onUseTemplate(template) }
            )
        }
    }

    // Custom Template Builder Dialog
    if (showCustomBuilderDialog) {
        var customName by remember { mutableStateOf("Executive Pastel Brief") }
        var customDesc by remember { mutableStateOf("Custom multi-section template with personalized paper & typography.") }
        var customCategory by remember { mutableStateOf("Career") }
        var customPaperHex by remember { mutableStateOf("#F1F7F4") }
        var customAccentHex by remember { mutableStateOf("#2D6A4F") }
        var customFont by remember { mutableStateOf("Lora") }
        var customPageFormat by remember { mutableStateOf("A4") }
        var customOrientation by remember { mutableStateOf("Portrait") }
        var includeSummary by remember { mutableStateOf(true) }
        var includeExperience by remember { mutableStateOf(true) }
        var includeTable by remember { mutableStateOf(true) }
        var includeSignature by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showCustomBuilderDialog = false },
            title = { Text("Custom Template Studio") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(410.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Template Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customDesc,
                        onValueChange = { customDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TemplateCatalog.categories.filter { it != "All" }, key = { it }) { cat ->
                            FilterChip(
                                selected = customCategory == cat,
                                onClick = { customCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Text("Paper Style Color", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PaperStylePresets, key = { it.first }) { (hex, label) ->
                            FilterChip(
                                selected = customPaperHex == hex,
                                onClick = { customPaperHex = hex },
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

                    Text("Accent Color", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BrandAccentPresets, key = { it.first }) { (hex, label) ->
                            FilterChip(
                                selected = customAccentHex == hex,
                                onClick = { customAccentHex = hex },
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

                    Text("Typography Font Family", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AvailableFontFamilies, key = { it }) { fontName ->
                            FilterChip(
                                selected = customFont == fontName,
                                onClick = { customFont = fontName },
                                label = { Text(fontName) }
                            )
                        }
                    }

                    Text("Page Format & Orientation", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A4", "Letter").forEach { fmt ->
                            FilterChip(
                                selected = customPageFormat == fmt,
                                onClick = { customPageFormat = fmt },
                                label = { Text(fmt) }
                            )
                        }
                        listOf("Portrait", "Landscape").forEach { ori ->
                            FilterChip(
                                selected = customOrientation == ori,
                                onClick = { customOrientation = ori },
                                label = { Text(ori) }
                            )
                        }
                    }

                    Text("Building-Block Sections", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = includeSummary,
                            onClick = { includeSummary = !includeSummary },
                            label = { Text("Summary") }
                        )
                        FilterChip(
                            selected = includeExperience,
                            onClick = { includeExperience = !includeExperience },
                            label = { Text("Bullets") }
                        )
                        FilterChip(
                            selected = includeTable,
                            onClick = { includeTable = !includeTable },
                            label = { Text("Table") }
                        )
                        FilterChip(
                            selected = includeSignature,
                            onClick = { includeSignature = !includeSignature },
                            label = { Text("Signature") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newTemplate = DocumentTemplate(
                            id = "custom_${System.currentTimeMillis()}",
                            name = customName.ifBlank { "Custom Editorial Template" },
                            category = customCategory,
                            description = customDesc.ifBlank { "Custom user-built template" },
                            accentHex = customAccentHex,
                            fontFamily = customFont,
                            layoutStyle = TemplateLayoutStyle.EDITORIAL_SERIF,
                            paperColorHex = customPaperHex,
                            isFeatured = true,
                            isPopular = true,
                            isCustom = true,
                            defaultPageFormat = customPageFormat,
                            defaultOrientation = customOrientation,
                            previewLines = listOf(
                                customName.uppercase(),
                                "$customFont • $customPaperHex",
                                "Custom Building Blocks",
                                "$customPageFormat $customOrientation"
                            ),
                            buildSections = {
                                val list = mutableListOf<DocumentSection>()
                                list.add(
                                    DocumentSection(
                                        type = SectionType.HEADING,
                                        title = customName.uppercase(),
                                        content = "Prepared in Opporrs Custom Template Studio • Font: $customFont",
                                        fontSizeSp = 18,
                                        isBold = true
                                    )
                                )
                                if (includeSummary) {
                                    list.add(
                                        DocumentSection(
                                            type = SectionType.PARAGRAPH,
                                            title = "Executive Overview",
                                            content = customDesc
                                        )
                                    )
                                }
                                if (includeExperience) {
                                    list.add(
                                        DocumentSection(
                                            type = SectionType.BULLET_LIST,
                                            title = "Key Highlights & Milestones",
                                            content = "Primary objective and deliverable #1\nStrategic milestone and impact metric #2\nCross-functional execution highlight #3"
                                        )
                                    )
                                }
                                if (includeTable) {
                                    list.add(
                                        DocumentSection(
                                            type = SectionType.TABLE,
                                            title = "Structured Data & Schedule",
                                            tableRows = listOf(
                                                listOf("Item / Phase", "Owner", "Status"),
                                                listOf("Phase 1", "Lead", "Completed"),
                                                listOf("Phase 2", "Team", "In Progress")
                                            )
                                        )
                                    )
                                }
                                if (includeSignature) {
                                    list.add(
                                        DocumentSection(
                                            type = SectionType.SIGNATURE,
                                            title = "Authorized Signature",
                                            content = "Authorized Signatory"
                                        )
                                    )
                                }
                                list
                            }
                        )
                        TemplateCatalog.registerCustomTemplate(newTemplate)
                        catalogVersion++
                        showCustomBuilderDialog = false
                        onUseTemplate(newTemplate)
                    }
                ) {
                    Text("Save & Open in Editor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomBuilderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full Template Preview Modal Dialog
    if (previewingTemplate != null) {
        val tmpl = previewingTemplate!!
        val sections = remember(tmpl.id) { tmpl.buildSections() }
        val paperBg = parseHexColor(tmpl.paperColorHex, Color(0xFFFAF8F2))
        val accent = parseHexColor(tmpl.accentHex)
        val previewFont = resolveFontFamily(tmpl.fontFamily)

        AlertDialog(
            onDismissRequest = { previewingTemplate = null },
            title = {
                Column {
                    Text(
                        text = "${tmpl.category.uppercase()} • ${tmpl.fontFamily.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                    Text(
                        text = tmpl.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = tmpl.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = paperBg,
                        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(accent, RoundedCornerShape(2.dp))
                            )
                            sections.forEach { sec ->
                                if (sec.title.isNotBlank()) {
                                    Text(
                                        text = sec.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontFamily = previewFont,
                                        color = accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (sec.content.isNotBlank()) {
                                    Text(
                                        text = sec.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = previewFont,
                                        color = Color(0xFF202A25)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        previewingTemplate = null
                        onUseTemplate(tmpl)
                    }
                ) {
                    Text("Use This Template")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewingTemplate = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun TemplateGridCard(
    template: DocumentTemplate,
    compactMode: Boolean,
    onPreview: () -> Unit,
    onUse: () -> Unit
) {
    val accent = parseHexColor(template.accentHex)
    val paperBg = parseHexColor(template.paperColorHex, Color(0xFFFAF8F2))
    val fontFamily = resolveFontFamily(template.fontFamily)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() }
            .testTag("template_card_${template.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(if (compactMode) 10.dp else 12.dp)) {
            // Realistic miniature document thumbnail tinted with template's paperColorHex
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compactMode) 90.dp else 116.dp),
                shape = RoundedCornerShape(10.dp),
                color = paperBg,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(4.dp)
                                .background(accent, RoundedCornerShape(2.dp))
                        )
                        if (template.isCustom) {
                            Text(
                                text = "CUSTOM",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent
                            )
                        } else if (template.isPopular) {
                            Text(
                                text = "POPULAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    template.previewLines.take(if (compactMode) 2 else 3).forEachIndexed { idx, line ->
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = template.fontFamily.split(" ").firstOrNull().orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!compactMode) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = "Preview",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onUse,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("use_template_${template.id}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Use", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

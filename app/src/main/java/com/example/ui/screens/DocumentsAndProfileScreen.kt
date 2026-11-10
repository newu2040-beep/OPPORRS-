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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DocumentItem
import com.example.data.model.FolderItem
import com.example.data.model.UserProfile
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalCompactMode
import com.example.ui.viewmodel.DataUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DocumentsAndProfileScreen(
    documentsState: DataUiState<List<DocumentItem>>,
    folders: List<FolderItem>,
    selectedFolderId: String,
    selectedFilter: String,
    searchQuery: String,
    userProfile: UserProfile,
    userEmail: String,
    onSearchQueryChanged: (String) -> Unit,
    onFilterChanged: (String) -> Unit,
    onSelectFolder: (String) -> Unit,
    onCreateDocument: () -> Unit,
    onCreateFolder: (String, String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onOpenDocument: (DocumentItem) -> Unit,
    onRenameDocument: (DocumentItem, String) -> Unit,
    onDuplicateDocument: (DocumentItem) -> Unit,
    onMoveDocument: (DocumentItem, String) -> Unit,
    onShareDocument: (DocumentItem) -> Unit,
    onExportDocument: (DocumentItem) -> Unit,
    onToggleArchiveDocument: (DocumentItem) -> Unit,
    onMarkCompletedDocument: (DocumentItem) -> Unit,
    onToggleFavoriteDocument: (DocumentItem) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onOpenProfileEditor: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isCompact = LocalCompactMode.current
    val filters = listOf("All", "Recent", "Drafts", "Completed", "Archived")
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var renamingDoc by remember { mutableStateOf<DocumentItem?>(null) }
    var movingDoc by remember { mutableStateOf<DocumentItem?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            horizontal = if (isCompact) 14.dp else 20.dp,
            vertical = if (isCompact) 10.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 16.dp)
    ) {
        // 1. Top Section: Heading "My documents", Search, Create button
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.myDocumentsTitle,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = onCreateDocument,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("docs_create_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create document",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.createDocument)
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text(strings.searchPlaceholder) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search documents"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear search")
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
                        .testTag("docs_search_field")
                )
            }
        }

        // 2. Profile Shortcut Card (Compact personal profile card with photo, name, completion status, Edit profile action)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_shortcut_card"),
                shape = RoundedCornerShape(if (isCompact) 14.dp else 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(if (isCompact) 12.dp else 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(if (isCompact) 38.dp else 46.dp)
                                    .clip(CircleShape),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val initial = (userProfile.fullName.firstOrNull()
                                        ?: userEmail.firstOrNull()
                                        ?: 'R').uppercaseChar()
                                    Text(
                                        text = initial.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userProfile.fullName.ifBlank { "Personal Profile" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = userProfile.jobTitle.ifBlank {
                                        userProfile.email.ifBlank { userEmail.ifBlank { "Add reusable CV & contact details" } }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenProfileEditor,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.testTag("edit_profile_shortcut_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit profile")
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))

                    val completion = userProfile.completionPercentage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Contact & CV fields completion",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$completion%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { completion / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // 3. Folders Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.foldersTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { showCreateFolderDialog = true },
                        modifier = Modifier.testTag("create_folder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = "New Folder",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.newFolder)
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedFolderId.isEmpty(),
                            onClick = { onSelectFolder("") },
                            label = { Text("All Folders") },
                            shape = RoundedCornerShape(18.dp)
                        )
                    }
                    items(folders, key = { it.id }) { folder ->
                        FilterChip(
                            selected = selectedFolderId == folder.id,
                            onClick = {
                                onSelectFolder(if (selectedFolderId == folder.id) "" else folder.id)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(folder.name) },
                            shape = RoundedCornerShape(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Document Status Filters (All, Recent, Drafts, Completed, Archived)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters, key = { it }) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChanged(filter) },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("doc_filter_${filter.lowercase()}")
                    )
                }
            }
        }

        // 5. Document List
        when (documentsState) {
            is DataUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is DataUiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = documentsState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            is DataUiState.Success -> {
                val filteredDocs = documentsState.data.filter { doc ->
                    val matchesSearch = searchQuery.isBlank() ||
                        doc.title.contains(searchQuery, ignoreCase = true) ||
                        doc.category.contains(searchQuery, ignoreCase = true)
                    val matchesFolder = selectedFolderId.isEmpty() || doc.folderId == selectedFolderId
                    val matchesStatus = when (selectedFilter) {
                        "Drafts" -> doc.status == "draft"
                        "Completed" -> doc.status == "completed"
                        "Archived" -> doc.status == "archived"
                        "Recent" -> doc.status != "archived"
                        else -> doc.status != "archived"
                    }
                    matchesSearch && matchesFolder && matchesStatus
                }

                if (filteredDocs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No matching documents",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create a new document or adjust your search and folder filters.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredDocs, key = { it.id }) { doc ->
                        FullDocumentRow(
                            document = doc,
                            isCompact = isCompact,
                            onOpen = { onOpenDocument(doc) },
                            onRename = { renamingDoc = doc },
                            onDuplicate = { onDuplicateDocument(doc) },
                            onMove = { movingDoc = doc },
                            onShare = { onShareDocument(doc) },
                            onExport = { onExportDocument(doc) },
                            onToggleArchive = { onToggleArchiveDocument(doc) },
                            onMarkCompleted = { onMarkCompletedDocument(doc) },
                            onToggleFavorite = { onToggleFavoriteDocument(doc) },
                            onDelete = { onDeleteDocument(doc.id) }
                        )
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        var folderDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create New Folder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = folderDesc,
                        onValueChange = { folderDesc = it },
                        label = { Text("Description (optional)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            onCreateFolder(folderName, folderDesc, "#285B4B")
                            showCreateFolderDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Document Dialog
    if (renamingDoc != null) {
        val target = renamingDoc!!
        var newName by remember(target.id) { mutableStateOf(target.title) }
        AlertDialog(
            onDismissRequest = { renamingDoc = null },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Document Title") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameDocument(target, newName)
                        renamingDoc = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingDoc = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Move Document to Folder Dialog
    if (movingDoc != null) {
        val target = movingDoc!!
        AlertDialog(
            onDismissRequest = { movingDoc = null },
            title = { Text("Move \"${target.title}\" to Folder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = {
                            onMoveDocument(target, "")
                            movingDoc = null
                        }
                    ) {
                        Text("Remove from folder (Root Workspace)")
                    }
                    if (folders.isEmpty()) {
                        Text(
                            text = "No custom folders created yet. Tap 'New Folder' first.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        folders.forEach { folder ->
                            OutlinedButton(
                                onClick = {
                                    onMoveDocument(target, folder.id)
                                    movingDoc = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(folder.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { movingDoc = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun FullDocumentRow(
    document: DocumentItem,
    isCompact: Boolean,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
    onToggleArchive: () -> Unit,
    onMarkCompleted: () -> Unit,
    onToggleFavorite: () -> Unit,
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
            .testTag("doc_row_${document.id}"),
        shape = RoundedCornerShape(if (isCompact) 12.dp else 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isCompact) 10.dp else 14.dp,
                    vertical = if (isCompact) 8.dp else 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(if (isCompact) 36.dp else 44.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "Document",
                            tint = MaterialTheme.colorScheme.primary
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
                        text = "${document.pageFormat} • ${document.category} • ${document.status.uppercase()} • $editedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (document.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (document.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("doc_overflow_${document.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Document actions"
                        )
                    }
                    // Functional actions required by Screen 05 spec:
                    // Open, Rename, Duplicate, Move, Share, Export, Archive, Delete
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open") },
                            onClick = {
                                menuExpanded = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                menuExpanded = false
                                onRename()
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
                            text = { Text("Move to Folder") },
                            onClick = {
                                menuExpanded = false
                                onMove()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (document.status == "completed") "Mark as Draft" else "Mark as Completed")
                            },
                            onClick = {
                                menuExpanded = false
                                onMarkCompleted()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                menuExpanded = false
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export (PDF / TXT / CSV)") },
                            onClick = {
                                menuExpanded = false
                                onExport()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (document.status == "archived") "Unarchive" else "Archive")
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleArchive()
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
}

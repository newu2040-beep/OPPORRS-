package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentSection
import com.example.data.model.DocumentSerializer
import com.example.data.model.ExportRecord
import com.example.data.model.FolderItem
import com.example.data.model.ScannedPage
import com.example.data.model.SectionType
import com.example.data.model.SignatureItem
import com.example.data.model.TextAlignment
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.example.data.repository.OpporrsRepository
import com.example.data.templates.DocumentTemplate
import com.example.data.templates.TemplateCatalog
import com.example.domain.ai.AiAssistState
import com.example.domain.ai.GeminiDocumentAssistant
import com.example.domain.export.DocumentExporter
import com.example.domain.export.ExportResult
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

class OpporrsViewModel(
    private val repository: OpporrsRepository,
    private val currentUserId: String
) : ViewModel() {

    private val aiAssistant = GeminiDocumentAssistant()

    // --- Real-time Firestore Streams ---
    val settingsState: StateFlow<UserSettings> = repository.observeUserSettings(currentUserId)
        .map { it ?: UserSettings(userId = currentUserId) }
        .catch { emit(UserSettings(userId = currentUserId)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserSettings(userId = currentUserId)
        )

    val profileState: StateFlow<UserProfile> = repository.observeUserProfile(currentUserId)
        .map { it ?: UserProfile(userId = currentUserId) }
        .catch { emit(UserProfile(userId = currentUserId)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserProfile(userId = currentUserId)
        )

    val documentsState: StateFlow<DataUiState<List<DocumentItem>>> = repository.observeDocuments(currentUserId)
        .map<List<DocumentItem>, DataUiState<List<DocumentItem>>> { DataUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Unable to sync documents"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    val foldersState: StateFlow<List<FolderItem>> = repository.observeFolders(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val signaturesState: StateFlow<List<SignatureItem>> = repository.observeSignatures(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val exportsState: StateFlow<List<ExportRecord>> = repository.observeExports(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // --- Template Discovery & Search ---
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _templateSearchQuery = MutableStateFlow("")
    val templateSearchQuery: StateFlow<String> = _templateSearchQuery.asStateFlow()

    private val _documentSearchQuery = MutableStateFlow("")
    val documentSearchQuery: StateFlow<String> = _documentSearchQuery.asStateFlow()

    private val _documentFilter = MutableStateFlow("All") // All, Recent, Drafts, Completed, Archived
    val documentFilter: StateFlow<String> = _documentFilter.asStateFlow()

    private val _selectedFolderId = MutableStateFlow("")
    val selectedFolderId: StateFlow<String> = _selectedFolderId.asStateFlow()

    // --- Editor State ---
    private val _activeDocument = MutableStateFlow(createVisualExampleDocument())
    val activeDocument: StateFlow<DocumentItem> = _activeDocument.asStateFlow()

    private val _editorSections = MutableStateFlow(
        DocumentSerializer.decodeSections(_activeDocument.value.sectionsData)
    )
    val editorSections: StateFlow<List<DocumentSection>> = _editorSections.asStateFlow()

    private val _selectedSectionId = MutableStateFlow<String?>(null)
    val selectedSectionId: StateFlow<String?> = _selectedSectionId.asStateFlow()

    private val _isVisualExampleOnly = MutableStateFlow(true)
    val isVisualExampleOnly: StateFlow<Boolean> = _isVisualExampleOnly.asStateFlow()

    private val _saveStatusText = MutableStateFlow("Visual Example • Edit to Save")
    val saveStatusText: StateFlow<String> = _saveStatusText.asStateFlow()

    private val undoStack = ArrayDeque<List<DocumentSection>>()
    private val redoStack = ArrayDeque<List<DocumentSection>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var autosaveJob: Job? = null

    // --- Scanner State ---
    private val _scannedPages = MutableStateFlow<List<ScannedPage>>(emptyList())
    val scannedPages: StateFlow<List<ScannedPage>> = _scannedPages.asStateFlow()

    // --- Export State ---
    private val _lastExportResult = MutableStateFlow<ExportResult?>(null)
    val lastExportResult: StateFlow<ExportResult?> = _lastExportResult.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // --- Gemini AI State ---
    private val _aiState = MutableStateFlow<AiAssistState>(AiAssistState.Idle)
    val aiState: StateFlow<AiAssistState> = _aiState.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun resetAiState() {
        _aiState.value = AiAssistState.Idle
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setTemplateSearchQuery(query: String) {
        _templateSearchQuery.value = query
    }

    fun setDocumentSearchQuery(query: String) {
        _documentSearchQuery.value = query
    }

    fun setDocumentFilter(filter: String) {
        _documentFilter.value = filter
    }

    fun setSelectedFolderId(folderId: String) {
        _selectedFolderId.value = folderId
    }

    fun selectSection(sectionId: String?) {
        _selectedSectionId.value = sectionId
    }

    /**
     * Builds the exact visual editing example from Screen 04 specification.
     * Note: Prompt specifies "The displayed content is a visual editing example only.
     * It must not be automatically saved as actual user information."
     */
    private fun createVisualExampleDocument(): DocumentItem {
        val exampleSections = listOf(
            DocumentSection(
                type = SectionType.HEADING,
                title = "PROFESSIONAL RESUME",
                content = "Rahul Shah",
                fontSizeSp = 18,
                isBold = true
            ),
            DocumentSection(
                type = SectionType.PARAGRAPH,
                title = "Professional Summary",
                content = "A clean, editable professional introduction.",
                fontSizeSp = 12
            ),
            DocumentSection(
                type = SectionType.PARAGRAPH,
                title = "WORK EXPERIENCE",
                content = "Job title\nOrganization\nEmployment dates",
                fontSizeSp = 12
            ),
            DocumentSection(
                type = SectionType.PARAGRAPH,
                title = "EDUCATION",
                content = "Institution\nQualification",
                fontSizeSp = 12
            ),
            DocumentSection(
                type = SectionType.PARAGRAPH,
                title = "SKILLS",
                content = "Communication | Organization | Technical Skills",
                fontSizeSp = 12,
                isBold = true
            )
        )
        return DocumentItem(
            id = "",
            userId = currentUserId,
            title = "Professional Resume",
            category = "Career",
            templateId = "career_01",
            status = "draft",
            sectionsData = DocumentSerializer.encodeSections(exampleSections),
            wordCount = DocumentSerializer.calculateWordCount(exampleSections),
            pageCount = 1
        )
    }

    fun openDocumentForEditing(doc: DocumentItem) {
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _isVisualExampleOnly.value = false
        _activeDocument.value = doc
        val decoded = DocumentSerializer.decodeSections(doc.sectionsData)
        _editorSections.value = if (decoded.isNotEmpty()) {
            decoded
        } else {
            listOf(
                DocumentSection(
                    type = SectionType.HEADING,
                    title = doc.title,
                    content = "Start writing your document here...",
                    fontSizeSp = 16,
                    isBold = true
                )
            )
        }
        _selectedSectionId.value = _editorSections.value.firstOrNull()?.id
        _saveStatusText.value = "Saved in Cloud"
    }

    fun startFromTemplate(template: DocumentTemplate, onReady: () -> Unit = {}) {
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _isVisualExampleOnly.value = false
        val sections = template.buildSections()
        val encoded = DocumentSerializer.encodeSections(sections)
        val draftDoc = DocumentItem(
            id = "",
            userId = currentUserId,
            title = template.name,
            category = template.category,
            templateId = template.id,
            status = "draft",
            pageFormat = template.defaultPageFormat,
            orientation = template.defaultOrientation,
            accentHex = template.accentHex,
            fontFamily = template.fontFamily,
            sectionsData = encoded,
            wordCount = DocumentSerializer.calculateWordCount(sections),
            pageCount = DocumentSerializer.calculatePageCount(sections, 0)
        )
        _activeDocument.value = draftDoc
        _editorSections.value = sections
        _selectedSectionId.value = sections.firstOrNull()?.id
        _saveStatusText.value = "Saving..."
        viewModelScope.launch {
            val res = repository.createDocument(draftDoc)
            res.onSuccess { newId ->
                _activeDocument.value = draftDoc.copy(id = newId)
                _saveStatusText.value = "Saved"
            }.onFailure { err ->
                _saveStatusText.value = "Offline / Pending"
                _statusMessage.value = err.localizedMessage ?: "Failed to save template document"
            }
        }
        onReady()
    }

    fun startBlankDocument(category: String = "Personal", title: String = "Untitled Document", onReady: () -> Unit = {}) {
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _isVisualExampleOnly.value = false
        val sections = listOf(
            DocumentSection(
                type = SectionType.HEADING,
                title = title.uppercase(),
                content = "Subtitle or date",
                fontSizeSp = 18,
                isBold = true
            ),
            DocumentSection(
                type = SectionType.PARAGRAPH,
                title = "Section 1",
                content = "Write your content here...",
                fontSizeSp = 12
            )
        )
        val draftDoc = DocumentItem(
            id = "",
            userId = currentUserId,
            title = title,
            category = category,
            status = "draft",
            sectionsData = DocumentSerializer.encodeSections(sections),
            wordCount = DocumentSerializer.calculateWordCount(sections),
            pageCount = 1
        )
        _activeDocument.value = draftDoc
        _editorSections.value = sections
        _selectedSectionId.value = sections.firstOrNull()?.id
        _saveStatusText.value = "Saving..."
        viewModelScope.launch {
            val res = repository.createDocument(draftDoc)
            res.onSuccess { newId ->
                _activeDocument.value = draftDoc.copy(id = newId)
                _saveStatusText.value = "Saved"
            }
        }
        onReady()
    }

    // --- Editor Mutations & Undo/Redo + Autosave ---
    private fun pushUndoSnapshot() {
        undoStack.addLast(_editorSections.value)
        if (undoStack.size > 30) undoStack.removeFirst()
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.addLast(_editorSections.value)
            val prev = undoStack.removeLast()
            _editorSections.value = prev
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = redoStack.isNotEmpty()
            scheduleAutosave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.addLast(_editorSections.value)
            val next = redoStack.removeLast()
            _editorSections.value = next
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = redoStack.isNotEmpty()
            scheduleAutosave()
        }
    }

    fun updateDocumentTitle(newTitle: String) {
        _isVisualExampleOnly.value = false
        _activeDocument.value = _activeDocument.value.copy(title = newTitle)
        scheduleAutosave()
    }

    fun updateDocumentPageSettings(
        pageFormat: String = _activeDocument.value.pageFormat,
        orientation: String = _activeDocument.value.orientation,
        marginMm: Int = _activeDocument.value.marginMm,
        accentHex: String = _activeDocument.value.accentHex,
        fontFamily: String = _activeDocument.value.fontFamily,
        status: String = _activeDocument.value.status
    ) {
        _isVisualExampleOnly.value = false
        _activeDocument.value = _activeDocument.value.copy(
            pageFormat = pageFormat,
            orientation = orientation,
            marginMm = marginMm,
            accentHex = accentHex,
            fontFamily = fontFamily,
            status = status
        )
        scheduleAutosave()
    }

    fun updateSection(sectionId: String, transform: (DocumentSection) -> DocumentSection) {
        pushUndoSnapshot()
        _isVisualExampleOnly.value = false
        _editorSections.value = _editorSections.value.map {
            if (it.id == sectionId) transform(it) else it
        }
        scheduleAutosave()
    }

    fun addSection(type: SectionType) {
        pushUndoSnapshot()
        _isVisualExampleOnly.value = false
        val newSec = when (type) {
            SectionType.HEADING -> DocumentSection(
                type = SectionType.HEADING,
                title = "NEW SECTION HEADING",
                content = "Section subtitle or summary",
                fontSizeSp = 16,
                isBold = true
            )
            SectionType.PARAGRAPH -> DocumentSection(
                type = SectionType.PARAGRAPH,
                title = "Paragraph Heading",
                content = "Enter paragraph text here...",
                fontSizeSp = 12
            )
            SectionType.BULLET_LIST -> DocumentSection(
                type = SectionType.BULLET_LIST,
                title = "Key Points",
                content = "First bullet item\nSecond bullet item\nThird bullet item",
                fontSizeSp = 12
            )
            SectionType.NUMBERED_LIST -> DocumentSection(
                type = SectionType.NUMBERED_LIST,
                title = "Action Steps",
                content = "Step one description\nStep two description\nStep three description",
                fontSizeSp = 12
            )
            SectionType.TABLE -> DocumentSection(
                type = SectionType.TABLE,
                title = "Data Table",
                content = "Structured table",
                tableRows = listOf(
                    listOf("Column 1", "Column 2", "Column 3"),
                    listOf("Row 1A", "Row 1B", "Row 1C"),
                    listOf("Row 2A", "Row 2B", "Row 2C")
                )
            )
            SectionType.IMAGE -> DocumentSection(
                type = SectionType.IMAGE,
                title = "Figure / Attachment",
                content = "Attachment caption"
            )
            SectionType.SIGNATURE -> DocumentSection(
                type = SectionType.SIGNATURE,
                title = "Authorized Signature",
                content = profileState.value.fullName.ifBlank { "Signatory Name" }
            )
            SectionType.PAGE_BREAK -> DocumentSection(
                type = SectionType.PAGE_BREAK,
                title = "Page Break"
            )
        }
        _editorSections.value = _editorSections.value + newSec
        _selectedSectionId.value = newSec.id
        scheduleAutosave()
    }

    fun removeSection(sectionId: String) {
        if (_editorSections.value.size <= 1) {
            _statusMessage.value = "A document must have at least one section."
            return
        }
        pushUndoSnapshot()
        _isVisualExampleOnly.value = false
        _editorSections.value = _editorSections.value.filterNot { it.id == sectionId }
        if (_selectedSectionId.value == sectionId) {
            _selectedSectionId.value = _editorSections.value.firstOrNull()?.id
        }
        scheduleAutosave()
    }

    fun moveSection(sectionId: String, direction: Int) {
        val current = _editorSections.value.toMutableList()
        val idx = current.indexOfFirst { it.id == sectionId }
        if (idx == -1) return
        val target = idx + direction
        if (target !in current.indices) return
        pushUndoSnapshot()
        _isVisualExampleOnly.value = false
        val item = current.removeAt(idx)
        current.add(target, item)
        _editorSections.value = current
        scheduleAutosave()
    }

    fun applyFormattingToSelected(
        toggleBold: Boolean = false,
        toggleItalic: Boolean = false,
        toggleUnderline: Boolean = false,
        newAlignment: TextAlignment? = null,
        fontSizeDelta: Int = 0,
        convertToType: SectionType? = null
    ) {
        val targetId = _selectedSectionId.value ?: _editorSections.value.firstOrNull()?.id ?: return
        updateSection(targetId) { sec ->
            sec.copy(
                isBold = if (toggleBold) !sec.isBold else sec.isBold,
                isItalic = if (toggleItalic) !sec.isItalic else sec.isItalic,
                isUnderline = if (toggleUnderline) !sec.isUnderline else sec.isUnderline,
                alignment = newAlignment ?: sec.alignment,
                fontSizeSp = (sec.fontSizeSp + fontSizeDelta).coerceIn(9, 24),
                type = convertToType ?: sec.type
            )
        }
    }

    /**
     * Explicitly inserts saved user profile fields into the active document only when approved by the user.
     */
    fun insertUserProfileIntoDocument() {
        val p = profileState.value
        if (p.fullName.isBlank() && p.email.isBlank() && p.summary.isBlank()) {
            _statusMessage.value = "Complete your Personal Profile first to insert profile details."
            return
        }
        pushUndoSnapshot()
        _isVisualExampleOnly.value = false
        val headerLines = listOfNotNull(
            p.fullName.takeIf { it.isNotBlank() },
            p.jobTitle.takeIf { it.isNotBlank() },
            listOfNotNull(
                p.email.takeIf { it.isNotBlank() },
                p.phone.takeIf { it.isNotBlank() },
                p.location.takeIf { it.isNotBlank() }
            ).joinToString(" • ").takeIf { it.isNotBlank() }
        ).joinToString("\n")

        val updated = mutableListOf<DocumentSection>()
        updated.add(
            DocumentSection(
                type = SectionType.HEADING,
                title = _activeDocument.value.title.uppercase(),
                content = headerLines,
                fontSizeSp = 18,
                isBold = true
            )
        )
        if (p.summary.isNotBlank()) {
            updated.add(
                DocumentSection(
                    type = SectionType.PARAGRAPH,
                    title = "Professional Summary",
                    content = p.summary
                )
            )
        }
        if (p.experienceText.isNotBlank()) {
            updated.add(
                DocumentSection(
                    type = SectionType.BULLET_LIST,
                    title = "Work Experience",
                    content = p.experienceText
                )
            )
        }
        if (p.educationText.isNotBlank()) {
            updated.add(
                DocumentSection(
                    type = SectionType.PARAGRAPH,
                    title = "Education",
                    content = p.educationText
                )
            )
        }
        if (p.skillsText.isNotBlank()) {
            updated.add(
                DocumentSection(
                    type = SectionType.PARAGRAPH,
                    title = "Skills",
                    content = p.skillsText,
                    isBold = true
                )
            )
        }
        if (p.referencesText.isNotBlank()) {
            updated.add(
                DocumentSection(
                    type = SectionType.PARAGRAPH,
                    title = "References",
                    content = p.referencesText
                )
            )
        }
        _editorSections.value = updated
        if (p.defaultSignatureSvg.isNotBlank()) {
            _activeDocument.value = _activeDocument.value.copy(signatureSvg = p.defaultSignatureSvg)
        }
        scheduleAutosave()
        _statusMessage.value = "Personal profile inserted into document."
    }

    fun attachSignatureToDocument(svgPathData: String, signerName: String) {
        pushUndoSnapshot()
        _isVisualExampleOnly.value = false
        _activeDocument.value = _activeDocument.value.copy(signatureSvg = svgPathData)
        val hasSigSection = _editorSections.value.any { it.type == SectionType.SIGNATURE }
        if (!hasSigSection) {
            _editorSections.value = _editorSections.value + DocumentSection(
                type = SectionType.SIGNATURE,
                title = "Authorized Signature",
                content = signerName.ifBlank { profileState.value.fullName.ifBlank { "Authorized Signatory" } }
            )
        }
        scheduleAutosave()
        _statusMessage.value = "Signature added to document."
    }

    private fun scheduleAutosave() {
        if (_isVisualExampleOnly.value) return
        _saveStatusText.value = "Saving..."
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(650L)
            saveActiveDocumentNow()
        }
    }

    fun saveActiveDocumentNow() {
        _isVisualExampleOnly.value = false
        val sections = _editorSections.value
        val encoded = DocumentSerializer.encodeSections(sections)
        val words = DocumentSerializer.calculateWordCount(sections)
        val pages = DocumentSerializer.calculatePageCount(sections, _scannedPages.value.size)
        val current = _activeDocument.value.copy(
            sectionsData = encoded,
            wordCount = words,
            pageCount = pages
        )
        _activeDocument.value = current
        viewModelScope.launch {
            _saveStatusText.value = "Saving..."
            if (current.id.isBlank()) {
                repository.createDocument(current).onSuccess { newId ->
                    _activeDocument.value = current.copy(id = newId)
                    _saveStatusText.value = "Saved"
                }.onFailure {
                    _saveStatusText.value = "Save Retry Needed"
                }
            } else {
                repository.updateDocument(current).onSuccess {
                    _saveStatusText.value = "Saved"
                }.onFailure {
                    _saveStatusText.value = "Save Retry Needed"
                }
            }
        }
    }

    // --- Document Organization Actions (Screen 05) ---
    fun renameDocument(doc: DocumentItem, newTitle: String) {
        viewModelScope.launch {
            repository.updateDocument(doc.copy(title = newTitle.ifBlank { "Untitled" })).onSuccess {
                _statusMessage.value = "Renamed to \"$newTitle\""
            }
        }
    }

    fun duplicateDocument(doc: DocumentItem) {
        viewModelScope.launch {
            val copyDoc = doc.copy(
                id = "",
                title = "${doc.title} (Copy)",
                status = "draft"
            )
            repository.createDocument(copyDoc).onSuccess {
                _statusMessage.value = "Duplicated \"${doc.title}\""
            }
        }
    }

    fun moveDocumentToFolder(doc: DocumentItem, folderId: String) {
        viewModelScope.launch {
            repository.updateDocument(doc.copy(folderId = folderId)).onSuccess {
                _statusMessage.value = "Moved document to folder"
            }
        }
    }

    fun toggleArchiveDocument(doc: DocumentItem) {
        val nextStatus = if (doc.status == "archived") "draft" else "archived"
        viewModelScope.launch {
            repository.updateDocument(doc.copy(status = nextStatus)).onSuccess {
                _statusMessage.value = if (nextStatus == "archived") "Document archived" else "Restored from archive"
            }
        }
    }

    fun markDocumentCompleted(doc: DocumentItem) {
        val nextStatus = if (doc.status == "completed") "draft" else "completed"
        viewModelScope.launch {
            repository.updateDocument(doc.copy(status = nextStatus)).onSuccess {
                _statusMessage.value = "Document marked as $nextStatus"
            }
        }
    }

    fun toggleFavoriteDocument(doc: DocumentItem) {
        viewModelScope.launch {
            repository.updateDocument(doc.copy(isFavorite = !doc.isFavorite))
        }
    }

    fun deleteDocument(docId: String) {
        viewModelScope.launch {
            repository.deleteDocument(docId).onSuccess {
                _statusMessage.value = "Document deleted"
            }
        }
    }

    // --- Folder, Profile, Signature & Settings Management ---
    fun createFolder(name: String, description: String, colorHex: String) {
        viewModelScope.launch {
            repository.createFolder(name, description, colorHex).onSuccess {
                _statusMessage.value = "Folder \"$name\" created"
            }
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            repository.deleteFolder(folderId).onSuccess {
                if (_selectedFolderId.value == folderId) _selectedFolderId.value = ""
                _statusMessage.value = "Folder removed"
            }
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile).onSuccess {
                _statusMessage.value = "Personal profile saved"
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Failed to save profile"
            }
        }
    }

    fun saveSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.saveUserSettings(settings).onSuccess {
                _statusMessage.value = "Preferences updated"
            }
        }
    }

    fun saveSignature(label: String, svgPathData: String, strokeColorHex: String) {
        viewModelScope.launch {
            repository.createSignature(label, svgPathData, strokeColorHex).onSuccess {
                _statusMessage.value = "Signature \"$label\" saved"
            }
        }
    }

    fun deleteSignature(sigId: String) {
        viewModelScope.launch {
            repository.deleteSignature(sigId).onSuccess {
                _statusMessage.value = "Signature deleted"
            }
        }
    }

    // --- Scanner Management ---
    fun addScannedPage(page: ScannedPage) {
        _scannedPages.value = _scannedPages.value + page
    }

    fun updateScannedPage(pageId: String, transform: (ScannedPage) -> ScannedPage) {
        _scannedPages.value = _scannedPages.value.map {
            if (it.id == pageId) transform(it) else it
        }
    }

    fun removeScannedPage(pageId: String) {
        _scannedPages.value = _scannedPages.value.filterNot { it.id == pageId }
    }

    fun moveScannedPage(pageId: String, direction: Int) {
        val list = _scannedPages.value.toMutableList()
        val index = list.indexOfFirst { it.id == pageId }
        if (index == -1) return
        val target = index + direction
        if (target !in list.indices) return
        val item = list.removeAt(index)
        list.add(target, item)
        _scannedPages.value = list
    }

    fun saveScannedPagesAsDocument(title: String, onSaved: () -> Unit = {}) {
        val pages = _scannedPages.value
        if (pages.isEmpty()) {
            _statusMessage.value = "Capture or import at least one page first."
            return
        }
        val sections = pages.mapIndexed { index, page ->
            DocumentSection(
                type = if (page.extractedText.isNotBlank()) SectionType.PARAGRAPH else SectionType.IMAGE,
                title = "Scanned Page ${index + 1}",
                content = page.extractedText.ifBlank { "High-resolution scanned document page (${page.filterMode.name})" },
                imageUri = page.imageUri
            )
        }
        val newDoc = DocumentItem(
            userId = currentUserId,
            title = title.ifBlank { "Scanned Document ${System.currentTimeMillis() % 10000}" },
            category = "Personal",
            status = "completed",
            sectionsData = DocumentSerializer.encodeSections(sections),
            scannedPagesData = DocumentSerializer.encodeScannedPages(pages),
            wordCount = DocumentSerializer.calculateWordCount(sections),
            pageCount = pages.size
        )
        viewModelScope.launch {
            repository.createDocument(newDoc).onSuccess { newId ->
                _activeDocument.value = newDoc.copy(id = newId)
                _editorSections.value = sections
                _isVisualExampleOnly.value = false
                _statusMessage.value = "Saved ${pages.size}-page scan to Documents"
                onSaved()
            }
        }
    }

    // --- Export Workflow (PDF, TXT, CSV) ---
    fun runExport(
        context: Context,
        format: String,
        targetDoc: DocumentItem = _activeDocument.value,
        onExportReady: (ExportResult) -> Unit = {}
    ) {
        val sections = if (targetDoc.id == _activeDocument.value.id) {
            _editorSections.value
        } else {
            DocumentSerializer.decodeSections(targetDoc.sectionsData)
        }
        viewModelScope.launch {
            val result = when (format.uppercase()) {
                "TXT" -> DocumentExporter.exportToTxt(context, targetDoc, sections)
                "CSV" -> DocumentExporter.exportToCsv(context, targetDoc, sections)
                else -> DocumentExporter.exportToPdf(context, targetDoc, sections)
            }
            result.onSuccess { exp ->
                _lastExportResult.value = exp
                repository.logExport(
                    ExportRecord(
                        userId = currentUserId,
                        documentId = targetDoc.id,
                        documentTitle = targetDoc.title,
                        format = exp.format,
                        pageSize = "${targetDoc.pageFormat} ${targetDoc.orientation}",
                        fileSizeBytes = exp.fileSizeBytes,
                        fileUri = exp.file.absolutePath,
                        status = "completed"
                    )
                )
                _statusMessage.value = "${exp.format} generated (${exp.fileSizeBytes / 1024 + 1} KB)"
                onExportReady(exp)
            }.onFailure { err ->
                _statusMessage.value = "Export failed: ${err.localizedMessage}"
            }
        }
    }

    fun shareDocumentSummary(context: Context, doc: DocumentItem) {
        val sections = DocumentSerializer.decodeSections(doc.sectionsData)
        val body = buildString {
            appendLine(doc.title.uppercase())
            appendLine("Category: ${doc.category} • Format: ${doc.pageFormat} ${doc.orientation}")
            appendLine()
            sections.forEach { sec ->
                if (sec.title.isNotBlank()) appendLine(sec.title)
                if (sec.content.isNotBlank()) appendLine(sec.content)
                appendLine()
            }
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, doc.title)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        val chooser = Intent.createChooser(sendIntent, "Share ${doc.title}")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    // --- Gemini AI Intelligence Workflows ---
    fun polishSelectedSectionWithAi(instruction: String, useFastModel: Boolean = false) {
        val targetId = _selectedSectionId.value ?: _editorSections.value.firstOrNull()?.id ?: return
        val section = _editorSections.value.find { it.id == targetId } ?: return
        viewModelScope.launch {
            aiAssistant.streamSectionRefinement(
                sectionTitle = section.title,
                currentContent = section.content,
                instruction = instruction,
                useFastModel = useFastModel
            ).collect { state ->
                _aiState.value = state
                if (state is AiAssistState.Success) {
                    updateSection(targetId) { it.copy(content = state.resultText) }
                }
            }
        }
    }

    fun generateFullDocumentWithAi(prompt: String, category: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _aiState.value = AiAssistState.Loading
            val state = aiAssistant.generateStructuredDocument(prompt, category)
            _aiState.value = state
            if (state is AiAssistState.Success && state.generatedSections.isNotEmpty()) {
                undoStack.clear()
                redoStack.clear()
                _isVisualExampleOnly.value = false
                val newDoc = DocumentItem(
                    userId = currentUserId,
                    title = state.resultText,
                    category = category,
                    status = "draft",
                    sectionsData = DocumentSerializer.encodeSections(state.generatedSections),
                    wordCount = DocumentSerializer.calculateWordCount(state.generatedSections),
                    pageCount = DocumentSerializer.calculatePageCount(state.generatedSections, 0)
                )
                _activeDocument.value = newDoc
                _editorSections.value = state.generatedSections
                _selectedSectionId.value = state.generatedSections.firstOrNull()?.id
                saveActiveDocumentNow()
                onComplete()
            }
        }
    }

    fun runExecutiveAuditWithAi() {
        viewModelScope.launch {
            _aiState.value = AiAssistState.Loading
            _aiState.value = aiAssistant.performExecutiveAudit(
                documentTitle = _activeDocument.value.title,
                sections = _editorSections.value
            )
        }
    }

    fun extractOcrFromScannedBitmap(pageId: String, bitmap: Bitmap) {
        viewModelScope.launch {
            _aiState.value = AiAssistState.Loading
            val state = aiAssistant.extractTextFromScannedBitmap(bitmap)
            _aiState.value = state
            if (state is AiAssistState.Success) {
                updateScannedPage(pageId) { it.copy(extractedText = state.resultText) }
                _statusMessage.value = "Extracted text from scanned page via Gemini Vision"
            }
        }
    }
}

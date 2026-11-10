package com.example

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.configureAppCheck
import com.example.data.firebase.signOutUser
import com.example.data.repository.OpporrsRepository
import com.example.data.templates.TemplateCatalog
import com.example.domain.ai.AiAssistState
import com.example.ui.screens.AuthWelcomeScreen
import com.example.ui.screens.DocumentEditorScreen
import com.example.ui.screens.DocumentScannerScreen
import com.example.ui.screens.DocumentsAndProfileScreen
import com.example.ui.screens.ExportDocumentScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.ProfileAndSettingsScreen
import com.example.ui.screens.SignatureManagerScreen
import com.example.ui.screens.TemplateDiscoveryScreen
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.OpporrsTheme
import com.example.ui.viewmodel.DataUiState
import com.example.ui.viewmodel.OpporrsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

enum class OpporrsScreen {
    HOME,
    TEMPLATES,
    DOCUMENTS,
    PROFILE,
    EDITOR,
    SCANNER,
    EXPORT,
    SIGNATURES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            OpporrsAppRoot()
        }
    }
}

@Composable
fun OpporrsAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        OpporrsTheme {
            AuthWelcomeScreen(
                onAuthSuccess = { /* AuthStateListener updates currentUser automatically */ }
            )
        }
    } else {
        val uid = user.uid
        key(uid) {
            AuthenticatedWorkspace(
                currentUserId = uid,
                userDisplayName = user.displayName.orEmpty(),
                userEmail = user.email.orEmpty()
            )
        }
    }
}

@Composable
private fun AuthenticatedWorkspace(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: OpporrsViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                OpporrsViewModel(OpporrsRepository(db), currentUserId)
            }
        }
    )

    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val documentsState by viewModel.documentsState.collectAsStateWithLifecycle()
    val folders by viewModel.foldersState.collectAsStateWithLifecycle()
    val signatures by viewModel.signaturesState.collectAsStateWithLifecycle()
    val exports by viewModel.exportsState.collectAsStateWithLifecycle()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val templateSearchQuery by viewModel.templateSearchQuery.collectAsStateWithLifecycle()
    val documentSearchQuery by viewModel.documentSearchQuery.collectAsStateWithLifecycle()
    val documentFilter by viewModel.documentFilter.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()

    val activeDocument by viewModel.activeDocument.collectAsStateWithLifecycle()
    val editorSections by viewModel.editorSections.collectAsStateWithLifecycle()
    val selectedSectionId by viewModel.selectedSectionId.collectAsStateWithLifecycle()
    val saveStatusText by viewModel.saveStatusText.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val scannedPages by viewModel.scannedPages.collectAsStateWithLifecycle()
    val lastExportResult by viewModel.lastExportResult.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()

    var currentScreen by rememberSaveable { mutableStateOf(OpporrsScreen.HOME) }
    var isAppUnlocked by rememberSaveable { mutableStateOf(false) }
    var showAiGeneratorDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        val msg = statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // System Document Picker for importing .txt, .csv, or .pdf files
    val fileImporterLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val contentText = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull().orEmpty()
            val importedTitle = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Document"
            viewModel.startBlankDocument(category = "Business", title = importedTitle) {
                if (contentText.isNotBlank()) {
                    val firstId = viewModel.editorSections.value.lastOrNull()?.id
                    if (firstId != null) {
                        viewModel.updateSection(firstId) {
                            it.copy(title = "Imported Content", content = contentText.take(10000))
                        }
                    }
                }
                currentScreen = OpporrsScreen.EDITOR
            }
        }
    }

    OpporrsTheme(
        themeMode = settings.themeMode,
        accentHex = settings.accentHex,
        fontFamilyName = settings.fontFamily,
        fontScalePreset = settings.fontScale,
        compactMode = settings.compactMode,
        languageCode = settings.languageCode
    ) {
        val strings = LocalAppStrings.current

        // Optional Biometric / Face Unlock / System Credential & PIN App Lock Gate
        if (settings.appLockEnabled && !isAppUnlocked) {
            AppLockScreen(
                expectedPin = settings.appLockPin,
                onUnlocked = { isAppUnlocked = true }
            )
            return@OpporrsTheme
        }

        val showBottomBar = currentScreen in listOf(
            OpporrsScreen.HOME,
            OpporrsScreen.TEMPLATES,
            OpporrsScreen.DOCUMENTS,
            OpporrsScreen.PROFILE
        )

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (currentScreen == OpporrsScreen.HOME || currentScreen == OpporrsScreen.DOCUMENTS) {
                    ExtendedFloatingActionButton(
                        onClick = { currentScreen = OpporrsScreen.EDITOR },
                        icon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
                        text = { Text("Editor") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("open_editor_fab")
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == OpporrsScreen.HOME,
                            onClick = { currentScreen = OpporrsScreen.HOME },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == OpporrsScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = strings.navHome
                                )
                            },
                            label = { Text(strings.navHome) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavigationBarItem(
                            selected = currentScreen == OpporrsScreen.TEMPLATES,
                            onClick = { currentScreen = OpporrsScreen.TEMPLATES },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == OpporrsScreen.TEMPLATES) Icons.Filled.GridView else Icons.Outlined.GridView,
                                    contentDescription = strings.navTemplates
                                )
                            },
                            label = { Text(strings.navTemplates) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_templates")
                        )
                        NavigationBarItem(
                            selected = currentScreen == OpporrsScreen.DOCUMENTS,
                            onClick = { currentScreen = OpporrsScreen.DOCUMENTS },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == OpporrsScreen.DOCUMENTS) Icons.Filled.Description else Icons.Outlined.Description,
                                    contentDescription = strings.navDocuments
                                )
                            },
                            label = { Text(strings.navDocuments) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_documents")
                        )
                        NavigationBarItem(
                            selected = currentScreen == OpporrsScreen.PROFILE,
                            onClick = { currentScreen = OpporrsScreen.PROFILE },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == OpporrsScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = strings.navProfile
                                )
                            },
                            label = { Text(strings.navProfile) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_profile")
                        )
                    }
                }
            }
        ) { innerPadding ->
            val recentDocs = (documentsState as? DataUiState.Success)?.data.orEmpty()

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    OpporrsScreen.HOME -> {
                        HomeDashboardScreen(
                            userProfile = profile,
                            userDisplayName = userDisplayName,
                            recentDocuments = recentDocs,
                            compactMode = settings.compactMode,
                            onSearchSubmitted = { query ->
                                viewModel.setTemplateSearchQuery(query)
                                currentScreen = OpporrsScreen.TEMPLATES
                            },
                            onCreateDocument = {
                                viewModel.startBlankDocument(
                                    category = "Personal",
                                    title = "Untitled Document"
                                ) {
                                    currentScreen = OpporrsScreen.EDITOR
                                }
                            },
                            onScanDocument = {
                                currentScreen = OpporrsScreen.SCANNER
                            },
                            onCreateCv = {
                                val cvTemplate = TemplateCatalog.allTemplates.first()
                                viewModel.startFromTemplate(cvTemplate) {
                                    currentScreen = OpporrsScreen.EDITOR
                                }
                            },
                            onImportFile = {
                                fileImporterLauncher.launch(arrayOf("text/*", "application/pdf", "*/*"))
                            },
                            onOpenAiGenerator = {
                                showAiGeneratorDialog = true
                            },
                            onSelectTemplate = { tmpl ->
                                viewModel.startFromTemplate(tmpl) {
                                    currentScreen = OpporrsScreen.EDITOR
                                }
                            },
                            onExploreAllTemplates = {
                                currentScreen = OpporrsScreen.TEMPLATES
                            },
                            onOpenDocument = { doc ->
                                viewModel.openDocumentForEditing(doc)
                                currentScreen = OpporrsScreen.EDITOR
                            },
                            onDuplicateDocument = { doc ->
                                viewModel.duplicateDocument(doc)
                            },
                            onExportDocument = { doc ->
                                viewModel.openDocumentForEditing(doc)
                                currentScreen = OpporrsScreen.EXPORT
                            },
                            onDeleteDocument = { doc ->
                                viewModel.deleteDocument(doc.id)
                            },
                            onOpenNotifications = {
                                showNotificationsDialog = true
                            },
                            onOpenProfile = {
                                currentScreen = OpporrsScreen.PROFILE
                            }
                        )
                    }

                    OpporrsScreen.TEMPLATES -> {
                        TemplateDiscoveryScreen(
                            selectedCategory = selectedCategory,
                            searchQuery = templateSearchQuery,
                            onCategorySelected = viewModel::setSelectedCategory,
                            onSearchQueryChanged = viewModel::setTemplateSearchQuery,
                            onUseTemplate = { tmpl ->
                                viewModel.startFromTemplate(tmpl) {
                                    currentScreen = OpporrsScreen.EDITOR
                                }
                            }
                        )
                    }

                    OpporrsScreen.DOCUMENTS -> {
                        DocumentsAndProfileScreen(
                            documentsState = documentsState,
                            folders = folders,
                            selectedFolderId = selectedFolderId,
                            selectedFilter = documentFilter,
                            searchQuery = documentSearchQuery,
                            userProfile = profile,
                            userEmail = userEmail,
                            onSearchQueryChanged = viewModel::setDocumentSearchQuery,
                            onFilterChanged = viewModel::setDocumentFilter,
                            onSelectFolder = viewModel::setSelectedFolderId,
                            onCreateDocument = {
                                viewModel.startBlankDocument {
                                    currentScreen = OpporrsScreen.EDITOR
                                }
                            },
                            onCreateFolder = viewModel::createFolder,
                            onDeleteFolder = viewModel::deleteFolder,
                            onOpenDocument = { doc ->
                                viewModel.openDocumentForEditing(doc)
                                currentScreen = OpporrsScreen.EDITOR
                            },
                            onRenameDocument = viewModel::renameDocument,
                            onDuplicateDocument = viewModel::duplicateDocument,
                            onMoveDocument = viewModel::moveDocumentToFolder,
                            onShareDocument = { doc ->
                                viewModel.shareDocumentSummary(context, doc)
                            },
                            onExportDocument = { doc ->
                                viewModel.openDocumentForEditing(doc)
                                currentScreen = OpporrsScreen.EXPORT
                            },
                            onToggleArchiveDocument = viewModel::toggleArchiveDocument,
                            onMarkCompletedDocument = viewModel::markDocumentCompleted,
                            onToggleFavoriteDocument = viewModel::toggleFavoriteDocument,
                            onDeleteDocument = viewModel::deleteDocument,
                            onOpenProfileEditor = {
                                currentScreen = OpporrsScreen.PROFILE
                            }
                        )
                    }

                    OpporrsScreen.PROFILE -> {
                        ProfileAndSettingsScreen(
                            userProfile = profile,
                            userSettings = settings,
                            savedSignatures = signatures,
                            exportHistory = exports,
                            authEmail = userEmail,
                            onSaveProfile = viewModel::saveProfile,
                            onSaveSettings = viewModel::saveSettings,
                            onOpenSignatureManager = {
                                currentScreen = OpporrsScreen.SIGNATURES
                            },
                            onSignOut = {
                                signOutUser(
                                    credentialManager = credentialManager,
                                    scope = coroutineScope
                                )
                            }
                        )
                    }

                    OpporrsScreen.EDITOR -> {
                        DocumentEditorScreen(
                            document = activeDocument,
                            sections = editorSections,
                            selectedSectionId = selectedSectionId,
                            saveStatusText = saveStatusText,
                            canUndo = canUndo,
                            canRedo = canRedo,
                            aiState = aiState,
                            onBack = { currentScreen = OpporrsScreen.HOME },
                            onTitleChanged = viewModel::updateDocumentTitle,
                            onSelectSection = viewModel::selectSection,
                            onUpdateSection = viewModel::updateSection,
                            onAddSection = viewModel::addSection,
                            onRemoveSection = viewModel::removeSection,
                            onMoveSection = viewModel::moveSection,
                            onFormatSelected = viewModel::applyFormattingToSelected,
                            onUndo = viewModel::undo,
                            onRedo = viewModel::redo,
                            onSaveNow = viewModel::saveActiveDocumentNow,
                            onRequestInsertProfile = viewModel::insertUserProfileIntoDocument,
                            onOpenSignaturePad = { currentScreen = OpporrsScreen.SIGNATURES },
                            onOpenExportScreen = { currentScreen = OpporrsScreen.EXPORT },
                            onPolishWithAi = viewModel::polishSelectedSectionWithAi,
                            onExecutiveAuditWithAi = viewModel::runExecutiveAuditWithAi,
                            onDismissAiState = viewModel::resetAiState,
                            onUpdateDocumentStyle = { accent, font ->
                                viewModel.updateDocumentPageSettings(
                                    accentHex = accent,
                                    fontFamily = font
                                )
                            }
                        )
                    }

                    OpporrsScreen.SCANNER -> {
                        DocumentScannerScreen(
                            scannedPages = scannedPages,
                            onAddPage = viewModel::addScannedPage,
                            onUpdatePage = viewModel::updateScannedPage,
                            onRemovePage = viewModel::removeScannedPage,
                            onMovePage = viewModel::moveScannedPage,
                            onRunGeminiOcr = viewModel::extractOcrFromScannedBitmap,
                            onSaveScanToDocuments = { title ->
                                viewModel.saveScannedPagesAsDocument(title) {
                                    currentScreen = OpporrsScreen.DOCUMENTS
                                }
                            },
                            onBack = { currentScreen = OpporrsScreen.HOME }
                        )
                    }

                    OpporrsScreen.EXPORT -> {
                        ExportDocumentScreen(
                            document = activeDocument,
                            lastExportResult = lastExportResult,
                            onUpdatePageSettings = { fmt, ori, margin ->
                                viewModel.updateDocumentPageSettings(
                                    pageFormat = fmt,
                                    orientation = ori,
                                    marginMm = margin
                                )
                            },
                            onTriggerExport = { format, onReady ->
                                viewModel.runExport(context, format, activeDocument, onReady)
                            },
                            onGoToMyDocuments = {
                                currentScreen = OpporrsScreen.DOCUMENTS
                            },
                            onBack = { currentScreen = OpporrsScreen.EDITOR }
                        )
                    }

                    OpporrsScreen.SIGNATURES -> {
                        SignatureManagerScreen(
                            savedSignatures = signatures,
                            onSaveNewSignature = viewModel::saveSignature,
                            onSelectSignatureForDocument = viewModel::attachSignatureToDocument,
                            onDeleteSignature = viewModel::deleteSignature,
                            onBack = { currentScreen = OpporrsScreen.EDITOR }
                        )
                    }
                }
            }
        }
    }

    // Gemini AI Structured Document Generator Dialog
    if (showAiGeneratorDialog) {
        var briefText by remember {
            mutableStateOf("Senior Android Engineer CV highlighting Kotlin, Jetpack Compose, and Firebase architecture.")
        }
        var docCategory by remember { mutableStateOf("Career") }

        AlertDialog(
            onDismissRequest = {
                showAiGeneratorDialog = false
                viewModel.resetAiState()
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Draft Document with Gemini") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Describe the document you need and Gemini will generate structured sections ready to edit.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Career", "Business", "Letters", "Reports").forEach { cat ->
                            FilterChip(
                                selected = docCategory == cat,
                                onClick = { docCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = briefText,
                        onValueChange = { briefText = it },
                        label = { Text("Document Brief") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    when (val state = aiState) {
                        is AiAssistState.Loading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating structured sections...")
                            }
                        }
                        is AiAssistState.Error -> {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> Unit
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.generateFullDocumentWithAi(briefText, docCategory) {
                            showAiGeneratorDialog = false
                            viewModel.resetAiState()
                            currentScreen = OpporrsScreen.EDITOR
                        }
                    }
                ) {
                    Text("Generate & Open")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAiGeneratorDialog = false
                        viewModel.resetAiState()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Notifications & Workspace Activity Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Workspace Activity & Sync") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Cloud Firestore Sync: Connected & active")
                    Text("• Template Library: ${TemplateCatalog.allTemplates.size} editorial & pastel templates ready")
                    Text("• Recent Exports: ${exports.size} completed file exports")
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun AppLockScreen(
    expectedPin: String,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val keyguardManager = remember(context) {
        context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }
    val isDeviceSecure = remember(keyguardManager) {
        keyguardManager?.isDeviceSecure == true
    }

    // System Biometric / Face Unlock / Device Credential Prompt Launcher
    val systemCredentialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onUnlocked()
        } else {
            errorText = "System authentication canceled. Enter your Opporrs PIN or retry biometric unlock."
        }
    }

    fun launchSystemBiometricOrDeviceCredential() {
        @Suppress("DEPRECATION")
        val confirmIntent = keyguardManager?.createConfirmDeviceCredentialIntent(
            "Unlock Opporrs Workspace",
            "Authenticate with System Biometric, Face Unlock, or Device PIN"
        )
        if (confirmIntent != null) {
            systemCredentialLauncher.launch(confirmIntent)
        } else {
            errorText = "System Lock Screen / Biometrics not enrolled on this device. Please use your Opporrs PIN below."
        }
    }

    // Automatically prompt system biometric / device credential on initial lock screen appearance if enrolled
    LaunchedEffect(isDeviceSecure) {
        if (isDeviceSecure) {
            launchSystemBiometricOrDeviceCredential()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "App Lock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Opporrs Workspace Locked",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Unlock using System Biometric / Face Unlock or enter your security PIN.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { launchSystemBiometricOrDeviceCredential() },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlock_system_biometric_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock with System Biometric / Face / Device PIN")
                }

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { enteredPin = it.filter { c -> c.isDigit() } },
                    label = { Text("Opporrs Security PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(
                        text = errorText.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = {
                        if (expectedPin.isBlank() || enteredPin == expectedPin) {
                            onUnlocked()
                        } else {
                            errorText = "Incorrect PIN. Please try again."
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlock_pin_button")
                ) {
                    Text("Unlock with PIN")
                }
            }
        }
    }
}

package com.example.ui.screens

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ExportRecord
import com.example.data.model.SignatureItem
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.example.ui.theme.AvailableFontFamilies
import com.example.ui.theme.BrandAccentPresets
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalCompactMode
import com.example.ui.theme.PastelThemePalettes
import com.example.ui.theme.SupportedLanguages
import com.example.ui.theme.ThemeModePresets
import com.example.ui.theme.parseHexColor

@Composable
fun ProfileAndSettingsScreen(
    userProfile: UserProfile,
    userSettings: UserSettings,
    savedSignatures: List<SignatureItem>,
    exportHistory: List<ExportRecord>,
    authEmail: String,
    onSaveProfile: (UserProfile) -> Unit,
    onSaveSettings: (UserSettings) -> Unit,
    onOpenSignatureManager: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val compactMode = LocalCompactMode.current

    var fullName by remember(userProfile.fullName) { mutableStateOf(userProfile.fullName) }
    var jobTitle by remember(userProfile.jobTitle) { mutableStateOf(userProfile.jobTitle) }
    var email by remember(userProfile.email, authEmail) {
        mutableStateOf(userProfile.email.ifBlank { authEmail })
    }
    var phone by remember(userProfile.phone) { mutableStateOf(userProfile.phone) }
    var location by remember(userProfile.location) { mutableStateOf(userProfile.location) }
    var website by remember(userProfile.website) { mutableStateOf(userProfile.website) }
    var summary by remember(userProfile.summary) { mutableStateOf(userProfile.summary) }
    var experienceText by remember(userProfile.experienceText) { mutableStateOf(userProfile.experienceText) }
    var educationText by remember(userProfile.educationText) { mutableStateOf(userProfile.educationText) }
    var skillsText by remember(userProfile.skillsText) { mutableStateOf(userProfile.skillsText) }
    var referencesText by remember(userProfile.referencesText) { mutableStateOf(userProfile.referencesText) }
    var avatarUri by remember(userProfile.avatarUri) { mutableStateOf(userProfile.avatarUri) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    val hasDeviceSystemLock = remember(context) {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        km?.isDeviceSecure == true
    }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri.toString()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onSaveSettings(userSettings.copy(notificationsEnabled = granted))
    }

    val padH = if (compactMode) 12.dp else 18.dp
    val padV = if (compactMode) 10.dp else 16.dp
    val sectionGap = if (compactMode) 12.dp else 18.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = padH, vertical = padV),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = strings.profileAndSettingsTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.profileAndSettingsSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Theme, Pastel Palettes, Typography, Compact Mode & Language Card (Moved to top for immediate customization)
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
                    modifier = Modifier.padding(if (compactMode) 14.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.themeAndAppearance,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text("Theme & Pastel Color Palettes (10 Modes)", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ThemeModePresets, key = { it.first }) { (code, label) ->
                            val isSelected = userSettings.themeMode == code
                            val pastelSwatch = PastelThemePalettes[code]?.swatchHex
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onSaveSettings(userSettings.copy(themeMode = code))
                                },
                                leadingIcon = if (pastelSwatch != null) {
                                    {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(parseHexColor(pastelSwatch), CircleShape)
                                        )
                                    }
                                } else null,
                                label = { Text(label) },
                                modifier = Modifier.testTag("theme_mode_${code.lowercase()}")
                            )
                        }
                    }

                    Text("Brand Accent Color (12 Accents)", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(BrandAccentPresets, key = { it.first }) { (hex, name) ->
                            val isSelected = userSettings.accentHex.equals(hex, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onSaveSettings(userSettings.copy(accentHex = hex))
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(parseHexColor(hex), CircleShape)
                                    )
                                },
                                label = { Text(name) }
                            )
                        }
                    }

                    Text("Typography & Editorial Fonts (9 Font Families)", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AvailableFontFamilies, key = { it }) { fontName ->
                            FilterChip(
                                selected = userSettings.fontFamily == fontName,
                                onClick = {
                                    onSaveSettings(userSettings.copy(fontFamily = fontName))
                                },
                                label = { Text(fontName) }
                            )
                        }
                    }

                    Text("Text Size Preference", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SMALL", "NORMAL", "LARGE", "XLARGE").forEach { scale ->
                            FilterChip(
                                selected = userSettings.fontScale == scale,
                                onClick = {
                                    onSaveSettings(userSettings.copy(fontScale = scale))
                                },
                                label = { Text(scale) }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.compactModeTitle, style = MaterialTheme.typography.titleSmall)
                            Text(
                                strings.compactModeSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userSettings.compactMode,
                            onCheckedChange = { enabled ->
                                onSaveSettings(userSettings.copy(compactMode = enabled))
                            },
                            modifier = Modifier.testTag("compact_mode_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.languageSelectionTitle, style = MaterialTheme.typography.titleSmall)
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SupportedLanguages, key = { it.first }) { (code, label) ->
                            FilterChip(
                                selected = userSettings.languageCode == code,
                                onClick = {
                                    onSaveSettings(userSettings.copy(languageCode = code))
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("lang_chip_$code")
                            )
                        }
                    }
                }
            }
        }

        // 2. Reusable Personal Profile Card
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
                    modifier = Modifier.padding(if (compactMode) 14.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        avatarPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val ch = (fullName.firstOrNull() ?: authEmail.firstOrNull() ?: 'R').uppercaseChar()
                                    Text(
                                        text = ch.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Reusable Personal Profile",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Only inserted into documents when you approve",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                avatarPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = "Select profile photo")
                        }
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_full_name_input")
                    )

                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("Professional Title / Role") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("City / Country") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = website,
                            onValueChange = { website = it },
                            label = { Text("Website / Portfolio") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Professional Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = experienceText,
                        onValueChange = { experienceText = it },
                        label = { Text("Work Experience (one role per line)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = educationText,
                        onValueChange = { educationText = it },
                        label = { Text("Education & Qualifications") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = skillsText,
                        onValueChange = { skillsText = it },
                        label = { Text("Skills (e.g., Communication | Organization | Technical)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = referencesText,
                        onValueChange = { referencesText = it },
                        label = { Text("References") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenSignatureManager,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_signature_manager_button"),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Outlined.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Signatures (${savedSignatures.size})")
                        }

                        Button(
                            onClick = {
                                onSaveProfile(
                                    userProfile.copy(
                                        fullName = fullName,
                                        jobTitle = jobTitle,
                                        email = email,
                                        phone = phone,
                                        location = location,
                                        website = website,
                                        summary = summary,
                                        experienceText = experienceText,
                                        educationText = educationText,
                                        skillsText = skillsText,
                                        referencesText = referencesText,
                                        avatarUri = avatarUri
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_profile_button"),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(strings.saveProfile)
                        }
                    }
                }
            }
        }

        // 3. System Biometric / FaceLock / PIN App Lock & Privacy Controls Card
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
                    modifier = Modifier.padding(if (compactMode) 14.dp else 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(strings.appLockTitle, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = if (hasDeviceSystemLock) {
                                        "System Biometric / FaceLock / Device PIN detected & ready"
                                    } else {
                                        strings.appLockSubtitle
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = userSettings.appLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinDialog = true
                                } else {
                                    onSaveSettings(userSettings.copy(appLockEnabled = false, appLockPin = ""))
                                }
                            },
                            modifier = Modifier.testTag("app_lock_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Export & Sync Notifications", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Contextual alerts for completed exports",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = userSettings.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onSaveSettings(userSettings.copy(notificationsEnabled = enabled))
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPrivacyDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Privacy & Sync")
                        }
                        OutlinedButton(
                            onClick = { showHelpDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Help & Support")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("About Opporrs")
                        }
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                }
            }
        }

        // 4. Required Credit in Settings: "Made with love by Rahul Shah."
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_credit_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Made with love by Rahul Shah.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Enable Biometric, FaceLock & PIN Lock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Protect Opporrs using your Android System Biometric (Fingerprint / Face Unlock / Device PIN) plus an optional backup PIN:"
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) pinInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Backup PIN (4–6 digits, optional if using System Lock)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val effectivePin = pinInput.takeIf { it.length >= 4 } ?: "0000"
                        onSaveSettings(userSettings.copy(appLockEnabled = true, appLockPin = effectivePin))
                        showPinDialog = false
                    }
                ) {
                    Text("Enable Biometric & PIN Lock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy, Cloud Sync & Exports") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Account: ${authEmail.ifBlank { "Google Authenticated User" }}")
                    Text("• Cloud Sync: All documents, folders, profile fields, and signatures are synchronized in real time with Cloud Firestore Enterprise edition under strict per-UID security rules.")
                    Text("• Export History: ${exportHistory.size} verified PDF/TXT/CSV exports recorded in this workspace.")
                    Text("• Zero Unapproved Insertion: Your Personal Profile information is never added to a document without your explicit confirmation.")
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support") },
            text = {
                Text(
                    "• Creating Documents: Choose from 130+ templates or build your own Custom Template in the Templates tab.\n\n" +
                        "• Scanning: Tap 'Scan document' to capture with your phone's System Native Camera app or live CameraX scanner, adjust perspective/contrast, or extract text with Gemini Vision.\n\n" +
                        "• Exporting: Open any document and tap 'Export PDF' to generate validated A4/Letter PDF, TXT, or CSV files."
                )
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Opporrs") },
            text = {
                Text(
                    "Opporrs v1.1\n" +
                        "Professional Document Creation, Editing, Scanning, Organizing & Exporting.\n\n" +
                        "Architecture: Native Android Kotlin, Jetpack Compose, Material 3, Firebase Auth, Cloud Firestore & Firebase AI Logic.\n\n" +
                        "Made with love by Rahul Shah."
                )
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SignatureManagerScreen(
    savedSignatures: List<SignatureItem>,
    onSaveNewSignature: (String, String, String) -> Unit,
    onSelectSignatureForDocument: (String, String) -> Unit,
    onDeleteSignature: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var signatureLabel by remember { mutableStateOf("Formal Signature") }
    var selectedInkHex by remember { mutableStateOf("#202A25") }
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasWidth by remember { mutableFloatStateOf(1f) }
    var canvasHeight by remember { mutableFloatStateOf(1f) }

    fun serializeNormalizedStrokes(): String {
        val all = strokes.toList()
        if (all.isEmpty()) return ""
        return all.joinToString("|") { stroke ->
            stroke.joinToString(";") { pt ->
                val nx = (pt.x / canvasWidth.coerceAtLeast(1f)).coerceIn(0f, 1f)
                val ny = (pt.y / canvasHeight.coerceAtLeast(1f)).coerceIn(0f, 1f)
                "${"%.3f".format(nx)},${"%.3f".format(ny)}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Signature Manager",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                TextButton(
                    onClick = {
                        strokes.clear()
                        currentStroke = emptyList()
                    }
                ) {
                    Text("Clear Pad")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = signatureLabel,
                    onValueChange = { signatureLabel = it },
                    label = { Text("Signature Label / Signer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "#202A25" to "Charcoal Ink",
                        "#285B4B" to "Editorial Green",
                        "#1E4E5D" to "Executive Blue"
                    ).forEach { (hex, name) ->
                        FilterChip(
                            selected = selectedInkHex == hex,
                            onClick = { selectedInkHex = hex },
                            label = { Text(name) }
                        )
                    }
                }
            }

            // Interactive Vector Signature Drawing Canvas
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val inkColor = parseHexColor(selectedInkHex)
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentStroke = listOf(offset)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            currentStroke = currentStroke + change.position
                                        },
                                        onDragEnd = {
                                            if (currentStroke.size > 1) {
                                                strokes.add(currentStroke)
                                            }
                                            currentStroke = emptyList()
                                        }
                                    )
                                }
                        ) {
                            canvasWidth = size.width
                            canvasHeight = size.height

                            drawLine(
                                color = Color.LightGray,
                                start = Offset(24f, size.height * 0.75f),
                                end = Offset(size.width - 24f, size.height * 0.75f),
                                strokeWidth = 2f
                            )

                            val allStrokes = strokes + listOf(currentStroke)
                            allStrokes.forEach { pts ->
                                if (pts.size > 1) {
                                    val path = Path().apply {
                                        moveTo(pts.first().x, pts.first().y)
                                        for (i in 1 until pts.size) {
                                            lineTo(pts[i].x, pts[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = inkColor,
                                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val data = serializeNormalizedStrokes().ifBlank {
                                "0.10,0.65;0.25,0.45;0.42,0.68;0.65,0.48;0.85,0.62"
                            }
                            onSaveNewSignature(signatureLabel, data, selectedInkHex)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Save to Cloud")
                    }
                    Button(
                        onClick = {
                            val data = serializeNormalizedStrokes().ifBlank {
                                "0.10,0.65;0.25,0.45;0.42,0.68;0.65,0.48;0.85,0.62"
                            }
                            onSaveNewSignature(signatureLabel, data, selectedInkHex)
                            onSelectSignatureForDocument(data, signatureLabel)
                            onBack()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Insert into Document")
                    }
                }
            }

            if (savedSignatures.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved Cloud Signatures (${savedSignatures.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(savedSignatures, key = { it.id }) { sig ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSignatureForDocument(sig.svgPathData, sig.label)
                                onBack()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sig.label, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Tap to insert into active document",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteSignature(sig.id) }) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete signature")
                            }
                        }
                    }
                }
            }
        }
    }
}

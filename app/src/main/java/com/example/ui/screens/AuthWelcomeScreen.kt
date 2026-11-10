package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.onGoogleSignInClicked
import com.example.ui.components.OpporrsBrandMark
import com.example.ui.components.PaperPenFolderIllustration

@Composable
fun AuthWelcomeScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showProviderInfoDialog by remember { mutableStateOf<String?>(null) }
    var showLegalDialog by remember { mutableStateOf<String?>(null) }

    // Attempt silent auto sign-in once on launch
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    fun triggerGoogleSignIn() {
        isLoading = true
        errorMessage = null
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
            },
            onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
            },
            scope = coroutineScope,
            onAuthCancelled = {
                isLoading = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Identity Section (Steps 2, 3, 4, 5 of Screen 01 spec)
            Column(
                modifier = Modifier.widthIn(max = 460.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    OpporrsBrandMark(size = 44.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Opporrs",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Your documents, beautifully organized.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Create, edit and manage professional documents in one place.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Center Illustration (Step 6 of Screen 01 spec)
            Card(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .height(210.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PaperPenFolderIllustration(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Bottom Actions & Legal (Steps 7, 8, 9, 10 of Screen 01 spec)
            Column(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // 7. Primary button: Continue with Google
                Button(
                    onClick = { triggerGoogleSignIn() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("continue_with_google_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Signing in with Google...")
                    } else {
                        Surface(
                            modifier = Modifier.size(26.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "G",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 8. Secondary button: Continue with Email
                OutlinedButton(
                    onClick = { showProviderInfoDialog = "EMAIL" },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("continue_with_email_button"),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email sign in",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Email",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 9. Tertiary option: Continue as Guest
                TextButton(
                    onClick = { showProviderInfoDialog = "GUEST" },
                    enabled = !isLoading,
                    modifier = Modifier.testTag("continue_as_guest_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = "Guest access info",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 10. Terms of Service and Privacy Policy at the bottom
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { showLegalDialog = "TERMS" }
                            .padding(6.dp)
                            .testTag("terms_of_service_link")
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { showLegalDialog = "PRIVACY" }
                            .padding(6.dp)
                            .testTag("privacy_policy_link")
                    )
                }
            }
        }
    }

    if (showProviderInfoDialog != null) {
        val isEmail = showProviderInfoDialog == "EMAIL"
        AlertDialog(
            onDismissRequest = { showProviderInfoDialog = null },
            icon = {
                Icon(
                    imageVector = if (isEmail) Icons.Outlined.VerifiedUser else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = if (isEmail) "Google Identity Verification" else "Zero-Trust Cloud Workspace",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = if (isEmail) {
                        "Opporrs uses Google Sign-In via Android Credential Manager to protect your Cloud Firestore documents and Gemini AI workspace with zero-trust security rules. Select your email account through Google Sign-In below (or enable additional providers in the Firebase Console)."
                    } else {
                        "To protect your private documents, signatures, and Gemini AI requests with hardware-backed App Check and per-user Firestore security rules, Opporrs authenticates sessions via Google Sign-In. Tap below to sign in with your Google account in one tap."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProviderInfoDialog = null
                        triggerGoogleSignIn()
                    }
                ) {
                    Text("Sign in with Google")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProviderInfoDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showLegalDialog != null) {
        val isTerms = showLegalDialog == "TERMS"
        AlertDialog(
            onDismissRequest = { showLegalDialog = null },
            title = {
                Text(
                    text = if (isTerms) "Opporrs Terms of Service" else "Opporrs Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = if (isTerms) {
                        "1. Ownership: You retain 100% ownership and copyright of all CVs, letters, business proposals, scans, and signatures created in Opporrs.\n\n2. Templates: All 112+ Opporrs templates are licensed for unrestricted personal, academic, and commercial document exports.\n\n3. Cloud Sync: Real-time synchronization is powered by Google Cloud Firestore with strict per-user isolation."
                    } else {
                        "1. Zero-Trust Isolation: Every document, profile field, folder, and signature is scoped strictly to your authenticated Firebase UID.\n\n2. Contextual Permissions: Camera, Photo Picker, and Storage Access Framework permissions are requested only when you initiate scanning or file export.\n\n3. Explicit Profile Consent: Your saved Personal Profile fields are never inserted into a document unless you explicitly approve it."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showLegalDialog = null }) {
                    Text("Understood")
                }
            }
        )
    }
}

package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.DocumentItem
import com.example.data.model.ExportRecord
import com.example.data.model.FolderItem
import com.example.data.model.SignatureItem
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class OpporrsRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. User Settings (/users/{userId}) ---
    fun observeUserSettings(userId: String = auth.currentUser?.uid.orEmpty()): Flow<UserSettings?> = flow {
        val uid = userId.ifBlank { requireUserId() }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    logFlowError(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    suspend fun saveUserSettings(settings: UserSettings): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updatePayload = mapOf(
                    "themeMode" to settings.themeMode,
                    "accentHex" to settings.accentHex,
                    "fontFamily" to settings.fontFamily,
                    "fontScale" to settings.fontScale,
                    "compactMode" to settings.compactMode,
                    "languageCode" to settings.languageCode,
                    "appLockEnabled" to settings.appLockEnabled,
                    "appLockPin" to settings.appLockPin,
                    "notificationsEnabled" to settings.notificationsEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "themeMode" to settings.themeMode,
                    "accentHex" to settings.accentHex,
                    "fontFamily" to settings.fontFamily,
                    "fontScale" to settings.fontScale,
                    "compactMode" to settings.compactMode,
                    "languageCode" to settings.languageCode,
                    "appLockEnabled" to settings.appLockEnabled,
                    "appLockPin" to settings.appLockPin,
                    "notificationsEnabled" to settings.notificationsEnabled,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 2. User Profile (/profiles/{userId}) ---
    fun observeUserProfile(userId: String = auth.currentUser?.uid.orEmpty()): Flow<UserProfile?> = flow {
        val uid = userId.ifBlank { requireUserId() }
        val path = "profiles/$uid"
        emitAll(
            db.collection("profiles").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    logFlowError(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("profiles").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updatePayload = mapOf(
                    "fullName" to profile.fullName.take(120),
                    "jobTitle" to profile.jobTitle.take(120),
                    "email" to profile.email.take(160),
                    "phone" to profile.phone.take(60),
                    "location" to profile.location.take(160),
                    "website" to profile.website.take(200),
                    "summary" to profile.summary.take(4000),
                    "experienceText" to profile.experienceText.take(8000),
                    "educationText" to profile.educationText.take(8000),
                    "skillsText" to profile.skillsText.take(4000),
                    "referencesText" to profile.referencesText.take(4000),
                    "avatarUri" to profile.avatarUri.take(1000),
                    "defaultSignatureSvg" to profile.defaultSignatureSvg.take(20000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "fullName" to profile.fullName.take(120),
                    "jobTitle" to profile.jobTitle.take(120),
                    "email" to profile.email.take(160),
                    "phone" to profile.phone.take(60),
                    "location" to profile.location.take(160),
                    "website" to profile.website.take(200),
                    "summary" to profile.summary.take(4000),
                    "experienceText" to profile.experienceText.take(8000),
                    "educationText" to profile.educationText.take(8000),
                    "skillsText" to profile.skillsText.take(4000),
                    "referencesText" to profile.referencesText.take(4000),
                    "avatarUri" to profile.avatarUri.take(1000),
                    "defaultSignatureSvg" to profile.defaultSignatureSvg.take(20000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 3. Documents (/documents/{documentId}) ---
    fun observeDocuments(userId: String = auth.currentUser?.uid.orEmpty()): Flow<List<DocumentItem>> = flow {
        val uid = userId.ifBlank { requireUserId() }
        val path = "documents"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(DocumentItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun getUserDocuments(): Result<List<DocumentItem>> {
        val uid = requireUserId()
        val path = "documents"
        return try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val list = snapshot.toObjects(DocumentItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun getDocumentById(docId: String): Result<DocumentItem?> {
        val docRef = db.collection("documents").document(docId)
        return try {
            val snapshot = docRef.get().await()
            val doc = snapshot.toObject(DocumentItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            Result.success(doc)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createDocument(doc: DocumentItem): Result<String> {
        val uid = requireUserId()
        val newId = doc.id.ifBlank { "doc_${UUID.randomUUID().toString().replace("-", "")}" }
        val docRef = db.collection("documents").document(newId)
        val payload = mapOf(
            "userId" to uid,
            "title" to doc.title.ifBlank { "Untitled Document" }.take(200),
            "category" to doc.category.ifBlank { "Career" }.take(60),
            "templateId" to doc.templateId.take(128),
            "folderId" to doc.folderId.take(128),
            "status" to doc.status.ifBlank { "draft" },
            "isFavorite" to doc.isFavorite,
            "pageFormat" to doc.pageFormat.ifBlank { "A4" },
            "orientation" to doc.orientation.ifBlank { "Portrait" },
            "marginMm" to doc.marginMm.coerceIn(0, 60),
            "accentHex" to doc.accentHex.ifBlank { "#285B4B" }.take(20),
            "fontFamily" to doc.fontFamily.ifBlank { "Playfair Display" }.take(60),
            "sectionsData" to doc.sectionsData.take(150000),
            "signatureSvg" to doc.signatureSvg.take(30000),
            "scannedPagesData" to doc.scannedPagesData.take(50000),
            "wordCount" to doc.wordCount.coerceAtLeast(0),
            "pageCount" to doc.pageCount.coerceAtLeast(1),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(newId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateDocument(doc: DocumentItem): Result<Unit> {
        requireUserId()
        require(doc.id.isNotBlank()) { "Document ID must not be blank for update" }
        val docRef = db.collection("documents").document(doc.id)
        val payload = mapOf(
            "title" to doc.title.ifBlank { "Untitled Document" }.take(200),
            "category" to doc.category.ifBlank { "Career" }.take(60),
            "templateId" to doc.templateId.take(128),
            "folderId" to doc.folderId.take(128),
            "status" to doc.status.ifBlank { "draft" },
            "isFavorite" to doc.isFavorite,
            "pageFormat" to doc.pageFormat.ifBlank { "A4" },
            "orientation" to doc.orientation.ifBlank { "Portrait" },
            "marginMm" to doc.marginMm.coerceIn(0, 60),
            "accentHex" to doc.accentHex.ifBlank { "#285B4B" }.take(20),
            "fontFamily" to doc.fontFamily.ifBlank { "Playfair Display" }.take(60),
            "sectionsData" to doc.sectionsData.take(150000),
            "signatureSvg" to doc.signatureSvg.take(30000),
            "scannedPagesData" to doc.scannedPagesData.take(50000),
            "wordCount" to doc.wordCount.coerceAtLeast(0),
            "pageCount" to doc.pageCount.coerceAtLeast(1),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(docId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("documents").document(docId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 4. Folders (/folders/{folderId}) ---
    fun observeFolders(userId: String = auth.currentUser?.uid.orEmpty()): Flow<List<FolderItem>> = flow {
        val uid = userId.ifBlank { requireUserId() }
        val path = "folders"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FolderItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun createFolder(name: String, description: String, colorHex: String): Result<String> {
        val uid = requireUserId()
        val folderId = "fld_${UUID.randomUUID().toString().replace("-", "")}"
        val docRef = db.collection("folders").document(folderId)
        val payload = mapOf(
            "userId" to uid,
            "name" to name.ifBlank { "New Folder" }.take(120),
            "description" to description.take(500),
            "colorHex" to colorHex.ifBlank { "#285B4B" }.take(20),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(folderId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteFolder(folderId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("folders").document(folderId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 5. Signatures (/signatures/{signatureId}) ---
    fun observeSignatures(userId: String = auth.currentUser?.uid.orEmpty()): Flow<List<SignatureItem>> = flow {
        val uid = userId.ifBlank { requireUserId() }
        val path = "signatures"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SignatureItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun createSignature(label: String, svgPathData: String, strokeColorHex: String): Result<String> {
        val uid = requireUserId()
        val sigId = "sig_${UUID.randomUUID().toString().replace("-", "")}"
        val docRef = db.collection("signatures").document(sigId)
        val payload = mapOf(
            "userId" to uid,
            "label" to label.ifBlank { "Signature" }.take(100),
            "svgPathData" to svgPathData.ifBlank { "M 10 50 L 90 50" }.take(30000),
            "strokeColorHex" to strokeColorHex.ifBlank { "#202A25" }.take(20),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(sigId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteSignature(signatureId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("signatures").document(signatureId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 6. Exports (/exports/{exportId}) ---
    fun observeExports(userId: String = auth.currentUser?.uid.orEmpty()): Flow<List<ExportRecord>> = flow {
        val uid = userId.ifBlank { requireUserId() }
        val path = "exports"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ExportRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logFlowError(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun logExport(record: ExportRecord): Result<String> {
        val uid = requireUserId()
        val expId = "exp_${UUID.randomUUID().toString().replace("-", "")}"
        val docRef = db.collection("exports").document(expId)
        val payload = mapOf(
            "userId" to uid,
            "documentId" to record.documentId.take(128),
            "documentTitle" to record.documentTitle.ifBlank { "Untitled" }.take(200),
            "format" to record.format.ifBlank { "PDF" },
            "pageSize" to record.pageSize.ifBlank { "A4 Portrait" }.take(40),
            "fileSizeBytes" to record.fileSizeBytes.coerceAtLeast(0),
            "fileUri" to record.fileUri.take(1000),
            "status" to record.status.ifBlank { "completed" },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(expId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    private fun logFlowError(error: Throwable, operationType: OperationType, path: String, uid: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }
}

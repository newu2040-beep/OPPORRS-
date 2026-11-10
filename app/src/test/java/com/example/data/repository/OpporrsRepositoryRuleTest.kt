package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.DocumentItem
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class OpporrsRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createDocument_validPayload_createsAndReadsBack() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = OpporrsRepository(firestore)

        val newDoc = DocumentItem(
            title = "Executive Resume 2026",
            category = "Career",
            templateId = "career_cv_01",
            status = "draft",
            wordCount = 240,
            pageCount = 1
        )
        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createDocument(newDoc) }
        assertTrue(createResult.isSuccess)
        val docId = createResult.getOrThrow()
        assertTrue(docId.isNotEmpty())

        val listResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getUserDocuments() }
        assertTrue(listResult.isSuccess)
        assertTrue(listResult.getOrThrow().any { it.id == docId && it.title == "Executive Resume 2026" })
    }

    @Test
    fun observeDocuments_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = OpporrsRepository(firestore)
        val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createDocument(DocumentItem(title = "Realtime Proposal", category = "Business")).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeDocuments(uid).first { list -> list.any { it.id == docId } }
        }
        assertTrue(emitted.any { it.id == docId })
    }

    @Test
    fun getDocumentById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = OpporrsRepository(firestore)
        val docId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createDocument(DocumentItem(title = "Alice Private Document")).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = OpporrsRepository(firestore)
        val getResult = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getDocumentById(docId) }
        assertTrue(getResult.isFailure)
        val firestoreEx = getResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeDocuments_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = OpporrsRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeDocuments(uid).first()
            }
            fail("Expected exception for unauthenticated query")
        } catch (e: Exception) {
            val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
        }
    }

    @Test
    fun saveProfileAndSettings_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = OpporrsRepository(firestore)

        val profileRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserProfile(
                UserProfile(
                    userId = uid,
                    fullName = "Rahul Shah",
                    jobTitle = "Principal Product Designer",
                    email = "rahul@example.com"
                )
            )
        }
        assertTrue(profileRes.isSuccess)

        val settingsRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserSettings(
                UserSettings(
                    userId = uid,
                    themeMode = "DARK",
                    accentHex = "#285B4B"
                )
            )
        }
        assertTrue(settingsRes.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@opporrs.test"
        const val BOB_EMAIL = "bob@opporrs.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}

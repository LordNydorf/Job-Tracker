package com.rohit.jobtracker.android.cache

import com.rohit.jobtracker.android.sync.MutationType
import com.rohit.jobtracker.android.sync.PendingMutation
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Note
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalApplicationStoreTest {

    private lateinit var tempDir: File
    private lateinit var store: LocalApplicationStore

    @Before
    fun setUp() {
        tempDir = File("build/test-data/store_test_${System.nanoTime()}")
        tempDir.mkdirs()
        store = LocalApplicationStore(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testSaveAndGetApplications() {
        val app1 = Application(
            id = "app-1",
            company = "Google",
            role = "Staff Android Engineer",
            source = Source.REFERRAL,
            dateApplied = LocalDate.parse("2026-08-31"),
            status = Status.INTERVIEW,
            lastUpdated = Instant.parse("2026-08-31T10:00:00Z")
        )

        store.saveOrUpdateApplication(app1)
        val cached = store.getCachedApplications()
        assertEquals(1, cached.size)
        assertEquals(app1, cached.first())
        assertEquals(app1, store.getCachedApplication("app-1"))

        // Update application
        val updated = app1.copy(role = "Principal Engineer")
        store.saveOrUpdateApplication(updated)
        assertEquals(1, store.getCachedApplications().size)
        assertEquals("Principal Engineer", store.getCachedApplication("app-1")?.role)
    }

    @Test
    fun testNotesCachingAndCascadeDeletion() {
        val app = Application(
            id = "app-2",
            company = "Anthropic",
            role = "AI Engineer",
            source = Source.WELLFOUND,
            dateApplied = LocalDate.parse("2026-08-31"),
            status = Status.APPLIED,
            lastUpdated = Instant.parse("2026-08-31T10:00:00Z")
        )
        store.saveOrUpdateApplication(app)

        val note1 = Note(id = "n-1", applicationId = "app-2", text = "Screen passed", createdAt = Instant.parse("2026-08-31T11:00:00Z"))
        val note2 = Note(id = "n-2", applicationId = "app-2", text = "Tech round scheduled", createdAt = Instant.parse("2026-08-31T12:00:00Z"))

        store.addCachedNote("app-2", note1)
        store.addCachedNote("app-2", note2)

        val notes = store.getCachedNotes("app-2")
        assertEquals(2, notes.size)

        store.deleteCachedNote("app-2", "n-1")
        assertEquals(1, store.getCachedNotes("app-2").size)
        assertEquals("n-2", store.getCachedNotes("app-2").first().id)

        // Delete application should cascade remove notes
        store.deleteCachedApplication("app-2")
        assertNull(store.getCachedApplication("app-2"))
        assertTrue(store.getCachedNotes("app-2").isEmpty())
    }

    @Test
    fun testMutationQueueOperations() {
        assertEquals(0, store.pendingMutationsCount.value)
        assertTrue(store.getPendingMutations().isEmpty())

        val mutation1 = PendingMutation(
            id = "m-1",
            type = MutationType.CREATE_APP,
            entityId = "app-10",
            payloadJson = "{}"
        )
        val mutation2 = PendingMutation(
            id = "m-2",
            type = MutationType.ADD_NOTE,
            entityId = "note-20",
            parentEntityId = "app-10",
            payloadJson = "{}"
        )

        store.enqueueMutation(mutation1)
        assertEquals(1, store.pendingMutationsCount.value)

        store.enqueueMutation(mutation2)
        assertEquals(2, store.pendingMutationsCount.value)

        val queue = store.getPendingMutations()
        assertEquals(2, queue.size)
        assertEquals("m-1", queue[0].id)
        assertEquals("m-2", queue[1].id)

        store.removeMutation("m-1")
        assertEquals(1, store.pendingMutationsCount.value)
        assertEquals("m-2", store.getPendingMutations().first().id)

        store.clearPendingMutations()
        assertEquals(0, store.pendingMutationsCount.value)
        assertTrue(store.getPendingMutations().isEmpty())
    }
}

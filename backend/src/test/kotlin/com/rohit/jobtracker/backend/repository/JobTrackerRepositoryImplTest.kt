package com.rohit.jobtracker.backend.repository

import com.rohit.jobtracker.backend.db.DatabaseFactory
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JobTrackerRepositoryImplTest {

    private lateinit var repository: JobTrackerRepository

    @Before
    fun setup() {
        val testDbFile = File("build/test-data/repo_test_${System.nanoTime()}.db")
        testDbFile.parentFile?.mkdirs()
        DatabaseFactory.init(jdbcUrl = "jdbc:sqlite:${testDbFile.absolutePath}")
        repository = JobTrackerRepositoryImpl()
    }

    @Test
    fun testCreateAndFetchApplication() = runBlocking {
        val request = CreateApplicationRequest(
            company = "Anthropic",
            role = "AI Engineer",
            source = Source.WELLFOUND,
            dateApplied = LocalDate.parse("2026-08-31"),
            jobLink = "https://anthropic.com/jobs/1",
            status = Status.APPLIED,
            reminderDays = 7,
            salary = "$180,000 / yr"
        )

        val created = repository.createApplication(request)
        assertNotNull(created.id)
        assertEquals("Anthropic", created.company)
        assertEquals("AI Engineer", created.role)
        assertEquals(Status.APPLIED, created.status)
        assertEquals(7, created.reminderDays)
        assertEquals("$180,000 / yr", created.salary)

        val fetched = repository.getApplication(created.id)
        assertNotNull(fetched)
        assertEquals(created.id, fetched.id)
        assertEquals("Anthropic", fetched.company)
        assertEquals("$180,000 / yr", fetched.salary)
    }

    @Test
    fun testUpdateApplicationFields() = runBlocking {
        val created = repository.createApplication(
            CreateApplicationRequest(
                company = "OpenAI",
                role = "Software Engineer",
                source = Source.REFERRAL,
                dateApplied = LocalDate.parse("2026-08-31"),
                status = Status.APPLIED,
                salary = "₹25 LPA"
            )
        )

        val updated = repository.updateApplication(
            id = created.id,
            request = UpdateApplicationRequest(
                role = "Senior Software Engineer",
                status = Status.INTERVIEW,
                reminderDays = 3,
                salary = "₹32 LPA"
            )
        )

        assertNotNull(updated)
        assertEquals("Senior Software Engineer", updated.role)
        assertEquals(Status.INTERVIEW, updated.status)
        assertEquals(3, updated.reminderDays)
        assertEquals("OpenAI", updated.company)
        assertEquals("₹32 LPA", updated.salary)
    }

    @Test
    fun testAddAndFetchNotesForApplication() = runBlocking {
        val app = repository.createApplication(
            CreateApplicationRequest(
                company = "Google",
                role = "Kotlin Specialist",
                source = Source.REFERRAL,
                dateApplied = LocalDate.parse("2026-08-31")
            )
        )

        val note1 = repository.addNote(app.id, CreateNoteRequest(text = "Recruiter phone screen passed."))
        val note2 = repository.addNote(app.id, CreateNoteRequest(text = "Technical round with staff engineer scheduled."))

        assertNotNull(note1)
        assertNotNull(note2)
        assertEquals(app.id, note1.applicationId)

        val notes = repository.getNotes(app.id)
        assertEquals(2, notes.size)
        assertTrue(notes.any { it.text == "Recruiter phone screen passed." })
        assertTrue(notes.any { it.text == "Technical round with staff engineer scheduled." })
    }

    @Test
    fun testDeleteApplicationCascadesNotes() = runBlocking {
        val app = repository.createApplication(
            CreateApplicationRequest(
                company = "Figma",
                role = "Product Engineer",
                source = Source.OTHER,
                dateApplied = LocalDate.parse("2026-08-31")
            )
        )

        repository.addNote(app.id, CreateNoteRequest(text = "Note before deletion"))
        assertEquals(1, repository.getNotes(app.id).size)

        val deleted = repository.deleteApplication(app.id)
        assertTrue(deleted)

        assertNull(repository.getApplication(app.id))
        assertEquals(0, repository.getNotes(app.id).size)
    }

    @Test
    fun testDeleteNoteRemovesNoteFromApplication() = runBlocking {
        val app = repository.createApplication(
            CreateApplicationRequest(
                company = "Vercel",
                role = "Frontend Engineer",
                source = Source.WELLFOUND,
                dateApplied = LocalDate.parse("2026-08-31")
            )
        )

        val note1 = repository.addNote(app.id, CreateNoteRequest(text = "Initial outreach"))
        val note2 = repository.addNote(app.id, CreateNoteRequest(text = "Hiring manager call"))

        assertNotNull(note1)
        assertNotNull(note2)
        assertEquals(2, repository.getNotes(app.id).size)

        val deleted = repository.deleteNote(app.id, note1.id)
        assertTrue(deleted)

        val remainingNotes = repository.getNotes(app.id)
        assertEquals(1, remainingNotes.size)
        assertEquals(note2.id, remainingNotes.first().id)
        assertEquals("Hiring manager call", remainingNotes.first().text)
    }
}

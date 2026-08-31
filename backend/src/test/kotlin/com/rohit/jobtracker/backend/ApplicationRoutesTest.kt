package com.rohit.jobtracker.backend

import com.rohit.jobtracker.backend.db.DatabaseFactory
import com.rohit.jobtracker.backend.repository.JobTrackerRepositoryImpl
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.Note
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationRoutesTest {

    private val validApiKey = "dev-secret-key"

    @Before
    fun setupDb() {
        val testDbFile = File("build/test-data/test_${System.nanoTime()}.db")
        testDbFile.parentFile?.mkdirs()
        DatabaseFactory.init(jdbcUrl = "jdbc:sqlite:${testDbFile.absolutePath}")
    }

    @Test
    fun testHealthAndRootPublicEndpoints() = testApplication {
        application {
            module(repository = JobTrackerRepositoryImpl(), initDb = false, apiKey = validApiKey)
        }

        val rootResponse = client.get("/")
        assertEquals(HttpStatusCode.OK, rootResponse.status)
        assertEquals("Job Application Tracker API is running!", rootResponse.bodyAsText())

        val healthResponse = client.get("/health")
        assertEquals(HttpStatusCode.OK, healthResponse.status)
    }

    @Test
    fun testAuthRejectsMissingOrInvalidApiKey() = testApplication {
        application {
            module(repository = JobTrackerRepositoryImpl(), initDb = false, apiKey = validApiKey)
        }

        // Missing X-API-Key
        val noKeyResponse = client.get("/applications")
        assertEquals(HttpStatusCode.Unauthorized, noKeyResponse.status)

        // Invalid X-API-Key
        val badKeyResponse = client.get("/applications") {
            header("X-API-Key", "wrong-key")
        }
        assertEquals(HttpStatusCode.Unauthorized, badKeyResponse.status)
    }

    @Test
    fun testFullApplicationCrudAndNotesLifecycle() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        application {
            module(repository = JobTrackerRepositoryImpl(), initDb = false, apiKey = validApiKey)
        }

        // 1. Create Application
        val createRequest = CreateApplicationRequest(
            company = "Wellfound Startup",
            role = "Senior Kotlin Dev",
            source = Source.WELLFOUND,
            dateApplied = LocalDate.parse("2026-08-31"),
            jobLink = "https://wellfound.com/jobs/123",
            status = Status.APPLIED,
            reminderDays = 7
        )

        val createResponse = client.post("/applications") {
            header("X-API-Key", validApiKey)
            contentType(ContentType.Application.Json)
            setBody(createRequest)
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val createdApp = createResponse.body<Application>()
        assertNotNull(createdApp.id)
        assertEquals("Wellfound Startup", createdApp.company)
        assertEquals(Status.APPLIED, createdApp.status)

        val appId = createdApp.id

        // 2. Get All Applications
        val listResponse = client.get("/applications") {
            header("X-API-Key", validApiKey)
        }
        assertEquals(HttpStatusCode.OK, listResponse.status)
        val appList = listResponse.body<List<Application>>()
        assertTrue(appList.any { it.id == appId })

        // 3. Get Single Application by ID
        val getResponse = client.get("/applications/$appId") {
            header("X-API-Key", validApiKey)
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val fetchedApp = getResponse.body<Application>()
        assertEquals(appId, fetchedApp.id)
        assertEquals("Wellfound Startup", fetchedApp.company)

        // 4. Update Application Status to INTERVIEW
        val updateRequest = UpdateApplicationRequest(
            status = Status.INTERVIEW,
            role = "Staff Kotlin Dev"
        )
        val patchResponse = client.patch("/applications/$appId") {
            header("X-API-Key", validApiKey)
            contentType(ContentType.Application.Json)
            setBody(updateRequest)
        }
        assertEquals(HttpStatusCode.OK, patchResponse.status)
        val updatedApp = patchResponse.body<Application>()
        assertEquals(Status.INTERVIEW, updatedApp.status)
        assertEquals("Staff Kotlin Dev", updatedApp.role)

        // 5. Add Note
        val noteRequest = CreateNoteRequest(text = "Initial technical screen scheduled for Thursday 2 PM.")
        val addNoteResponse = client.post("/applications/$appId/notes") {
            header("X-API-Key", validApiKey)
            contentType(ContentType.Application.Json)
            setBody(noteRequest)
        }
        assertEquals(HttpStatusCode.Created, addNoteResponse.status)
        val createdNote = addNoteResponse.body<Note>()
        assertEquals(appId, createdNote.applicationId)
        assertEquals("Initial technical screen scheduled for Thursday 2 PM.", createdNote.text)

        // 6. Get Notes
        val getNotesResponse = client.get("/applications/$appId/notes") {
            header("X-API-Key", validApiKey)
        }
        assertEquals(HttpStatusCode.OK, getNotesResponse.status)
        val notesList = getNotesResponse.body<List<Note>>()
        assertEquals(1, notesList.size)
        assertEquals(createdNote.id, notesList.first().id)

        // 7. Delete Application
        val deleteResponse = client.delete("/applications/$appId") {
            header("X-API-Key", validApiKey)
        }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // 8. Confirm Application and Notes 404 after Deletion
        val getDeletedResponse = client.get("/applications/$appId") {
            header("X-API-Key", validApiKey)
        }
        assertEquals(HttpStatusCode.NotFound, getDeletedResponse.status)

        val getDeletedNotesResponse = client.get("/applications/$appId/notes") {
            header("X-API-Key", validApiKey)
        }
        assertEquals(HttpStatusCode.NotFound, getDeletedNotesResponse.status)
    }

    @Test
    fun testValidationErrorsOnCreate() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        application {
            module(repository = JobTrackerRepositoryImpl(), initDb = false, apiKey = validApiKey)
        }

        // Blank company
        val blankCompanyReq = CreateApplicationRequest(
            company = "   ",
            role = "Dev",
            source = Source.REFERRAL,
            dateApplied = LocalDate.parse("2026-08-31")
        )
        val res1 = client.post("/applications") {
            header("X-API-Key", validApiKey)
            contentType(ContentType.Application.Json)
            setBody(blankCompanyReq)
        }
        assertEquals(HttpStatusCode.BadRequest, res1.status)

        // Blank role
        val blankRoleReq = CreateApplicationRequest(
            company = "Company",
            role = "   ",
            source = Source.REFERRAL,
            dateApplied = LocalDate.parse("2026-08-31")
        )
        val res2 = client.post("/applications") {
            header("X-API-Key", validApiKey)
            contentType(ContentType.Application.Json)
            setBody(blankRoleReq)
        }
        assertEquals(HttpStatusCode.BadRequest, res2.status)
    }
}

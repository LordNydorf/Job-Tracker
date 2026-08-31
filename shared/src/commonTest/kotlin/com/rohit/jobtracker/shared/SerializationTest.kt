package com.rohit.jobtracker.shared

import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.Note
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SerializationTest {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    @Test
    fun testApplicationSerializationRoundTrip() {
        val application = Application(
            id = "app-123",
            company = "Google",
            role = "Senior Kotlin Engineer",
            source = Source.REFERRAL,
            dateApplied = LocalDate.parse("2026-08-31"),
            jobLink = "https://careers.google.com/jobs/123",
            status = Status.INTERVIEW,
            lastUpdated = Instant.parse("2026-08-31T10:15:30Z"),
            reminderDays = 7
        )

        val serialized = json.encodeToString(application)
        val deserialized = json.decodeFromString<Application>(serialized)

        assertEquals(application, deserialized)
        assertEquals("app-123", deserialized.id)
        assertEquals("Google", deserialized.company)
        assertEquals("Senior Kotlin Engineer", deserialized.role)
        assertEquals(Source.REFERRAL, deserialized.source)
        assertEquals(LocalDate.parse("2026-08-31"), deserialized.dateApplied)
        assertEquals("https://careers.google.com/jobs/123", deserialized.jobLink)
        assertEquals(Status.INTERVIEW, deserialized.status)
        assertEquals(Instant.parse("2026-08-31T10:15:30Z"), deserialized.lastUpdated)
        assertEquals(7, deserialized.reminderDays)
    }

    @Test
    fun testApplicationWithNullOptionalFields() {
        val application = Application(
            id = "app-456",
            company = "Acme Corp",
            role = "Android Dev",
            source = Source.WELLFOUND,
            dateApplied = LocalDate.parse("2026-08-30"),
            jobLink = null,
            status = Status.APPLIED,
            lastUpdated = Instant.parse("2026-08-30T12:00:00Z"),
            reminderDays = null
        )

        val serialized = json.encodeToString(application)
        val deserialized = json.decodeFromString<Application>(serialized)

        assertEquals(application, deserialized)
        assertNull(deserialized.jobLink)
        assertNull(deserialized.reminderDays)
    }

    @Test
    fun testNoteSerializationRoundTrip() {
        val note = Note(
            id = "note-789",
            applicationId = "app-123",
            text = "Had screening call with recruiter. Next step is technical round on Friday.",
            createdAt = Instant.parse("2026-08-31T14:30:00Z")
        )

        val serialized = json.encodeToString(note)
        val deserialized = json.decodeFromString<Note>(serialized)

        assertEquals(note, deserialized)
        assertEquals("note-789", deserialized.id)
        assertEquals("app-123", deserialized.applicationId)
        assertEquals("Had screening call with recruiter. Next step is technical round on Friday.", deserialized.text)
    }

    @Test
    fun testRequestDtosSerialization() {
        val createReq = CreateApplicationRequest(
            company = "Stripe",
            role = "Backend Kotlin Engineer",
            source = Source.BRAINTRUST,
            dateApplied = LocalDate.parse("2026-08-31"),
            jobLink = "https://stripe.com/jobs",
            status = Status.APPLIED,
            reminderDays = 5
        )
        val createSerialized = json.encodeToString(createReq)
        val createDeserialized = json.decodeFromString<CreateApplicationRequest>(createSerialized)
        assertEquals(createReq, createDeserialized)

        val updateReq = UpdateApplicationRequest(
            status = Status.OFFER,
            reminderDays = null
        )
        val updateSerialized = json.encodeToString(updateReq)
        val updateDeserialized = json.decodeFromString<UpdateApplicationRequest>(updateSerialized)
        assertEquals(updateReq, updateDeserialized)

        val noteReq = CreateNoteRequest(text = "Offer received!")
        val noteSerialized = json.encodeToString(noteReq)
        val noteDeserialized = json.decodeFromString<CreateNoteRequest>(noteSerialized)
        assertEquals(noteReq, noteDeserialized)
    }

    @Test
    fun testAllEnumValuesSerializedCorrectly() {
        for (status in Status.entries) {
            val encoded = json.encodeToString(status)
            val decoded = json.decodeFromString<Status>(encoded)
            assertEquals(status, decoded)
        }

        for (source in Source.entries) {
            val encoded = json.encodeToString(source)
            val decoded = json.decodeFromString<Source>(encoded)
            assertEquals(source, decoded)
        }
    }
}

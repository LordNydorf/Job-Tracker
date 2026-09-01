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
    fun testApplicationSerializationRoundTripWithAllFields() {
        val application = Application(
            id = "app-123",
            company = "Google",
            role = "Senior Kotlin Engineer",
            source = Source.REFERRAL,
            dateApplied = LocalDate.parse("2026-08-31"),
            jobLink = "https://careers.google.com/jobs/123",
            status = Status.INTERVIEW,
            lastUpdated = Instant.parse("2026-08-31T10:15:30Z"),
            reminderDays = 7,
            salary = "$180,000 / yr"
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
        assertEquals("$180,000 / yr", deserialized.salary)
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
            reminderDays = null,
            salary = null
        )

        val serialized = json.encodeToString(application)
        val deserialized = json.decodeFromString<Application>(serialized)

        assertEquals(application, deserialized)
        assertNull(deserialized.jobLink)
        assertNull(deserialized.reminderDays)
        assertNull(deserialized.salary)
    }

    @Test
    fun testApplicationDefaultValues() {
        val application = Application(
            id = "app-default",
            company = "Linear",
            role = "Product Engineer",
            source = Source.OTHER,
            dateApplied = LocalDate.parse("2026-09-01"),
            lastUpdated = Instant.parse("2026-09-01T08:00:00Z")
        )

        assertEquals(Status.APPLIED, application.status)
        assertNull(application.jobLink)
        assertNull(application.reminderDays)
        assertNull(application.salary)

        val serialized = json.encodeToString(application)
        val deserialized = json.decodeFromString<Application>(serialized)

        assertEquals(application, deserialized)
        assertEquals(Status.APPLIED, deserialized.status)
        assertNull(deserialized.jobLink)
        assertNull(deserialized.reminderDays)
        assertNull(deserialized.salary)
    }

    @Test
    fun testCreateApplicationRequestWithClientSuppliedIdAndSalary() {
        val request = CreateApplicationRequest(
            id = "client-uuid-12345",
            company = "Stripe",
            role = "Staff Android Engineer",
            source = Source.BRAINTRUST,
            dateApplied = LocalDate.parse("2026-09-01"),
            jobLink = "https://stripe.com/jobs/staff-android",
            status = Status.SCREENING,
            reminderDays = 5,
            salary = "₹35 LPA"
        )

        val serialized = json.encodeToString(request)
        val deserialized = json.decodeFromString<CreateApplicationRequest>(serialized)

        assertEquals(request, deserialized)
        assertEquals("client-uuid-12345", deserialized.id)
        assertEquals("Stripe", deserialized.company)
        assertEquals("Staff Android Engineer", deserialized.role)
        assertEquals(Source.BRAINTRUST, deserialized.source)
        assertEquals(LocalDate.parse("2026-09-01"), deserialized.dateApplied)
        assertEquals("https://stripe.com/jobs/staff-android", deserialized.jobLink)
        assertEquals(Status.SCREENING, deserialized.status)
        assertEquals(5, deserialized.reminderDays)
        assertEquals("₹35 LPA", deserialized.salary)
    }

    @Test
    fun testCreateApplicationRequestWithNullIdAndNullSalary() {
        val request = CreateApplicationRequest(
            id = null,
            company = "Shopify",
            role = "Mobile Engineer",
            source = Source.TOPTAL,
            dateApplied = LocalDate.parse("2026-09-01"),
            jobLink = null,
            status = Status.APPLIED,
            reminderDays = null,
            salary = null
        )

        val serialized = json.encodeToString(request)
        val deserialized = json.decodeFromString<CreateApplicationRequest>(serialized)

        assertEquals(request, deserialized)
        assertNull(deserialized.id)
        assertNull(deserialized.jobLink)
        assertNull(deserialized.reminderDays)
        assertNull(deserialized.salary)
    }

    @Test
    fun testCreateApplicationRequestDefaultValues() {
        val request = CreateApplicationRequest(
            company = "Figma",
            role = "UI Engineer",
            source = Source.CONTRA,
            dateApplied = LocalDate.parse("2026-09-01")
        )

        assertNull(request.id)
        assertEquals(Status.APPLIED, request.status)
        assertNull(request.jobLink)
        assertNull(request.reminderDays)
        assertNull(request.salary)

        val serialized = json.encodeToString(request)
        val deserialized = json.decodeFromString<CreateApplicationRequest>(serialized)

        assertEquals(request, deserialized)
        assertNull(deserialized.id)
        assertEquals(Status.APPLIED, deserialized.status)
        assertNull(deserialized.jobLink)
        assertNull(deserialized.reminderDays)
        assertNull(deserialized.salary)
    }

    @Test
    fun testCreateNoteRequestWithClientSuppliedId() {
        val request = CreateNoteRequest(
            id = "note-uuid-custom-42",
            text = "Initial recruiter screen went great. Tech interview scheduled for next Tuesday."
        )

        val serialized = json.encodeToString(request)
        val deserialized = json.decodeFromString<CreateNoteRequest>(serialized)

        assertEquals(request, deserialized)
        assertEquals("note-uuid-custom-42", deserialized.id)
        assertEquals("Initial recruiter screen went great. Tech interview scheduled for next Tuesday.", deserialized.text)
    }

    @Test
    fun testCreateNoteRequestWithNullId() {
        val request = CreateNoteRequest(
            id = null,
            text = "Submitted take-home assignment."
        )

        val serialized = json.encodeToString(request)
        val deserialized = json.decodeFromString<CreateNoteRequest>(serialized)

        assertEquals(request, deserialized)
        assertNull(deserialized.id)
        assertEquals("Submitted take-home assignment.", deserialized.text)
    }

    @Test
    fun testCreateNoteRequestDefaultId() {
        val request = CreateNoteRequest(text = "Follow up in 3 days")

        assertNull(request.id)

        val serialized = json.encodeToString(request)
        val deserialized = json.decodeFromString<CreateNoteRequest>(serialized)

        assertEquals(request, deserialized)
        assertNull(deserialized.id)
        assertEquals("Follow up in 3 days", deserialized.text)
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
        assertEquals(Instant.parse("2026-08-31T14:30:00Z"), deserialized.createdAt)
    }

    @Test
    fun testUpdateApplicationRequestFullAndPartialSerialization() {
        val fullUpdate = UpdateApplicationRequest(
            company = "Netflix",
            role = "Lead Mobile Architect",
            source = Source.REFERRAL,
            dateApplied = LocalDate.parse("2026-09-02"),
            jobLink = "https://jobs.netflix.com/lead",
            status = Status.OFFER,
            reminderDays = 3,
            salary = "$250,000 / yr"
        )
        val fullSerialized = json.encodeToString(fullUpdate)
        val fullDeserialized = json.decodeFromString<UpdateApplicationRequest>(fullSerialized)
        assertEquals(fullUpdate, fullDeserialized)
        assertEquals("Netflix", fullDeserialized.company)
        assertEquals("$250,000 / yr", fullDeserialized.salary)
        assertEquals(Status.OFFER, fullDeserialized.status)

        val partialUpdate = UpdateApplicationRequest(
            status = Status.GHOSTED
        )
        val partialSerialized = json.encodeToString(partialUpdate)
        val partialDeserialized = json.decodeFromString<UpdateApplicationRequest>(partialSerialized)
        assertEquals(partialUpdate, partialDeserialized)
        assertEquals(Status.GHOSTED, partialDeserialized.status)
        assertNull(partialDeserialized.company)
        assertNull(partialDeserialized.salary)

        val emptyUpdate = UpdateApplicationRequest()
        val emptySerialized = json.encodeToString(emptyUpdate)
        val emptyDeserialized = json.decodeFromString<UpdateApplicationRequest>(emptySerialized)
        assertEquals(emptyUpdate, emptyDeserialized)
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

    @Test
    fun testEnumDisplayNames() {
        assertEquals("Applied", Status.APPLIED.displayName)
        assertEquals("Screening", Status.SCREENING.displayName)
        assertEquals("Interview", Status.INTERVIEW.displayName)
        assertEquals("Offer", Status.OFFER.displayName)
        assertEquals("Rejected", Status.REJECTED.displayName)
        assertEquals("Ghosted", Status.GHOSTED.displayName)

        assertEquals("Wellfound", Source.WELLFOUND.displayName)
        assertEquals("Upwork", Source.UPWORK.displayName)
        assertEquals("Contra", Source.CONTRA.displayName)
        assertEquals("Braintrust", Source.BRAINTRUST.displayName)
        assertEquals("Toptal", Source.TOPTAL.displayName)
        assertEquals("Referral", Source.REFERRAL.displayName)
        assertEquals("Other", Source.OTHER.displayName)
    }
}

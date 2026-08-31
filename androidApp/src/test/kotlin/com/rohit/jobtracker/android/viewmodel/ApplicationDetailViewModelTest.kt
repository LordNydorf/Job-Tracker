package com.rohit.jobtracker.android.viewmodel

import com.rohit.jobtracker.android.ui.detail.ApplicationDetailViewModel
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadApplicationAndChangeStatus() = runTest(testDispatcher) {
        val testApp = Application(
            id = "app-100",
            company = "Netflix",
            role = "Senior Android Eng",
            source = Source.REFERRAL,
            dateApplied = LocalDate.parse("2026-08-31"),
            status = Status.APPLIED,
            lastUpdated = Instant.parse("2026-08-31T10:00:00Z")
        )

        val api = FakeJobTrackerApi(mutableListOf(testApp))
        val viewModel = ApplicationDetailViewModel(applicationId = "app-100", api = api)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.application)
        assertEquals("Netflix", state.application?.company)
        assertEquals(Status.APPLIED, state.application?.status)

        // Update status to INTERVIEW
        viewModel.updateStatus(Status.INTERVIEW)
        advanceUntilIdle()

        assertEquals(Status.INTERVIEW, viewModel.uiState.value.application?.status)
        assertFalse(viewModel.uiState.value.isUpdatingStatus)
        assertEquals(Status.INTERVIEW, api.getApplication("app-100")?.status)
    }

    @Test
    fun testAddNoteAndTimelineFeed() = runTest(testDispatcher) {
        val testApp = Application(
            id = "app-200",
            company = "Linear",
            role = "Staff Eng",
            source = Source.OTHER,
            dateApplied = LocalDate.parse("2026-08-31"),
            status = Status.INTERVIEW,
            lastUpdated = Instant.parse("2026-08-31T10:00:00Z")
        )

        val api = FakeJobTrackerApi(mutableListOf(testApp))
        val viewModel = ApplicationDetailViewModel(applicationId = "app-200", api = api)
        advanceUntilIdle()

        viewModel.updateNewNoteText("Round 1 passed with flying colors.")
        viewModel.addNote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAddingNote)
        assertEquals("", state.newNoteText)
        assertEquals(1, state.notes.size)
        assertEquals("Round 1 passed with flying colors.", state.notes.first().text)
    }

    @Test
    fun testDeleteApplicationEmitsEvent() = runTest(testDispatcher) {
        val testApp = Application(
            id = "app-300",
            company = "Vercel",
            role = "Fullstack Eng",
            source = Source.WELLFOUND,
            dateApplied = LocalDate.parse("2026-08-31"),
            status = Status.REJECTED,
            lastUpdated = Instant.parse("2026-08-31T10:00:00Z")
        )

        val api = FakeJobTrackerApi(mutableListOf(testApp))
        val viewModel = ApplicationDetailViewModel(applicationId = "app-300", api = api)
        advanceUntilIdle()

        var deletedEmitted = false
        val job = launch {
            viewModel.deleteSuccessEvent.first()
            deletedEmitted = true
        }

        viewModel.deleteApplication()
        advanceUntilIdle()

        assertTrue(deletedEmitted)
        assertEquals(0, api.getApplications().size)

        job.cancel()
    }
}

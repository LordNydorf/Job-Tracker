package com.rohit.jobtracker.android.viewmodel

import com.rohit.jobtracker.android.ui.list.ApplicationListViewModel
import com.rohit.jobtracker.android.ui.list.SortOption
import com.rohit.jobtracker.android.ui.list.StatusFilter
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.Note
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

class FakeJobTrackerApi(
    private var applications: MutableList<Application> = mutableListOf()
) : JobTrackerApi {

    override suspend fun getApplications(): List<Application> = applications

    override suspend fun getApplication(id: String): Application? = applications.find { it.id == id }

    override suspend fun createApplication(request: CreateApplicationRequest): Application {
        val app = Application(
            id = "app-${applications.size + 1}",
            company = request.company,
            role = request.role,
            source = request.source,
            dateApplied = request.dateApplied,
            jobLink = request.jobLink,
            status = request.status,
            lastUpdated = Instant.parse("2026-08-31T10:00:00Z"),
            reminderDays = request.reminderDays,
            salary = request.salary
        )
        applications.add(app)
        return app
    }

    override suspend fun updateApplication(id: String, request: UpdateApplicationRequest): Application {
        val index = applications.indexOfFirst { it.id == id }
        val current = applications[index]
        val updated = current.copy(
            status = request.status ?: current.status,
            company = request.company ?: current.company,
            role = request.role ?: current.role,
            salary = request.salary ?: current.salary,
            reminderDays = request.reminderDays ?: current.reminderDays
        )
        applications[index] = updated
        return updated
    }

    private var notesMap: MutableMap<String, MutableList<Note>> = mutableMapOf()

    override suspend fun deleteApplication(id: String): Boolean = applications.removeIf { it.id == id }

    override suspend fun getNotes(applicationId: String): List<Note> = notesMap[applicationId] ?: emptyList()

    override suspend fun addNote(applicationId: String, request: CreateNoteRequest): Note {
        val note = Note(id = "note-${System.nanoTime()}", applicationId = applicationId, text = request.text, createdAt = Instant.parse("2026-08-31T12:00:00Z"))
        val list = notesMap.getOrPut(applicationId) { mutableListOf() }
        list.add(0, note)
        return note
    }

    override suspend fun deleteNote(applicationId: String, noteId: String): Boolean {
        val list = notesMap[applicationId] ?: return false
        return list.removeIf { it.id == noteId }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationListViewModelTest {

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
    fun testLoadApplicationsAndFilterSort() = runTest(testDispatcher) {
        val testApps = mutableListOf(
            Application(
                id = "1",
                company = "Zebra Inc",
                role = "Dev",
                source = Source.WELLFOUND,
                dateApplied = LocalDate.parse("2026-08-20"),
                status = Status.APPLIED,
                lastUpdated = Instant.parse("2026-08-20T10:00:00Z")
            ),
            Application(
                id = "2",
                company = "Alpha Labs",
                role = "Lead",
                source = Source.REFERRAL,
                dateApplied = LocalDate.parse("2026-08-25"),
                status = Status.INTERVIEW,
                lastUpdated = Instant.parse("2026-08-28T10:00:00Z")
            ),
            Application(
                id = "3",
                company = "Beta Corp",
                role = "Staff",
                source = Source.UPWORK,
                dateApplied = LocalDate.parse("2026-08-15"),
                status = Status.GHOSTED,
                lastUpdated = Instant.parse("2026-08-15T10:00:00Z")
            )
        )

        val api = FakeJobTrackerApi(testApps)
        val viewModel = ApplicationListViewModel(api)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.applications.size)
        assertEquals(3, state.filteredApplications.size)

        // Filter for INTERVIEW
        viewModel.setFilter(StatusFilter.INTERVIEW)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredApplications.size)
        assertEquals("Alpha Labs", viewModel.uiState.value.filteredApplications.first().company)

        // Filter for CLOSED (should match GHOSTED)
        viewModel.setFilter(StatusFilter.CLOSED)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredApplications.size)
        assertEquals("Beta Corp", viewModel.uiState.value.filteredApplications.first().company)

        // Reset filter and test Alphabetical Sort
        viewModel.setFilter(StatusFilter.ALL)
        viewModel.setSort(SortOption.COMPANY)
        advanceUntilIdle()
        val sortedCompanies = viewModel.uiState.value.filteredApplications.map { it.company }
        assertEquals(listOf("Alpha Labs", "Beta Corp", "Zebra Inc"), sortedCompanies)
    }
}

package com.rohit.jobtracker.android.viewmodel

import com.rohit.jobtracker.android.ui.addedit.AddEditViewModel
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
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditViewModelTest {

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
    fun testValidationFailsOnBlankCompanyAndRole() = runTest(testDispatcher) {
        val api = FakeJobTrackerApi()
        val viewModel = AddEditViewModel(api = api)

        viewModel.updateCompany("")
        viewModel.updateRole("")
        val result = viewModel.saveApplication()

        assertFalse(result)
        assertNotNull(viewModel.uiState.value.companyError)
        assertNotNull(viewModel.uiState.value.roleError)
    }

    @Test
    fun testValidateStepReturnsFalseAndSetsErrorsWhenFieldsBlank() = runTest(testDispatcher) {
        val api = FakeJobTrackerApi()
        val viewModel = AddEditViewModel(api = api)

        // Step 1 validation
        val isValid = viewModel.validateStep(1)
        assertFalse(isValid)
        assertEquals("Company name is required", viewModel.uiState.value.companyError)
        assertEquals("Job role / title is required", viewModel.uiState.value.roleError)

        // Fill company only
        viewModel.updateCompany("Linear")
        val isValid2 = viewModel.validateStep(1)
        assertFalse(isValid2)
        assertNull(viewModel.uiState.value.companyError)
        assertEquals("Job role / title is required", viewModel.uiState.value.roleError)

        // Fill role as well
        viewModel.updateRole("Product Engineer")
        val isValid3 = viewModel.validateStep(1)
        assertTrue(isValid3)
        assertNull(viewModel.uiState.value.companyError)
        assertNull(viewModel.uiState.value.roleError)
    }

    @Test
    fun testSuccessfulSaveWithCurrencyAndSalary() = runTest(testDispatcher) {
        val api = FakeJobTrackerApi()
        val viewModel = AddEditViewModel(api = api)

        viewModel.updateCompany("Stripe")
        viewModel.updateRole("Senior Kotlin Dev")
        viewModel.updateSource(Source.WELLFOUND)
        viewModel.updateStatus(Status.INTERVIEW)
        viewModel.updateDateApplied(LocalDate.parse("2026-08-31"))
        viewModel.updateJobLink("https://stripe.com/jobs/123")
        viewModel.updateReminderDays(7)
        viewModel.updateCurrency("₹")
        viewModel.updateSalary("28 LPA")

        var emitted = false
        val job = launch {
            viewModel.saveSuccessEvent.first()
            emitted = true
        }

        val result = viewModel.saveApplication()
        advanceUntilIdle()

        assertTrue(result)
        assertTrue(emitted)
        assertNull(viewModel.uiState.value.companyError)
        assertNull(viewModel.uiState.value.roleError)
        assertFalse(viewModel.uiState.value.isSaving)

        val savedApps = api.getApplications()
        assertEquals(1, savedApps.size)
        assertEquals("Stripe", savedApps.first().company)
        assertEquals("Senior Kotlin Dev", savedApps.first().role)
        assertEquals(Status.INTERVIEW, savedApps.first().status)
        assertEquals(7, savedApps.first().reminderDays)
        assertEquals("₹28 LPA", savedApps.first().salary)

        job.cancel()
    }

    @Test
    fun testEditModePrePopulatesExistingApplicationAndUpdates() = runTest(testDispatcher) {
        val api = FakeJobTrackerApi()
        val existing = api.createApplication(
            com.rohit.jobtracker.shared.model.CreateApplicationRequest(
                company = "Google",
                role = "Software Engineer",
                source = Source.REFERRAL,
                dateApplied = LocalDate.parse("2026-08-31"),
                status = Status.APPLIED,
                salary = "$150,000 / yr",
                reminderDays = 3
            )
        )

        val viewModel = AddEditViewModel(applicationId = existing.id, api = api)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEditMode)
        assertEquals("Google", viewModel.uiState.value.company)
        assertEquals("Software Engineer", viewModel.uiState.value.role)
        assertEquals("$", viewModel.uiState.value.currency)
        assertEquals("150,000 / yr", viewModel.uiState.value.salary)

        // Modify fields
        viewModel.updateRole("Staff Software Engineer")
        viewModel.updateSalary("200,000 / yr")

        var emitted = false
        val job = launch {
            viewModel.saveSuccessEvent.first()
            emitted = true
        }

        val result = viewModel.saveApplication()
        advanceUntilIdle()

        assertTrue(result)
        assertTrue(emitted)

        val updated = api.getApplication(existing.id)
        assertNotNull(updated)
        assertEquals("Staff Software Engineer", updated.role)
        assertEquals("$200,000 / yr", updated.salary)

        job.cancel()
    }
}

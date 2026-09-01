package com.rohit.jobtracker.android.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohit.jobtracker.android.cache.LocalApplicationStore
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import com.rohit.jobtracker.android.sync.MutationType
import com.rohit.jobtracker.android.sync.PendingMutation
import com.rohit.jobtracker.shared.model.Application
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

data class AddEditUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val company: String = "",
    val role: String = "",
    val source: Source = Source.WELLFOUND,
    val dateApplied: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val jobLink: String = "",
    val status: Status = Status.APPLIED,
    val reminderDays: Int? = 7,
    val salary: String = "",
    val currency: String = "$",
    val isSaving: Boolean = false,
    val companyError: String? = null,
    val roleError: String? = null,
    val generalError: String? = null
)

class AddEditViewModel(
    private val applicationId: String? = null,
    private val api: JobTrackerApi,
    private val localStore: LocalApplicationStore? = null
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _uiState = MutableStateFlow(
        AddEditUiState(isEditMode = !applicationId.isNullOrBlank())
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    init {
        if (!applicationId.isNullOrBlank()) {
            val cached = localStore?.getCachedApplication(applicationId)
            if (cached != null) {
                val (cur, sal) = parseCurrencyAndSalary(cached.salary)
                _uiState.update {
                    it.copy(
                        isEditMode = true,
                        company = cached.company,
                        role = cached.role,
                        source = cached.source,
                        dateApplied = cached.dateApplied,
                        jobLink = cached.jobLink ?: "",
                        status = cached.status,
                        reminderDays = cached.reminderDays,
                        currency = cur,
                        salary = sal
                    )
                }
            }
            loadExistingApplication(applicationId)
        }
    }

    private fun loadExistingApplication(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val app = api.getApplication(id)
                if (app != null) {
                    val (detectedCurrency, detectedSalary) = parseCurrencyAndSalary(app.salary)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            company = app.company,
                            role = app.role,
                            source = app.source,
                            dateApplied = app.dateApplied,
                            jobLink = app.jobLink ?: "",
                            status = app.status,
                            reminderDays = app.reminderDays,
                            currency = detectedCurrency,
                            salary = detectedSalary
                        )
                    }
                    localStore?.saveOrUpdateApplication(app)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = "Application not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun parseCurrencyAndSalary(rawSalary: String?): Pair<String, String> {
        if (rawSalary.isNullOrBlank()) return Pair("$", "")
        val trimmed = rawSalary.trim()
        val knownCurrencies = listOf("₹", "€", "£", "AED", "CA$", "A$", "S$", "$")
        for (c in knownCurrencies) {
            if (trimmed.startsWith(c, ignoreCase = true)) {
                val rest = trimmed.substring(c.length).trim()
                return Pair(c, rest)
            }
        }
        return Pair("$", trimmed)
    }

    fun updateCompany(value: String) {
        _uiState.update { it.copy(company = value, companyError = null) }
    }

    fun updateRole(value: String) {
        _uiState.update { it.copy(role = value, roleError = null) }
    }

    fun updateSource(value: Source) {
        _uiState.update { it.copy(source = value) }
    }

    fun updateDateApplied(value: LocalDate) {
        _uiState.update { it.copy(dateApplied = value) }
    }

    fun updateJobLink(value: String) {
        _uiState.update { it.copy(jobLink = value) }
    }

    fun updateStatus(value: Status) {
        _uiState.update { it.copy(status = value) }
    }

    fun updateReminderDays(value: Int?) {
        _uiState.update { it.copy(reminderDays = value) }
    }

    fun updateSalary(value: String) {
        _uiState.update { it.copy(salary = value) }
    }

    fun updateCurrency(value: String) {
        _uiState.update { it.copy(currency = value) }
    }

    fun saveApplication(): Boolean {
        val state = _uiState.value
        var hasError = false

        if (state.company.isBlank()) {
            _uiState.update { it.copy(companyError = "Company name is required") }
            hasError = true
        }

        if (state.role.isBlank()) {
            _uiState.update { it.copy(roleError = "Role is required") }
            hasError = true
        }

        if (hasError) return false

        val formattedSalary = if (state.salary.isNotBlank()) {
            val sal = state.salary.trim()
            val hasSymbol = listOf("$", "₹", "€", "£", "AED", "CA$", "A$", "S$").any { sal.startsWith(it, ignoreCase = true) }
            if (hasSymbol) sal else "${state.currency}$sal"
        } else {
            null
        }

        val now = Clock.System.now()

        if (state.isEditMode && !applicationId.isNullOrBlank()) {
            val updateReq = UpdateApplicationRequest(
                company = state.company.trim(),
                role = state.role.trim(),
                source = state.source,
                dateApplied = state.dateApplied,
                jobLink = state.jobLink.trim().takeIf { it.isNotEmpty() },
                status = state.status,
                reminderDays = state.reminderDays,
                salary = formattedSalary
            )

            // Optimistic local update
            val existing = localStore?.getCachedApplication(applicationId)
            val updatedApp = Application(
                id = applicationId,
                company = state.company.trim(),
                role = state.role.trim(),
                source = state.source,
                dateApplied = state.dateApplied,
                jobLink = state.jobLink.trim().takeIf { it.isNotEmpty() },
                status = state.status,
                lastUpdated = now,
                reminderDays = state.reminderDays,
                salary = formattedSalary
            )
            localStore?.saveOrUpdateApplication(updatedApp)

            val mutation = PendingMutation(
                id = UUID.randomUUID().toString(),
                type = MutationType.UPDATE_APP,
                entityId = applicationId,
                payloadJson = json.encodeToString(updateReq),
                createdAt = System.currentTimeMillis()
            )
            localStore?.enqueueMutation(mutation)

            viewModelScope.launch {
                _saveSuccessEvent.emit(Unit)
                try {
                    val serverApp = api.updateApplication(applicationId, updateReq)
                    localStore?.saveOrUpdateApplication(serverApp)
                    localStore?.removeMutation(mutation.id)
                } catch (_: Exception) {
                    // SyncWorker will drain mutation in background
                }
            }
        } else {
            val targetId = UUID.randomUUID().toString()
            val createReq = CreateApplicationRequest(
                id = targetId,
                company = state.company.trim(),
                role = state.role.trim(),
                source = state.source,
                dateApplied = state.dateApplied,
                jobLink = state.jobLink.trim().takeIf { it.isNotEmpty() },
                status = state.status,
                reminderDays = state.reminderDays,
                salary = formattedSalary
            )

            // Optimistic local save
            val newApp = Application(
                id = targetId,
                company = state.company.trim(),
                role = state.role.trim(),
                source = state.source,
                dateApplied = state.dateApplied,
                jobLink = state.jobLink.trim().takeIf { it.isNotEmpty() },
                status = state.status,
                lastUpdated = now,
                reminderDays = state.reminderDays,
                salary = formattedSalary
            )
            localStore?.saveOrUpdateApplication(newApp)

            val mutation = PendingMutation(
                id = UUID.randomUUID().toString(),
                type = MutationType.CREATE_APP,
                entityId = targetId,
                payloadJson = json.encodeToString(createReq),
                createdAt = System.currentTimeMillis()
            )
            localStore?.enqueueMutation(mutation)

            viewModelScope.launch {
                _saveSuccessEvent.emit(Unit)
                try {
                    val created = api.createApplication(createReq)
                    localStore?.saveOrUpdateApplication(created)
                    localStore?.removeMutation(mutation.id)
                } catch (_: Exception) {
                    // SyncWorker will drain mutation in background
                }
            }
        }
        return true
    }
}

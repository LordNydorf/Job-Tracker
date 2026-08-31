package com.rohit.jobtracker.android.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.CreateApplicationRequest
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
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

data class AddEditUiState(
    val company: String = "",
    val role: String = "",
    val source: Source = Source.WELLFOUND,
    val dateApplied: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val jobLink: String = "",
    val status: Status = Status.APPLIED,
    val reminderDays: String = "7",
    val isSaving: Boolean = false,
    val companyError: String? = null,
    val roleError: String? = null,
    val generalError: String? = null
)

class AddEditViewModel(
    private val api: JobTrackerApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

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

    fun updateReminderDays(value: String) {
        _uiState.update { it.copy(reminderDays = value) }
    }

    fun saveApplication() {
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

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, generalError = null) }
            try {
                val reminderInt = state.reminderDays.toIntOrNull()
                val request = CreateApplicationRequest(
                    company = state.company.trim(),
                    role = state.role.trim(),
                    source = state.source,
                    dateApplied = state.dateApplied,
                    jobLink = state.jobLink.trim().takeIf { it.isNotEmpty() },
                    status = state.status,
                    reminderDays = reminderInt
                )
                api.createApplication(request)
                _uiState.update { it.copy(isSaving = false) }
                _saveSuccessEvent.emit(Unit)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        generalError = e.message ?: "Failed to save application"
                    )
                }
            }
        }
    }
}

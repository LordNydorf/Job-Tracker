package com.rohit.jobtracker.android.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.CreateNoteRequest
import com.rohit.jobtracker.shared.model.Note
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApplicationDetailUiState(
    val isLoading: Boolean = true,
    val application: Application? = null,
    val notes: List<Note> = emptyList(),
    val isAddingNote: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val isDeleting: Boolean = false,
    val newNoteText: String = "",
    val errorMessage: String? = null
)

class ApplicationDetailViewModel(
    private val applicationId: String,
    private val api: JobTrackerApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationDetailUiState())
    val uiState: StateFlow<ApplicationDetailUiState> = _uiState.asStateFlow()

    private val _deleteSuccessEvent = MutableSharedFlow<Unit>()
    val deleteSuccessEvent: SharedFlow<Unit> = _deleteSuccessEvent.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val app = api.getApplication(applicationId)
                val notes = api.getNotes(applicationId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        application = app,
                        notes = notes,
                        errorMessage = if (app == null) "Application not found" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load application details"
                    )
                }
            }
        }
    }

    fun updateStatus(newStatus: Status) {
        val currentApp = _uiState.value.application ?: return
        if (currentApp.status == newStatus) return

        _uiState.update {
            it.copy(
                application = currentApp.copy(status = newStatus),
                isUpdatingStatus = true
            )
        }

        viewModelScope.launch {
            try {
                val updated = api.updateApplication(applicationId, UpdateApplicationRequest(status = newStatus))
                _uiState.update { it.copy(application = updated, isUpdatingStatus = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        application = currentApp,
                        isUpdatingStatus = false,
                        errorMessage = "Failed to update status: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateNewNoteText(text: String) {
        _uiState.update { it.copy(newNoteText = text) }
    }

    fun addNote() {
        val text = _uiState.value.newNoteText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingNote = true) }
            try {
                val newNote = api.addNote(applicationId, CreateNoteRequest(text = text))
                _uiState.update {
                    it.copy(
                        isAddingNote = false,
                        newNoteText = "",
                        notes = listOf(newNote) + it.notes
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingNote = false,
                        errorMessage = "Failed to add note: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteApplication() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                val success = api.deleteApplication(applicationId)
                if (success) {
                    _deleteSuccessEvent.emit(Unit)
                } else {
                    _uiState.update { it.copy(isDeleting = false, errorMessage = "Failed to delete application") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "Failed to delete application: ${e.message}"
                    )
                }
            }
        }
    }
}

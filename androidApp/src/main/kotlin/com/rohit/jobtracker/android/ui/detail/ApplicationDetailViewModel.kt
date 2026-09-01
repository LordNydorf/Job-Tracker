package com.rohit.jobtracker.android.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohit.jobtracker.android.cache.LocalApplicationStore
import com.rohit.jobtracker.android.network.ServerConfig
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
    val currentServerUrl: String = "",
    val currentApiKey: String = "",
    val errorMessage: String? = null
)

class ApplicationDetailViewModel(
    private val applicationId: String,
    private val api: JobTrackerApi,
    private val localStore: LocalApplicationStore? = null,
    private val serverConfig: ServerConfig? = null
) : ViewModel() {

    private val cachedApp = localStore?.getCachedApplication(applicationId)
    private val cachedNotes = localStore?.getCachedNotes(applicationId) ?: emptyList()

    private val _uiState = MutableStateFlow(
        ApplicationDetailUiState(
            isLoading = cachedApp == null,
            application = cachedApp,
            notes = cachedNotes,
            currentServerUrl = serverConfig?.getBaseUrl() ?: ServerConfig.PRESETS.first().url,
            currentApiKey = serverConfig?.getApiKey() ?: ""
        )
    )
    val uiState: StateFlow<ApplicationDetailUiState> = _uiState.asStateFlow()

    private val _deleteSuccessEvent = MutableSharedFlow<Unit>()
    val deleteSuccessEvent: SharedFlow<Unit> = _deleteSuccessEvent.asSharedFlow()

    init {
        loadData()
    }

    fun updateServerConfig(newUrl: String, newApiKey: String? = null) {
        serverConfig?.updateConfig(newUrl, newApiKey)
        _uiState.update {
            it.copy(
                currentServerUrl = newUrl.trim(),
                currentApiKey = newApiKey?.trim() ?: it.currentApiKey
            )
        }
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            if (_uiState.value.application == null) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            try {
                val app = api.getApplication(applicationId)
                val notes = api.getNotes(applicationId)
                if (app != null) {
                    localStore?.saveOrUpdateApplication(app)
                    localStore?.saveNotes(applicationId, notes)
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        application = app,
                        notes = notes,
                        errorMessage = if (app == null) "Application not found on server" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (it.application == null) e.message ?: "Failed to load application details" else null
                    )
                }
            }
        }
    }

    fun updateStatus(newStatus: Status) {
        val currentApp = _uiState.value.application ?: return
        if (currentApp.status == newStatus) return

        val updatedApp = currentApp.copy(status = newStatus)
        localStore?.saveOrUpdateApplication(updatedApp)

        _uiState.update {
            it.copy(
                application = updatedApp,
                isUpdatingStatus = true
            )
        }

        viewModelScope.launch {
            try {
                val updated = api.updateApplication(applicationId, UpdateApplicationRequest(status = newStatus))
                localStore?.saveOrUpdateApplication(updated)
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
                localStore?.addCachedNote(applicationId, newNote)
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

    fun deleteNote(noteId: String) {
        val currentNotes = _uiState.value.notes
        val updatedNotes = currentNotes.filter { it.id != noteId }
        localStore?.deleteCachedNote(applicationId, noteId)
        _uiState.update { it.copy(notes = updatedNotes) }

        viewModelScope.launch {
            try {
                val success = api.deleteNote(applicationId, noteId)
                if (!success) {
                    _uiState.update {
                        it.copy(
                            notes = currentNotes,
                            errorMessage = "Failed to delete note"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        notes = currentNotes,
                        errorMessage = "Failed to delete note: ${e.message}"
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
                    localStore?.deleteCachedApplication(applicationId)
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

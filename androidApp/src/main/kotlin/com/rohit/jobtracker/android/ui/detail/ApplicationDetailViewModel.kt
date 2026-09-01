package com.rohit.jobtracker.android.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohit.jobtracker.android.cache.LocalApplicationStore
import com.rohit.jobtracker.android.network.ServerConfig
import com.rohit.jobtracker.android.sync.MutationType
import com.rohit.jobtracker.android.sync.PendingMutation
import com.rohit.jobtracker.android.ui.theme.ThemeConfig
import com.rohit.jobtracker.android.ui.theme.ThemeMode
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
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

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
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val errorMessage: String? = null
)

class ApplicationDetailViewModel(
    private val applicationId: String,
    private val api: JobTrackerApi,
    private val localStore: LocalApplicationStore? = null,
    private val serverConfig: ServerConfig? = null,
    private val themeConfig: ThemeConfig? = null
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val cachedApp = localStore?.getCachedApplication(applicationId)
    private val cachedNotes = localStore?.getCachedNotes(applicationId) ?: emptyList()

    private val _uiState = MutableStateFlow(
        ApplicationDetailUiState(
            isLoading = cachedApp == null,
            application = cachedApp,
            notes = cachedNotes,
            currentServerUrl = serverConfig?.getBaseUrl() ?: ServerConfig.PRESETS.first().url,
            currentApiKey = serverConfig?.getApiKey() ?: "",
            themeMode = themeConfig?.getThemeMode() ?: ThemeMode.SYSTEM
        )
    )
    val uiState: StateFlow<ApplicationDetailUiState> = _uiState.asStateFlow()

    private val _deleteSuccessEvent = MutableSharedFlow<Unit>()
    val deleteSuccessEvent: SharedFlow<Unit> = _deleteSuccessEvent.asSharedFlow()

    init {
        themeConfig?.themeMode?.let { modeFlow ->
            viewModelScope.launch {
                modeFlow.collect { mode ->
                    _uiState.update { it.copy(themeMode = mode) }
                }
            }
        }
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

    fun setThemeMode(mode: ThemeMode) {
        themeConfig?.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
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

        val now = Clock.System.now()
        val updatedApp = currentApp.copy(status = newStatus, lastUpdated = now)
        localStore?.saveOrUpdateApplication(updatedApp)

        _uiState.update {
            it.copy(
                application = updatedApp,
                isUpdatingStatus = false
            )
        }

        val updateReq = UpdateApplicationRequest(status = newStatus)
        val mutation = PendingMutation(
            id = UUID.randomUUID().toString(),
            type = MutationType.UPDATE_APP,
            entityId = applicationId,
            payloadJson = json.encodeToString(updateReq),
            createdAt = System.currentTimeMillis()
        )
        localStore?.enqueueMutation(mutation)

        viewModelScope.launch {
            try {
                val serverApp = api.updateApplication(applicationId, updateReq)
                localStore?.saveOrUpdateApplication(serverApp)
                localStore?.removeMutation(mutation.id)
            } catch (_: Exception) {
                // Background SyncWorker will retry
            }
        }
    }

    fun updateNewNoteText(text: String) {
        _uiState.update { it.copy(newNoteText = text) }
    }

    fun addNote() {
        val text = _uiState.value.newNoteText.trim()
        if (text.isEmpty()) return

        val targetNoteId = UUID.randomUUID().toString()
        val now = Clock.System.now()
        val localNote = Note(
            id = targetNoteId,
            applicationId = applicationId,
            text = text,
            createdAt = now
        )

        localStore?.addCachedNote(applicationId, localNote)
        _uiState.update {
            it.copy(
                newNoteText = "",
                notes = listOf(localNote) + it.notes
            )
        }

        val createNoteReq = CreateNoteRequest(id = targetNoteId, text = text)
        val mutation = PendingMutation(
            id = UUID.randomUUID().toString(),
            type = MutationType.ADD_NOTE,
            entityId = targetNoteId,
            parentEntityId = applicationId,
            payloadJson = json.encodeToString(createNoteReq),
            createdAt = System.currentTimeMillis()
        )
        localStore?.enqueueMutation(mutation)

        viewModelScope.launch {
            try {
                val serverNote = api.addNote(applicationId, createNoteReq)
                localStore?.addCachedNote(applicationId, serverNote)
                localStore?.removeMutation(mutation.id)
            } catch (_: Exception) {
                // Background SyncWorker will retry
            }
        }
    }

    fun deleteNote(noteId: String) {
        val currentNotes = _uiState.value.notes
        val updatedNotes = currentNotes.filter { it.id != noteId }
        localStore?.deleteCachedNote(applicationId, noteId)
        _uiState.update { it.copy(notes = updatedNotes) }

        val mutation = PendingMutation(
            id = UUID.randomUUID().toString(),
            type = MutationType.DELETE_NOTE,
            entityId = noteId,
            parentEntityId = applicationId,
            createdAt = System.currentTimeMillis()
        )
        localStore?.enqueueMutation(mutation)

        viewModelScope.launch {
            try {
                val success = api.deleteNote(applicationId, noteId)
                if (success) {
                    localStore?.removeMutation(mutation.id)
                }
            } catch (_: Exception) {
                // Background SyncWorker will retry
            }
        }
    }

    fun deleteApplication() {
        localStore?.deleteCachedApplication(applicationId)
        val mutation = PendingMutation(
            id = UUID.randomUUID().toString(),
            type = MutationType.DELETE_APP,
            entityId = applicationId,
            createdAt = System.currentTimeMillis()
        )
        localStore?.enqueueMutation(mutation)

        viewModelScope.launch {
            _deleteSuccessEvent.emit(Unit)
            try {
                val success = api.deleteApplication(applicationId)
                if (success) {
                    localStore?.removeMutation(mutation.id)
                }
            } catch (_: Exception) {
                // Background SyncWorker will retry
            }
        }
    }
}

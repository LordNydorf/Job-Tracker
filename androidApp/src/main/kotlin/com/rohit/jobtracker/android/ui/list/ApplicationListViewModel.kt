package com.rohit.jobtracker.android.ui.list

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
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

enum class StatusFilter(val displayName: String) {
    ALL("All"),
    APPLIED("Applied"),
    SCREENING("Screening"),
    INTERVIEW("Interview"),
    OFFER("Offer"),
    CLOSED("Closed")
}

enum class SortOption(val displayName: String) {
    LAST_UPDATED("Last Updated"),
    DATE_APPLIED("Date Applied"),
    COMPANY("Company (A-Z)")
}

data class ApplicationListUiState(
    val isLoading: Boolean = false,
    val applications: List<Application> = emptyList(),
    val filteredApplications: List<Application> = emptyList(),
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val sortOption: SortOption = SortOption.LAST_UPDATED,
    val searchQuery: String = "",
    val currentServerUrl: String = "",
    val currentApiKey: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val pendingMutationsCount: Int = 0,
    val errorMessage: String? = null
)

class ApplicationListViewModel(
    private val api: JobTrackerApi,
    private val localStore: LocalApplicationStore? = null,
    private val serverConfig: ServerConfig? = null,
    private val themeConfig: ThemeConfig? = null
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val cached = localStore?.getCachedApplications() ?: emptyList()

    private val _uiState = MutableStateFlow(
        ApplicationListUiState(
            isLoading = cached.isEmpty(),
            applications = cached,
            filteredApplications = applyFilterAndSort(cached, StatusFilter.ALL, SortOption.LAST_UPDATED, ""),
            currentServerUrl = serverConfig?.getBaseUrl() ?: ServerConfig.PRESETS.first().url,
            currentApiKey = serverConfig?.getApiKey() ?: "",
            themeMode = themeConfig?.getThemeMode() ?: ThemeMode.SYSTEM,
            pendingMutationsCount = localStore?.pendingMutationsCount?.value ?: 0
        )
    )
    val uiState: StateFlow<ApplicationListUiState> = _uiState.asStateFlow()

    init {
        themeConfig?.themeMode?.let { modeFlow ->
            viewModelScope.launch {
                modeFlow.collect { mode ->
                    _uiState.update { it.copy(themeMode = mode) }
                }
            }
        }
        localStore?.pendingMutationsCount?.let { countFlow ->
            viewModelScope.launch {
                countFlow.collect { count ->
                    _uiState.update { it.copy(pendingMutationsCount = count) }
                }
            }
        }
        localStore?.applicationsFlow?.let { appsFlow ->
            viewModelScope.launch {
                appsFlow.collect { apps ->
                    _uiState.update { state ->
                        val filtered = applyFilterAndSort(apps, state.statusFilter, state.sortOption, state.searchQuery)
                        state.copy(
                            applications = apps,
                            filteredApplications = filtered,
                            isLoading = false
                        )
                    }
                }
            }
        }
        loadApplications()
    }

    fun loadApplications() {
        viewModelScope.launch {
            if (_uiState.value.applications.isEmpty()) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            try {
                val list = api.getApplications()
                localStore?.saveApplications(list)
                _uiState.update { state ->
                    val filtered = applyFilterAndSort(list, state.statusFilter, state.sortOption, state.searchQuery)
                    state.copy(
                        isLoading = false,
                        applications = list,
                        filteredApplications = filtered,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (it.applications.isEmpty()) e.message ?: "Unable to connect to backend server." else null
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.applications, state.statusFilter, state.sortOption, query)
            state.copy(searchQuery = query, filteredApplications = filtered)
        }
    }

    fun updateServerConfig(newUrl: String, newApiKey: String? = null) {
        serverConfig?.updateConfig(newUrl, newApiKey)
        _uiState.update {
            it.copy(
                currentServerUrl = newUrl.trim(),
                currentApiKey = newApiKey?.trim() ?: it.currentApiKey
            )
        }
        loadApplications()
    }

    fun setThemeMode(mode: ThemeMode) {
        themeConfig?.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setFilter(filter: StatusFilter) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.applications, filter, state.sortOption, state.searchQuery)
            state.copy(statusFilter = filter, filteredApplications = filtered)
        }
    }

    fun setSort(sort: SortOption) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.applications, state.statusFilter, sort, state.searchQuery)
            state.copy(sortOption = sort, filteredApplications = filtered)
        }
    }

    fun quickUpdateStatus(applicationId: String, newStatus: Status) {
        val currentApps = _uiState.value.applications
        val targetIndex = currentApps.indexOfFirst { it.id == applicationId }
        if (targetIndex == -1) return

        val updatedList = currentApps.toMutableList()
        val updatedApp = updatedList[targetIndex].copy(status = newStatus)
        updatedList[targetIndex] = updatedApp
        localStore?.saveOrUpdateApplication(updatedApp)

        _uiState.update { state ->
            val filtered = applyFilterAndSort(updatedList, state.statusFilter, state.sortOption, state.searchQuery)
            state.copy(applications = updatedList, filteredApplications = filtered)
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
                // SyncWorker will drain in background
            }
        }
    }

    private fun applyFilterAndSort(
        list: List<Application>,
        filter: StatusFilter,
        sort: SortOption,
        query: String
    ): List<Application> {
        val uniqueList = list.distinctBy { it.id }
        val queryFiltered = if (query.isBlank()) {
            uniqueList
        } else {
            val q = query.trim().lowercase()
            uniqueList.filter {
                it.company.lowercase().contains(q) ||
                        it.role.lowercase().contains(q) ||
                        it.source.displayName.lowercase().contains(q)
            }
        }

        val statusFiltered = when (filter) {
            StatusFilter.ALL -> queryFiltered
            StatusFilter.APPLIED -> queryFiltered.filter { it.status == Status.APPLIED }
            StatusFilter.SCREENING -> queryFiltered.filter { it.status == Status.SCREENING }
            StatusFilter.INTERVIEW -> queryFiltered.filter { it.status == Status.INTERVIEW }
            StatusFilter.OFFER -> queryFiltered.filter { it.status == Status.OFFER }
            StatusFilter.CLOSED -> queryFiltered.filter { it.status == Status.REJECTED || it.status == Status.GHOSTED }
        }

        return when (sort) {
            SortOption.LAST_UPDATED -> statusFiltered.sortedByDescending { it.lastUpdated }
            SortOption.DATE_APPLIED -> statusFiltered.sortedByDescending { it.dateApplied }
            SortOption.COMPANY -> statusFiltered.sortedBy { it.company.lowercase() }
        }
    }
}

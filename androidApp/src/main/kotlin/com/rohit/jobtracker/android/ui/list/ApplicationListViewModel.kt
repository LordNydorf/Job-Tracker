package com.rohit.jobtracker.android.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohit.jobtracker.android.network.ServerConfig
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Status
import com.rohit.jobtracker.shared.model.UpdateApplicationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val errorMessage: String? = null
)

class ApplicationListViewModel(
    private val api: JobTrackerApi,
    private val serverConfig: ServerConfig? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ApplicationListUiState(
            isLoading = true,
            currentServerUrl = serverConfig?.getBaseUrl() ?: ServerConfig.PRESETS.first().url,
            currentApiKey = serverConfig?.getApiKey() ?: ""
        )
    )
    val uiState: StateFlow<ApplicationListUiState> = _uiState.asStateFlow()

    init {
        loadApplications()
    }

    fun loadApplications() {
        viewModelScope.launch {
            if (_uiState.value.applications.isEmpty()) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            try {
                val list = api.getApplications()
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

        _uiState.update { state ->
            val filtered = applyFilterAndSort(updatedList, state.statusFilter, state.sortOption, state.searchQuery)
            state.copy(applications = updatedList, filteredApplications = filtered)
        }

        viewModelScope.launch {
            try {
                api.updateApplication(applicationId, UpdateApplicationRequest(status = newStatus))
            } catch (e: Exception) {
                loadApplications() // Rollback on network failure
            }
        }
    }

    private fun applyFilterAndSort(
        list: List<Application>,
        filter: StatusFilter,
        sort: SortOption,
        query: String
    ): List<Application> {
        val queryFiltered = if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter {
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

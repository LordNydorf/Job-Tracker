package com.rohit.jobtracker.android.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohit.jobtracker.shared.api.JobTrackerApi
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Status
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StatusFilter {
    ALL,
    APPLIED,
    SCREENING,
    INTERVIEW,
    OFFER,
    CLOSED
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
    val errorMessage: String? = null
)

class ApplicationListViewModel(
    private val api: JobTrackerApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationListUiState(isLoading = true))
    val uiState: StateFlow<ApplicationListUiState> = _uiState.asStateFlow()

    init {
        loadApplications()
    }

    fun loadApplications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val list = api.getApplications()
                _uiState.update { state ->
                    val filtered = applyFilterAndSort(list, state.statusFilter, state.sortOption)
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
                        errorMessage = e.message ?: "Unable to load applications. Is the backend server running?"
                    )
                }
            }
        }
    }

    fun setFilter(filter: StatusFilter) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.applications, filter, state.sortOption)
            state.copy(statusFilter = filter, filteredApplications = filtered)
        }
    }

    fun setSort(sort: SortOption) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.applications, state.statusFilter, sort)
            state.copy(sortOption = sort, filteredApplications = filtered)
        }
    }

    private fun applyFilterAndSort(
        list: List<Application>,
        filter: StatusFilter,
        sort: SortOption
    ): List<Application> {
        val filtered = when (filter) {
            StatusFilter.ALL -> list
            StatusFilter.APPLIED -> list.filter { it.status == Status.APPLIED }
            StatusFilter.SCREENING -> list.filter { it.status == Status.SCREENING }
            StatusFilter.INTERVIEW -> list.filter { it.status == Status.INTERVIEW }
            StatusFilter.OFFER -> list.filter { it.status == Status.OFFER }
            StatusFilter.CLOSED -> list.filter { it.status == Status.REJECTED || it.status == Status.GHOSTED }
        }

        return when (sort) {
            SortOption.LAST_UPDATED -> filtered.sortedByDescending { it.lastUpdated }
            SortOption.DATE_APPLIED -> filtered.sortedByDescending { it.dateApplied }
            SortOption.COMPANY -> filtered.sortedBy { it.company.lowercase() }
        }
    }
}

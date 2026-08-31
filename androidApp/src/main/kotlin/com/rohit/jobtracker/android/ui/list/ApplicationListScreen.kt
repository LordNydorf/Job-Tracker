package com.rohit.jobtracker.android.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rohit.jobtracker.android.ui.components.PipelineDashboardCard
import com.rohit.jobtracker.android.ui.components.StatusBadge
import com.rohit.jobtracker.android.ui.theme.getCompanyAvatarBrush
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Status
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ApplicationListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf("") }

    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Backend Server URL") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Set the Ktor backend address for physical device or emulator testing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("Server Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Quick Presets:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = { serverUrlInput = "http://192.168.0.164:8080" },
                            label = { Text("Wi-Fi IP") }
                        )
                        SuggestionChip(
                            onClick = { serverUrlInput = "http://127.0.0.1:8080" },
                            label = { Text("USB (127.0.0.1)") }
                        )
                        SuggestionChip(
                            onClick = { serverUrlInput = "http://10.0.2.2:8080" },
                            label = { Text("Emulator") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (serverUrlInput.isNotBlank()) {
                        viewModel.updateServerUrl(serverUrlInput)
                    }
                    showServerDialog = false
                }) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Job Tracker",
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.6).sp
                        )
                        if (uiState.applications.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${uiState.filteredApplications.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        serverUrlInput = uiState.currentServerUrl
                        showServerDialog = true
                    }) {
                        Icon(Icons.Default.Dns, contentDescription = "Change server URL")
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort applications")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            fontWeight = if (uiState.sortOption == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSort(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.loadApplications() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh applications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new application", modifier = Modifier.size(26.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Error banner if any
            uiState.errorMessage?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(onClick = { viewModel.loadApplications() }) {
                                Text("Retry")
                            }
                            Button(onClick = {
                                serverUrlInput = uiState.currentServerUrl
                                showServerDialog = true
                            }) {
                                Text("Change Server URL")
                            }
                        }
                    }
                }
            }

            // Real-Time Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search company, role, or source...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ),
                singleLine = true
            )

            // Pipeline Radar Card
            if (uiState.applications.isNotEmpty()) {
                PipelineDashboardCard(applications = uiState.applications)
            }

            // Status Filter Chips Row with Counters
            val filterScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filterScrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusFilter.entries.forEach { filter ->
                    val isSelected = uiState.statusFilter == filter
                    val count = when (filter) {
                        StatusFilter.ALL -> uiState.applications.size
                        StatusFilter.APPLIED -> uiState.applications.count { it.status == Status.APPLIED }
                        StatusFilter.SCREENING -> uiState.applications.count { it.status == Status.SCREENING }
                        StatusFilter.INTERVIEW -> uiState.applications.count { it.status == Status.INTERVIEW }
                        StatusFilter.OFFER -> uiState.applications.count { it.status == Status.OFFER }
                        StatusFilter.CLOSED -> uiState.applications.count { it.status == Status.REJECTED || it.status == Status.GHOSTED }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(
                                text = "${filter.name} ($count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Main Content Area
            when {
                uiState.isLoading && uiState.applications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }

                uiState.filteredApplications.isEmpty() -> {
                    EmptyState(
                        isFiltered = (uiState.statusFilter != StatusFilter.ALL || uiState.searchQuery.isNotEmpty()) && uiState.applications.isNotEmpty(),
                        onAddClicked = onNavigateToAdd,
                        onClearFilter = {
                            viewModel.setFilter(StatusFilter.ALL)
                            viewModel.updateSearchQuery("")
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredApplications,
                            key = { it.id }
                        ) { application ->
                            ApplicationCard(
                                application = application,
                                onClick = { onNavigateToDetail(application.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(
    application: Application,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val avatarBrush = getCompanyAvatarBrush(application.company)
    val initial = application.company.firstOrNull()?.uppercaseChar()?.toString() ?: "J"
    val now = Clock.System.now()
    val daysSinceUpdate = (now - application.lastUpdated).inWholeDays
    val isOverdue = (application.status == Status.APPLIED || application.status == Status.SCREENING || application.status == Status.INTERVIEW) &&
            application.reminderDays != null && daysSinceUpdate >= application.reminderDays!!

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gradient Company Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(avatarBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.company,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = application.role,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = application.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = application.source.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Nudge due",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatRelativeTime(application.lastUpdated),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    isFiltered: Boolean,
    onAddClicked: () -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkOutline,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = if (isFiltered) "No matching applications" else "No applications yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isFiltered) "Try adjusting your search query or reset filter." else "Track your job hunt, follow-ups, and interview notes in one place.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (isFiltered) {
                Button(onClick = onClearFilter, shape = RoundedCornerShape(14.dp)) {
                    Text("Show All Applications")
                }
            } else {
                Button(onClick = onAddClicked, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add First Application")
                }
            }
        }
    }
}

fun formatRelativeTime(instant: Instant): String {
    val now = Clock.System.now()
    val diffSeconds = (now - instant).inWholeSeconds

    return when {
        diffSeconds < 60 -> "Just now"
        diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
        diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
        diffSeconds < 604800 -> "${diffSeconds / 86400}d ago"
        else -> "${diffSeconds / 604800}w ago"
    }
}

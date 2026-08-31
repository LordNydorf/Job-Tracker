package com.rohit.jobtracker.android.ui.addedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditApplicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var sourceExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collect {
            onNavigateBack()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                        viewModel.updateDateApplied(localDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Application", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General error banner if any
            uiState.generalError?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Section 1: Role & Company
            Text(
                text = "Target Role & Company",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )

            OutlinedTextField(
                value = uiState.company,
                onValueChange = { viewModel.updateCompany(it) },
                label = { Text("Company Name *") },
                placeholder = { Text("e.g. Linear, Stripe, Google") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                isError = uiState.companyError != null,
                supportingText = uiState.companyError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.role,
                onValueChange = { viewModel.updateRole(it) },
                label = { Text("Job Role / Title *") },
                placeholder = { Text("e.g. Senior Android Engineer") },
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                isError = uiState.roleError != null,
                supportingText = uiState.roleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Section 2: Tracking Details
            Text(
                text = "Source & Status",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )

            // Source Dropdown
            ExposedDropdownMenuBox(
                expanded = sourceExpanded,
                onExpandedChange = { sourceExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.source.name,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Application Source") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(
                    expanded = sourceExpanded,
                    onDismissRequest = { sourceExpanded = false }
                ) {
                    Source.entries.forEach { src ->
                        DropdownMenuItem(
                            text = { Text(src.name, fontWeight = FontWeight.Medium) },
                            onClick = {
                                viewModel.updateSource(src)
                                sourceExpanded = false
                            }
                        )
                    }
                }
            }

            // Status Dropdown
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.status.name,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Initial Pipeline Stage") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    Status.entries.forEach { st ->
                        DropdownMenuItem(
                            text = { Text(st.name, fontWeight = FontWeight.Medium) },
                            onClick = {
                                viewModel.updateStatus(st)
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            // Date Applied with DatePicker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = uiState.dateApplied.toString(),
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text("Date Applied (Tap to choose)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Job Link (Optional)
            OutlinedTextField(
                value = uiState.jobLink,
                onValueChange = { viewModel.updateJobLink(it) },
                label = { Text("Job Posting Link (Optional)") },
                placeholder = { Text("https://...") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Section 3: Follow-up Nudges with Quick Preset Chips
            Text(
                text = "Follow-up Reminder",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("3", "5", "7", "14", "30").forEach { days ->
                    val isSelected = uiState.reminderDays == days
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateReminderDays(days) },
                        label = { Text("$days Days", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = uiState.reminderDays,
                onValueChange = { viewModel.updateReminderDays(it) },
                label = { Text("Custom Days Threshold") },
                placeholder = { Text("7") },
                leadingIcon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Get a background nudge if no update occurs within N days") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveApplication() },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Save Application",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

package com.rohit.jobtracker.android.ui.addedit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rohit.jobtracker.android.ui.components.StatusBadge
import com.rohit.jobtracker.android.ui.theme.backgroundColor
import com.rohit.jobtracker.android.ui.theme.textColor
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditApplicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 3

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
                    Text("Select Date", fontWeight = FontWeight.Bold)
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
                title = {
                    Column {
                        Text("Log Application", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = when (currentStep) {
                                1 -> "Step 1 of 3: Role & Company"
                                2 -> "Step 2 of 3: Source & Stage"
                                else -> "Step 3 of 3: Timeline & Reminders"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) {
                            currentStep -= 1
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Sticky Action Bar (Always visible without scrolling)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep -= 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Back", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (currentStep < totalSteps) {
                            Button(
                                onClick = {
                                    if (currentStep == 1) {
                                        if (uiState.company.isBlank()) {
                                            viewModel.updateCompany("")
                                            return@Button
                                        }
                                        if (uiState.role.isBlank()) {
                                            viewModel.updateRole("")
                                            return@Button
                                        }
                                    }
                                    currentStep += 1
                                },
                                modifier = Modifier
                                    .weight(if (currentStep > 1) 1.6f else 1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = if (currentStep == 1) "Next: Source & Stage" else "Next: Timeline",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Button(
                                onClick = { viewModel.saveApplication() },
                                enabled = !uiState.isSaving,
                                modifier = Modifier
                                    .weight(1.8f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Application", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Linear Step Indicator
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Animated Step Wizard Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(tween(250)) { it } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally(tween(250)) { -it } + fadeOut(tween(250)))
                    } else {
                        (slideInHorizontally(tween(250)) { -it } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally(tween(250)) { it } + fadeOut(tween(250)))
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
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

                    when (step) {
                        // -------------------------------------------------------------
                        // STEP 1: COMPANY & ROLE
                        // -------------------------------------------------------------
                        1 -> {
                            Text(
                                text = "What job are you applying to?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Enter the target organization and position title to track your interview pipeline.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = uiState.company,
                                onValueChange = { viewModel.updateCompany(it) },
                                label = { Text("Company Name *") },
                                placeholder = { Text("e.g. Stripe, Linear, Google") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                isError = uiState.companyError != null,
                                supportingText = uiState.companyError?.let { { Text(it) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = uiState.role,
                                onValueChange = { viewModel.updateRole(it) },
                                label = { Text("Job Role / Title *") },
                                placeholder = { Text("e.g. Senior Android Engineer") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Work,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                isError = uiState.roleError != null,
                                supportingText = uiState.roleError?.let { { Text(it) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                        }

                        // -------------------------------------------------------------
                        // STEP 2: SOURCE & PIPELINE STAGE
                        // -------------------------------------------------------------
                        2 -> {
                            Text(
                                text = "Source & Initial Stage",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Where did you discover this role, and what is the current stage of this application?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Job Platform / Source",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Source.entries.forEach { src ->
                                    val isSelected = uiState.source == src
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateSource(src) },
                                        label = {
                                            Text(
                                                text = src.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Initial Pipeline Stage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            val isDark = isSystemInDarkTheme()
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Status.entries.forEach { st ->
                                    val isSelected = uiState.status == st
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateStatus(st) },
                                        label = {
                                            Text(
                                                text = st.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = st.textColor(isDark)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = st.backgroundColor(isDark),
                                            selectedLabelColor = st.textColor(isDark)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        // -------------------------------------------------------------
                        // STEP 3: TIMELINE, URL & FOLLOW-UP NUDGES
                        // -------------------------------------------------------------
                        3 -> {
                            Text(
                                text = "Timeline & Follow-up",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Set the date applied and configure follow-up nudge notifications.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Date Applied Box (DD/MM/YYYY) with clean rounded clipping and no square shadow
                            val formattedDate = formatLocalDate(uiState.dateApplied)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showDatePicker = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Date Applied",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formattedDate,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "Change",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Job Posting URL (Optional)
                            OutlinedTextField(
                                value = uiState.jobLink,
                                onValueChange = { viewModel.updateJobLink(it) },
                                label = { Text("Job Link (Optional)") },
                                placeholder = { Text("https://...") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Follow-up Nudges
                            Text(
                                text = "Follow-up Reminder Nudge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
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
                                label = { Text("Days without activity") },
                                placeholder = { Text("7") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                supportingText = { Text("Get background reminder if no progress in N days") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatLocalDate(date: LocalDate): String {
    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.monthNumber.toString().padStart(2, '0')
    return "$day/$month/${date.year}"
}

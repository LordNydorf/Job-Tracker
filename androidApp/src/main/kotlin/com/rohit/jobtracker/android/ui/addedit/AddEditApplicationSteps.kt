package com.rohit.jobtracker.android.ui.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.rohit.jobtracker.android.ui.theme.textColor
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApplicationStepOne(
    company: String,
    role: String,
    salary: String,
    currency: String,
    companyError: String?,
    roleError: String?,
    onCompanyChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onSalaryChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit
) {
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
        value = company,
        onValueChange = onCompanyChange,
        label = { Text("Company Name *") },
        placeholder = { Text("e.g. Stripe, Linear, Google") },
        leadingIcon = {
            Icon(
                Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        isError = companyError != null,
        supportingText = companyError?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )

    OutlinedTextField(
        value = role,
        onValueChange = onRoleChange,
        label = { Text("Job Role / Title *") },
        placeholder = { Text("e.g. Senior Android Engineer") },
        leadingIcon = {
            Icon(
                Icons.Default.Work,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        isError = roleError != null,
        supportingText = roleError?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Multi-Currency Compensation Section
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Target Compensation (Optional)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Quick Currency Selector Chips
        val currencies = listOf("$", "₹", "€", "£", "AED", "CA$", "A$", "S$")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            currencies.forEach { curr ->
                val isSelected = currency == curr
                FilterChip(
                    selected = isSelected,
                    onClick = { onCurrencyChange(curr) },
                    label = {
                        Text(
                            text = when (curr) {
                                "$" -> "$ USD"
                                "₹" -> "₹ INR"
                                "€" -> "€ EUR"
                                "£" -> "£ GBP"
                                "AED" -> "AED"
                                "CA$" -> "CA$"
                                "A$" -> "A$"
                                "S$" -> "S$"
                                else -> curr
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        OutlinedTextField(
            value = salary,
            onValueChange = onSalaryChange,
            label = { Text("Salary / Rate") },
            placeholder = {
                Text(
                    when (currency) {
                        "₹" -> "e.g. 25 LPA or 2,500,000"
                        "€" -> "e.g. 85,000 / yr"
                        "£" -> "e.g. 75,000 / yr"
                        "AED" -> "e.g. 25,000 / mo"
                        else -> "e.g. 140,000 / yr or $75 / hr"
                    }
                )
            },
            prefix = {
                Text(
                    text = "$currency ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApplicationStepTwo(
    selectedSource: Source,
    selectedStatus: Status,
    onSourceChange: (Source) -> Unit,
    onStatusChange: (Status) -> Unit
) {
    val isDark = isSystemInDarkTheme()

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
            val isSelected = selectedSource == src
            FilterChip(
                selected = isSelected,
                onClick = { onSourceChange(src) },
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

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Status.entries.forEach { st ->
            val isSelected = selectedStatus == st
            FilterChip(
                selected = isSelected,
                onClick = { onStatusChange(st) },
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
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApplicationStepThree(
    formattedDate: String,
    jobLink: String,
    reminderDays: Int?,
    onOpenDatePicker: () -> Unit,
    onJobLinkChange: (String) -> Unit,
    onReminderDaysChange: (Int?) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var showCustomDialog by remember { mutableStateOf(false) }
    var customInputText by remember { mutableStateOf(reminderDays?.toString() ?: "") }

    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Custom Reminder Days") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter how many days after applying or updating you want a nudge to follow up:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 3) {
                                customInputText = input
                            }
                        },
                        label = { Text("Days") },
                        placeholder = { Text("e.g. 5, 10, 21") },
                        suffix = { Text("days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedDays = customInputText.toIntOrNull()
                        if (parsedDays != null && parsedDays > 0) {
                            onReminderDaysChange(parsedDays)
                        }
                        showCustomDialog = false
                    },
                    enabled = customInputText.toIntOrNull()?.let { it > 0 } == true
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Text(
        text = "Timeline & Follow-ups",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.5).sp
    )
    Text(
        text = "Set application date, link to posting, and configure automatic follow-up reminders.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(4.dp))

    // Date Picker Input
    val dateShape = RoundedCornerShape(16.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Application Date (DD/MM/YYYY)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dateShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = dateShape
                )
                .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface)
                .clickable { onOpenDatePicker() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Tap to change",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Job Posting Link
    OutlinedTextField(
        value = jobLink,
        onValueChange = onJobLinkChange,
        label = { Text("Job Posting URL (Optional)") },
        placeholder = { Text("https://...") },
        leadingIcon = {
            Icon(
                Icons.Default.Link,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )

    // Follow-up Reminders
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Follow-up Reminder Nudge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val presets = listOf(3, 7, 14)
        val isCustomSelected = reminderDays != null && reminderDays !in presets

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val reminderOptions = listOf(
                null to "None",
                3 to "3 Days",
                7 to "7 Days",
                14 to "14 Days"
            )

            reminderOptions.forEach { (days, label) ->
                val isSelected = reminderDays == days
                FilterChip(
                    selected = isSelected,
                    onClick = { onReminderDaysChange(days) },
                    label = {
                        Text(
                            text = label,
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

            // Custom Option Chip
            FilterChip(
                selected = isCustomSelected,
                onClick = {
                    customInputText = if (isCustomSelected) reminderDays.toString() else ""
                    showCustomDialog = true
                },
                label = {
                    Text(
                        text = if (isCustomSelected) "$reminderDays Days (Custom)" else "Custom...",
                        fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium
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
}

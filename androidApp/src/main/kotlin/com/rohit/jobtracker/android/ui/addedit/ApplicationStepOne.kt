package com.rohit.jobtracker.android.ui.addedit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohit.jobtracker.android.ui.theme.isAppInDarkTheme

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
    val isDark = isAppInDarkTheme()

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
                tint = if (companyError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            if (companyError != null) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        isError = companyError != null,
        supportingText = companyError?.let { errorText ->
            {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
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
                tint = if (roleError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            if (roleError != null) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        isError = roleError != null,
        supportingText = roleError?.let { errorText ->
            {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
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
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

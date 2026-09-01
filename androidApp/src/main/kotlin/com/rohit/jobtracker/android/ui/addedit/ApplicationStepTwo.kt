package com.rohit.jobtracker.android.ui.addedit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohit.jobtracker.android.ui.theme.backgroundColor
import com.rohit.jobtracker.android.ui.theme.borderColor
import com.rohit.jobtracker.android.ui.theme.isAppInDarkTheme
import com.rohit.jobtracker.android.ui.theme.textColor
import com.rohit.jobtracker.shared.model.Source
import com.rohit.jobtracker.shared.model.Status

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApplicationStepTwo(
    selectedSource: Source,
    selectedStatus: Status,
    onSourceChange: (Source) -> Unit,
    onStatusChange: (Status) -> Unit
) {
    val isDark = isAppInDarkTheme()

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
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
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
            val targetTextColor = if (isSelected) st.textColor(isDark) else MaterialTheme.colorScheme.onSurfaceVariant
            val targetBgColor = if (isSelected) st.backgroundColor(isDark) else if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface

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
                            tint = targetTextColor
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = targetBgColor,
                    labelColor = targetTextColor,
                    selectedContainerColor = targetBgColor,
                    selectedLabelColor = targetTextColor
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) st.borderColor(isDark) else MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

package com.rohit.jobtracker.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohit.jobtracker.android.ui.theme.borderColor
import com.rohit.jobtracker.android.ui.theme.backgroundColor
import com.rohit.jobtracker.android.ui.theme.textColor
import com.rohit.jobtracker.shared.model.Status

@Composable
fun StatusPipelineStepper(
    currentStatus: Status,
    onStatusSelected: (Status) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Status.entries.forEach { status ->
            val isSelected = status == currentStatus
            val targetTextColor = if (isSelected) status.textColor(isDark) else MaterialTheme.colorScheme.onSurfaceVariant
            val targetBgColor = if (isSelected) status.backgroundColor(isDark) else if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface

            val animatedTextColor by animateColorAsState(targetTextColor, tween(200), label = "textColor")
            val animatedBgColor by animateColorAsState(targetBgColor, tween(200), label = "bgColor")

            FilterChip(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStatusSelected(status)
                    }
                },
                enabled = enabled,
                label = {
                    Text(
                        text = status.displayName,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Current Stage",
                            tint = animatedTextColor
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = animatedBgColor,
                    labelColor = animatedTextColor,
                    selectedContainerColor = animatedBgColor,
                    selectedLabelColor = animatedTextColor
                ),
                border = if (isSelected) {
                    BorderStroke(1.dp, status.borderColor(isDark))
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                },
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}

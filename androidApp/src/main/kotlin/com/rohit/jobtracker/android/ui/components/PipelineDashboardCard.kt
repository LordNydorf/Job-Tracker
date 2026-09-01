package com.rohit.jobtracker.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohit.jobtracker.android.ui.list.StatusFilter
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Status

import com.rohit.jobtracker.android.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border

@Composable
fun PipelineDashboardCard(
    applications: List<Application>,
    onFilterSelected: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val appliedCount = applications.count { it.status == Status.APPLIED }
    val screeningCount = applications.count { it.status == Status.SCREENING }
    val interviewCount = applications.count { it.status == Status.INTERVIEW }
    val offerCount = applications.count { it.status == Status.OFFER }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pipeline Dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${applications.size} Total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PipelineStageItem(
                    count = appliedCount,
                    label = "Applied",
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFilterSelected(StatusFilter.APPLIED)
                    },
                    modifier = Modifier.weight(1f)
                )
                PipelineStageItem(
                    count = screeningCount,
                    label = "Screening",
                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFilterSelected(StatusFilter.SCREENING)
                    },
                    modifier = Modifier.weight(1f)
                )
                PipelineStageItem(
                    count = interviewCount,
                    label = "Interview",
                    color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFilterSelected(StatusFilter.INTERVIEW)
                    },
                    modifier = Modifier.weight(1f)
                )
                PipelineStageItem(
                    count = offerCount,
                    label = "Offers",
                    color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFilterSelected(StatusFilter.OFFER)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PipelineStageItem(
    count: Int,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val shape = RoundedCornerShape(16.dp)
    val itemBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else color.copy(alpha = 0.08f)
    val itemStroke = if (isDark) Color.Transparent else color.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(itemBg)
            .border(BorderStroke(1.dp, itemStroke), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

package com.rohit.jobtracker.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohit.jobtracker.android.ui.theme.StatusApplied
import com.rohit.jobtracker.android.ui.theme.StatusInterview
import com.rohit.jobtracker.android.ui.theme.StatusOffer
import com.rohit.jobtracker.android.ui.theme.StatusScreening
import com.rohit.jobtracker.shared.model.Application
import com.rohit.jobtracker.shared.model.Status
import kotlinx.datetime.Clock

@Composable
fun PipelineDashboardCard(
    applications: List<Application>,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val total = applications.size
    val active = applications.count { it.status == Status.INTERVIEW || it.status == Status.SCREENING }
    val offers = applications.count { it.status == Status.OFFER }
    val now = Clock.System.now()
    val nudges = applications.count {
        (it.status == Status.APPLIED || it.status == Status.SCREENING || it.status == Status.INTERVIEW) &&
                it.reminderDays != null && (now - it.lastUpdated).inWholeDays >= it.reminderDays!!
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pipeline Radar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                if (offers > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFD1FAE5)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$offers Offer${if (offers > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Grid Metric Pillars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricPill(
                    icon = Icons.Default.Work,
                    label = "Total",
                    value = total.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricPill(
                    icon = Icons.Default.AutoGraph,
                    label = "Active",
                    value = active.toString(),
                    color = StatusInterview,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricPill(
                    icon = Icons.Default.CheckCircle,
                    label = "Offers",
                    value = offers.toString(),
                    color = StatusOffer,
                    modifier = Modifier.weight(1f)
                )
                if (nudges > 0) {
                    DashboardMetricPill(
                        icon = Icons.Default.NotificationsActive,
                        label = "Nudge",
                        value = nudges.toString(),
                        color = Color(0xFFD97706),
                        isAlert = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (total > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                PipelineDistributionBar(applications = applications)
            }
        }
    }
}

@Composable
fun DashboardMetricPill(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    isAlert: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isAlert) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (isAlert) Color(0xFFB45309) else color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PipelineDistributionBar(
    applications: List<Application>,
    modifier: Modifier = Modifier
) {
    val total = applications.size.toFloat()
    val appliedWeight = applications.count { it.status == Status.APPLIED } / total
    val screeningWeight = applications.count { it.status == Status.SCREENING } / total
    val interviewWeight = applications.count { it.status == Status.INTERVIEW } / total
    val offerWeight = applications.count { it.status == Status.OFFER } / total
    val closedWeight = applications.count { it.status == Status.REJECTED || it.status == Status.GHOSTED } / total

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        if (appliedWeight > 0f) Box(modifier = Modifier.weight(appliedWeight).height(6.dp).background(StatusApplied))
        if (screeningWeight > 0f) Box(modifier = Modifier.weight(screeningWeight).height(6.dp).background(StatusScreening))
        if (interviewWeight > 0f) Box(modifier = Modifier.weight(interviewWeight).height(6.dp).background(StatusInterview))
        if (offerWeight > 0f) Box(modifier = Modifier.weight(offerWeight).height(6.dp).background(StatusOffer))
        if (closedWeight > 0f) Box(modifier = Modifier.weight(closedWeight).height(6.dp).background(Color(0xFF94A3B8)))
    }
}

package com.rohit.jobtracker.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohit.jobtracker.android.ui.theme.borderColor
import com.rohit.jobtracker.android.ui.theme.backgroundColor
import com.rohit.jobtracker.android.ui.theme.textColor
import com.rohit.jobtracker.shared.model.Status

@Composable
fun StatusBadge(
    status: Status,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(10.dp)
    val txtColor = status.textColor(isDark)
    val bgColor = status.backgroundColor(isDark)
    val strokeColor = status.borderColor(isDark)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(BorderStroke(1.dp, strokeColor), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(txtColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = status.displayName,
                color = txtColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

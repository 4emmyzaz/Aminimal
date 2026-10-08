package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.TextSecondary

@Composable
fun DeadlineBadge(
    dueDateMillis: Long,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val status = remember(dueDateMillis) {
        TimeFormatters.getDeadlineRelativeStatus(dueDateMillis)
    }

    val (bgColor, textColor, icon) = when {
        isCompleted -> Triple(
            DarkSurfaceContainer,
            TextSecondary,
            Icons.Outlined.Alarm
        )
        status.isOverdue -> Triple(
            AccentCoral.copy(alpha = 0.18f),
            AccentCoral,
            Icons.Outlined.ErrorOutline
        )
        status.isUrgent -> Triple(
            AccentAmber.copy(alpha = 0.18f),
            AccentAmber,
            Icons.Outlined.Alarm
        )
        else -> Triple(
            DarkSurfaceContainer,
            TextSecondary,
            Icons.Outlined.Alarm
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.8.dp, if (status.isOverdue && !isCompleted) AccentCoral.copy(alpha = 0.4f) else DarkBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Deadline icon",
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (status.isOverdue || status.isUrgent) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

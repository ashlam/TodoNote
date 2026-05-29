package com.todonote.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todonote.domain.model.Priority
import com.todonote.presentation.theme.PriorityHigh
import com.todonote.presentation.theme.PriorityLow
import com.todonote.presentation.theme.PriorityMedium

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val (label, color) = when (priority) {
        Priority.HIGH -> "高" to PriorityHigh
        Priority.MEDIUM -> "中" to PriorityMedium
        Priority.LOW -> "低" to PriorityLow
        Priority.NONE -> "" to Color.Transparent
    }
    if (priority == Priority.NONE) return
    Text(
        text = label,
        fontSize = 11.sp,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

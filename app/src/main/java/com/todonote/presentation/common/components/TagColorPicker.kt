package com.todonote.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val tagColors = listOf(
    0xFFE53935.toInt(), // Red
    0xFFFF5722.toInt(), // Orange
    0xFFFFC107.toInt(), // Amber
    0xFF4CAF50.toInt(), // Green
    0xFF009688.toInt(), // Teal
    0xFF2196F3.toInt(), // Blue
    0xFF3F51B5.toInt(), // Indigo
    0xFF9C27B0.toInt(), // Purple
    0xFFE91E63.toInt(), // Pink
    0xFF795548.toInt(), // Brown
    0xFF607D8B.toInt(), // Blue Grey
    0xFF9E9E9E.toInt()  // Grey
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tagColors.forEach { color ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

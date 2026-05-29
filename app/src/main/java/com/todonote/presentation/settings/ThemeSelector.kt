package com.todonote.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todonote.presentation.theme.*

@Composable
fun ThemeSelector(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    val themes = listOf(
        AppTheme.DEFAULT to ("默认蓝" to BluePrimary),
        AppTheme.OCEAN to ("海洋" to OceanPrimary),
        AppTheme.SUNSET to ("日落" to SunsetPrimary),
        AppTheme.FOREST to ("森林" to ForestPrimary),
        AppTheme.LAVENDER to ("薰衣草" to LavenderPrimary),
        AppTheme.CHERRY to ("樱桃" to CherryPrimary),
        AppTheme.MINT to ("薄荷" to MintPrimary),
        AppTheme.GOLD to ("金色" to GoldPrimary)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        themes.chunked(4).forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowThemes.forEach { (theme, pair) ->
                    val (name, color) = pair
                    ThemeColorItem(
                        name = name,
                        color = color,
                        selected = selectedTheme == theme,
                        onClick = { onThemeSelected(theme) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeColorItem(
    name: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 12.sp)
    }
}

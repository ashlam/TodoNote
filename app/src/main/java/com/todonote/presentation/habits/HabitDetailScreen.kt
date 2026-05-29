package com.todonote.presentation.habits
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todonote.R
import com.todonote.data.local.entity.HabitCheckInEntity
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: HabitDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId ?: 0L)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (habitId == null || habitId == 0L) "新建习惯" else uiState.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (habitId != null && habitId != 0L) {
                        val isArchived = uiState.habit?.isArchived == true
                        IconButton(onClick = {
                            if (isArchived) {
                                viewModel.unarchiveHabit()
                            } else {
                                viewModel.archiveHabit()
                                onNavigateBack()
                            }
                        }) {
                            Icon(
                                imageVector = if (isArchived) Icons.Filled.Unarchive else Icons.Filled.Archive,
                                contentDescription = if (isArchived) "恢复" else "归档"
                            )
                        }
                        IconButton(onClick = {
                            viewModel.deleteHabit()
                            onNavigateBack()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveHabit() },
                text = { Text(stringResource(R.string.save)) },
                icon = { Icon(Icons.Filled.Check, contentDescription = null) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("习惯名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Text("颜色", style = MaterialTheme.typography.titleMedium)
            ColorPicker(
                selectedColor = uiState.color,
                onColorSelected = viewModel::updateColor
            )

            if (habitId != null && habitId != 0L) {
                // Streak stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StreakStat("当前连续", uiState.currentStreak.toString(), Color(0xFF4CAF50))
                        StreakStat("最佳连续", uiState.bestStreak.toString(), Color(0xFFFF9800))
                    }
                }

                // Calendar heatmap
                Text("本月打卡", style = MaterialTheme.typography.titleMedium)
                MonthlyHeatmap(
                    checkIns = uiState.checkIns,
                    color = Color(uiState.color),
                    onDayClick = { date, isComplete ->
                        viewModel.checkIn(date, !isComplete)
                    }
                )
            }
        }
    }
}

@Composable
private fun StreakStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    val colors = listOf(
        0xFF2196F3.toInt(),
        0xFF4CAF50.toInt(),
        0xFFFF9800.toInt(),
        0xFFF44336.toInt(),
        0xFF9C27B0.toInt(),
        0xFF00BCD4.toInt(),
        0xFFE91E63.toInt(),
        0xFF795548.toInt()
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(
                        width = if (color == selectedColor) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun MonthlyHeatmap(
    checkIns: List<HabitCheckInEntity>,
    color: Color,
    onDayClick: (Long, Boolean) -> Unit
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)

    calendar.set(year, month, 1)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

    val completedDates = remember(checkIns) {
        checkIns
            .filter { it.isComplete }
            .map { normalizeDate(it.date) }
            .toSet()
    }

    val allDates = remember(year, month) {
        (1..daysInMonth).map { day ->
            val cal = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }
    }

    Column {
        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        var dayIndex = 0
        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

        for (week in 0 until totalCells / 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val cellIndex = week * 7 + dayOfWeek
                    if (cellIndex < firstDayOfWeek || dayIndex >= daysInMonth) {
                        Spacer(modifier = Modifier.size(36.dp))
                    } else {
                        val date = allDates[dayIndex]
                        val isComplete = completedDates.contains(date)
                        val isToday = normalizeDate(System.currentTimeMillis()) == date

                        DayCell(
                            day = dayIndex + 1,
                            isComplete = isComplete,
                            isToday = isToday,
                            color = color,
                            onClick = { onDayClick(date, isComplete) }
                        )
                        dayIndex++
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isComplete: Boolean,
    isToday: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                when {
                    isComplete -> color
                    isToday -> color.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) color else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 13.sp,
            color = if (isComplete) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun normalizeDate(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

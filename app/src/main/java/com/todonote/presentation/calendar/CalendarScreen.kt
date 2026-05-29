package com.todonote.presentation.calendar
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todonote.R
import com.todonote.domain.model.Task
import com.todonote.presentation.common.components.PriorityBadge
import com.todonote.presentation.common.extensions.formatDate
import com.todonote.presentation.common.extensions.isOverdue
import com.todonote.presentation.common.extensions.isToday
import org.koin.androidx.compose.koinViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val month = uiState.currentMonth
    var showQuickCreate by remember { mutableStateOf(false) }
    var quickCreateDate by remember { mutableStateOf<Calendar?>(null) }
    var quickCreateTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_calendar)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上个月")
                }
                Text(
                    text = "${month.get(Calendar.YEAR)}年${month.get(Calendar.MONTH) + 1}月",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下个月")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekday headers
            Row(modifier = Modifier.fillMaxWidth()) {
                val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            CalendarGrid(
                month = month,
                tasksByDate = uiState.tasksByDate,
                selectedDate = uiState.selectedDate,
                onDateClick = viewModel::selectDate,
                onDateLongClick = { date ->
                    quickCreateDate = date
                    showQuickCreate = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick create dialog
            if (showQuickCreate && quickCreateDate != null) {
                AlertDialog(
                    onDismissRequest = {
                        showQuickCreate = false
                        quickCreateTitle = ""
                    },
                    title = { Text("快速创建任务") },
                    text = {
                        OutlinedTextField(
                            value = quickCreateTitle,
                            onValueChange = { quickCreateTitle = it },
                            label = { Text("任务标题") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (quickCreateTitle.isNotBlank()) {
                                    viewModel.quickCreateTask(
                                        quickCreateTitle,
                                        quickCreateDate!!.timeInMillis
                                    )
                                    quickCreateTitle = ""
                                    showQuickCreate = false
                                }
                            }
                        ) { Text("创建") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showQuickCreate = false
                            quickCreateTitle = ""
                        }) { Text("取消") }
                    }
                )
            }

            // Selected date tasks
            val selectedDate = uiState.selectedDate
            if (selectedDate != null) {
                Text(
                    text = selectedDate.timeInMillis.formatDate(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.selectedDateTasks.isEmpty()) {
                    Text(
                        text = "该日期没有任务",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn {
                        items(uiState.selectedDateTasks, key = { it.id }) { task ->
                            CalendarTaskItem(task = task)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    month: Calendar,
    tasksByDate: Map<Int, List<Task>>,
    selectedDate: Calendar?,
    onDateClick: (Calendar) -> Unit,
    onDateLongClick: (Calendar) -> Unit = {}
) {
    val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = month.clone() as Calendar
    firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

    val today = Calendar.getInstance()

    Column {
        var day = 1
        for (week in 0 until 6) {
            if (day > daysInMonth) break
            Row(modifier = Modifier.fillMaxWidth()) {
                for (weekDay in 0 until 7) {
                    if (week == 0 && weekDay < firstDayOfWeek) {
                        Box(modifier = Modifier.weight(1f)) { }
                    } else if (day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f)) { }
                    } else {
                        val date = month.clone() as Calendar
                        date.set(Calendar.DAY_OF_MONTH, day)
                        val isToday = today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
                        val isSelected = selectedDate?.let {
                            it.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                                    it.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
                        } ?: false
                        val hasTasks = tasksByDate[day]?.isNotEmpty() == true
                        val hasIncompleteTasks = tasksByDate[day]?.any { !it.isCompleted } == true

                        CalendarDayCell(
                            day = day,
                            isToday = isToday,
                            isSelected = isSelected,
                            hasTasks = hasTasks,
                            hasIncompleteTasks = hasIncompleteTasks,
                            onClick = { onDateClick(date) },
                            onLongClick = { onDateLongClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                        day++
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasTasks: Boolean,
    hasIncompleteTasks: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (hasIncompleteTasks) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun CalendarTaskItem(task: Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    textDecoration = if (task.isCompleted)
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    else androidx.compose.ui.text.style.TextDecoration.None
                )
                if (task.dueDate != null) {
                    val dateColor = if (task.dueDate.isOverdue() && !task.isCompleted)
                        Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(
                        text = task.dueDate.formatDate(),
                        fontSize = 12.sp,
                        color = dateColor
                    )
                }
            }
            PriorityBadge(priority = task.priority)
        }
    }
}

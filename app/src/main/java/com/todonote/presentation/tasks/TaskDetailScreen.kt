package com.todonote.presentation.tasks
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.todonote.R
import com.todonote.domain.model.Priority
import com.todonote.presentation.common.components.PriorityBadge
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long?,
    listId: Long = 1L,
    onNavigateBack: () -> Unit,
    viewModel: TaskDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId ?: 0L, listId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "新建任务" else "编辑任务") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (taskId != null) {
                        IconButton(onClick = {
                            viewModel.deleteTask()
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
                onClick = { viewModel.saveTask(listId) },
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
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text(stringResource(R.string.task_title_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text(stringResource(R.string.task_notes_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Text("优先级", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.filter { it != Priority.NONE }.forEach { priority ->
                    FilterChip(
                        selected = uiState.priority == priority,
                        onClick = { viewModel.updatePriority(priority) },
                        label = { PriorityBadge(priority = priority) }
                    )
                }
            }

            Text("截止日期", style = MaterialTheme.typography.titleMedium)
            DatePickerButton(
                selectedDate = uiState.dueDate,
                onDateSelected = viewModel::updateDueDate
            )

            Text("提醒时间", style = MaterialTheme.typography.titleMedium)
            DatePickerButton(
                selectedDate = uiState.reminderTime,
                onDateSelected = viewModel::updateReminderTime
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { showPicker = true }) {
        Text(
            selectedDate?.let {
                val sdf = java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.CHINA)
                sdf.format(java.util.Date(it))
            } ?: "选择日期"
        )
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showPicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDateSelected(null)
                    showPicker = false
                }) { Text("清除") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

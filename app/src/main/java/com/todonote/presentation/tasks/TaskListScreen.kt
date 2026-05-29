package com.todonote.presentation.tasks
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.todonote.R
import com.todonote.domain.model.Task
import com.todonote.domain.model.TaskList
import com.todonote.presentation.common.components.*
import com.todonote.presentation.common.extensions.formatDate
import com.todonote.presentation.common.extensions.isOverdue
import com.todonote.presentation.common.extensions.isToday
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onTaskClick: (Long, Long) -> Unit,
    onAddTask: (Long) -> Unit,
    viewModel: TaskListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddList by remember { mutableStateOf(false) }
    var newListTitle by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Drag and drop state
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val itemHeightPx = 72f // Approximate item height in pixels

    LaunchedEffect(uiState.justDeletedTask) {
        uiState.justDeletedTask?.let {
            val result = snackbarHostState.showSnackbar(
                message = "任务已删除",
                actionLabel = "撤销",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_tasks)) },
                actions = {
                    IconButton(onClick = { showAddList = true }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_list))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    uiState.selectedListId?.let { onAddTask(it) }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = stringResource(R.string.search)
            )

            if (uiState.lists.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    itemsIndexed(uiState.lists, key = { _, list -> list.id }) { _, list ->
                        ListChip(
                            list = list,
                            selected = list.id == uiState.selectedListId,
                            onClick = { viewModel.selectList(list.id) }
                        )
                    }
                }
            }

            if (uiState.tasks.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    title = stringResource(R.string.empty_state_title),
                    description = stringResource(R.string.empty_state_desc)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(uiState.tasks, key = { _, task -> task.id }) { index, task ->
                        val isDragging = draggingItemIndex == index
                        val scale by animateFloatAsState(if (isDragging) 1.02f else 1f)

                        TaskItem(
                            task = task,
                            onToggle = { viewModel.toggleTask(task.id) },
                            onClick = { onTaskClick(task.id, task.listId) },
                            onDelete = { viewModel.deleteTask(task) },
                            onDragStart = {
                                draggingItemIndex = index
                                dragOffset = 0f
                            },
                            onDrag = { delta ->
                                dragOffset += delta
                                val moveCount = (dragOffset / itemHeightPx).toInt()
                                if (moveCount != 0) {
                                    val newIndex = (index + moveCount)
                                        .coerceIn(0, uiState.tasks.size - 1)
                                    if (newIndex != index) {
                                        viewModel.reorderTask(index, newIndex)
                                        draggingItemIndex = newIndex
                                        dragOffset = 0f
                                    }
                                }
                            },
                            onDragEnd = {
                                draggingItemIndex = null
                                dragOffset = 0f
                            },
                            isDragging = isDragging,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = if (isDragging) dragOffset else 0f
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .zIndex(if (isDragging) 1f else 0f)
                        )
                        if (!isDragging) {
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddList) {
        AlertDialog(
            onDismissRequest = { showAddList = false },
            title = { Text(stringResource(R.string.add_list)) },
            text = {
                OutlinedTextField(
                    value = newListTitle,
                    onValueChange = { newListTitle = it },
                    placeholder = { Text("清单名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newListTitle.isNotBlank()) {
                            viewModel.createList(newListTitle, 0xFF2196F3.toInt())
                            newListTitle = ""
                            showAddList = false
                        }
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddList = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun ListChip(
    list: TaskList,
    selected: Boolean,
    onClick: () -> Unit
) {
    val progress = list.progress
    val progressText = "${list.completedCount}/${list.taskCount}"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(list.color).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(list.color))
            )
            Text(list.title, fontSize = 14.sp)
            if (list.taskCount > 0) {
                Text(progressText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = Color.White
                )
            }
        },
        content = {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .background(
                        if (isDragging) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else Color.Transparent
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag handle
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "拖拽排序",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount.y)
                                },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() }
                            )
                        }
                )

                TodoCheckbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        task.dueDate?.let { date ->
                            val dateColor = when {
                                date.isOverdue() && !task.isCompleted -> Color.Red
                                date.isToday() -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Text(
                                text = date.formatDate(),
                                fontSize = 12.sp,
                                color = dateColor
                            )
                        }
                        PriorityBadge(priority = task.priority)
                    }
                }
            }
        }
    )
}

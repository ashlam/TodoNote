package com.todonote.presentation.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.data.local.notification.AlarmReceiver
import com.todonote.domain.model.Priority
import com.todonote.domain.model.Task
import com.todonote.domain.usecase.task.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val task: Task? = null,
    val title: String = "",
    val notes: String = "",
    val dueDate: Long? = null,
    val reminderTime: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class TaskDetailViewModel(
    private val context: Context,
    private val getTasksUseCase: GetTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: Long, listId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (taskId == 0L) {
                _uiState.update {
                    it.copy(
                        task = null,
                        title = "",
                        notes = "",
                        dueDate = null,
                        reminderTime = null,
                        priority = Priority.MEDIUM,
                        isLoading = false
                    )
                }
            } else {
                try {
                    val task = getTasksUseCase(listId).first().find { it.id == taskId }
                    task?.let {
                        _uiState.update { state ->
                            state.copy(
                                task = it,
                                title = it.title,
                                notes = it.notes,
                                dueDate = it.dueDate,
                                reminderTime = it.reminderTime,
                                priority = it.priority,
                                isLoading = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updateDueDate(date: Long?) {
        _uiState.update { it.copy(dueDate = date) }
    }

    fun updateReminderTime(time: Long?) {
        _uiState.update { it.copy(reminderTime = time) }
    }

    fun updatePriority(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun saveTask(listId: Long) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank()) return@launch

            val task = state.task?.copy(
                title = state.title,
                notes = state.notes,
                dueDate = state.dueDate,
                reminderTime = state.reminderTime,
                priority = state.priority,
                updatedAt = System.currentTimeMillis()
            ) ?: Task(
                listId = listId,
                title = state.title,
                notes = state.notes,
                dueDate = state.dueDate,
                reminderTime = state.reminderTime,
                priority = state.priority
            )

            val taskId = if (task.id == 0L) {
                createTaskUseCase(task)
            } else {
                updateTaskUseCase(task)
                task.id
            }

            // Schedule or cancel reminder
            if (state.reminderTime != null && state.reminderTime > System.currentTimeMillis()) {
                AlarmReceiver.scheduleReminder(context, taskId, state.title, state.reminderTime)
            } else {
                AlarmReceiver.cancelReminder(context, taskId)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            _uiState.value.task?.let {
                deleteTaskUseCase(it)
                AlarmReceiver.cancelReminder(context, it.id)
            }
        }
    }
}

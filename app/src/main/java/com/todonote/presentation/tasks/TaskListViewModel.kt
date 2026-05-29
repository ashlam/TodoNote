package com.todonote.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.domain.model.Task
import com.todonote.domain.model.TaskList
import com.todonote.domain.repository.ITaskRepository
import com.todonote.domain.usecase.list.CreateListUseCase
import com.todonote.domain.usecase.list.GetListsUseCase
import com.todonote.domain.usecase.task.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TaskListUiState(
    val lists: List<TaskList> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val selectedListId: Long? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val justDeletedTask: Task? = null
)

class TaskListViewModel(
    private val getListsUseCase: GetListsUseCase,
    private val createListUseCase: CreateListUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val searchTasksUseCase: SearchTasksUseCase,
    private val taskRepository: ITaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadLists()

        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        val listId = _uiState.value.selectedListId ?: return@flatMapLatest flowOf(emptyList())
                        getTasksUseCase(listId)
                    } else {
                        searchTasksUseCase(query)
                    }
                }
                .collect { tasks ->
                    _uiState.update { it.copy(tasks = tasks) }
                }
        }
    }

    private fun loadLists() {
        viewModelScope.launch {
            getListsUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { lists ->
                    if (lists.isEmpty()) {
                        createListUseCase(TaskList(title = "默认清单", color = 0xFF2196F3.toInt()))
                        return@collect
                    }
                    val currentListId = _uiState.value.selectedListId
                    val selectedListId = when {
                        currentListId == null || lists.none { it.id == currentListId } -> lists.first().id
                        else -> currentListId
                    }
                    _uiState.update { it.copy(lists = lists, selectedListId = selectedListId) }
                    selectedListId?.let { loadTasks(it) }
                }
        }
    }

    private fun loadTasks(listId: Long) {
        viewModelScope.launch {
            getTasksUseCase(listId)
                .collect { tasks ->
                    _uiState.update { it.copy(tasks = tasks) }
                }
        }
    }

    fun selectList(listId: Long) {
        _uiState.update { it.copy(selectedListId = listId, searchQuery = "") }
        _searchQuery.value = ""
        loadTasks(listId)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
    }

    fun toggleTask(taskId: Long) {
        viewModelScope.launch {
            toggleTaskUseCase(taskId)
        }
    }

    private var deleteJob: Job? = null

    fun deleteTask(task: Task) {
        deleteJob?.cancel()
        _uiState.update { it.copy(justDeletedTask = task) }
        deleteJob = viewModelScope.launch {
            delay(3000)
            deleteTaskUseCase(task)
            _uiState.update { it.copy(justDeletedTask = null) }
        }
    }

    fun undoDelete() {
        deleteJob?.cancel()
        _uiState.update { it.copy(justDeletedTask = null) }
    }

    fun createList(title: String, color: Int) {
        viewModelScope.launch {
            val list = TaskList(title = title, color = color)
            createListUseCase(list)
        }
    }

    fun createTask(title: String, listId: Long) {
        viewModelScope.launch {
            val task = Task(listId = listId, title = title)
            createTaskUseCase(task)
        }
    }

    fun reorderTask(fromIndex: Int, toIndex: Int) {
        val currentTasks = _uiState.value.tasks.toMutableList()
        if (fromIndex < 0 || fromIndex >= currentTasks.size) return
        if (toIndex < 0 || toIndex >= currentTasks.size) return
        if (fromIndex == toIndex) return

        val item = currentTasks.removeAt(fromIndex)
        currentTasks.add(toIndex, item)
        _uiState.update { it.copy(tasks = currentTasks) }

        viewModelScope.launch {
            currentTasks.forEachIndexed { index, task ->
                if (task.sortOrder != index) {
                    taskRepository.updateSortOrder(task.id, index)
                }
            }
        }
    }
}

package com.todonote.domain.usecase.task

import com.todonote.domain.model.Task
import com.todonote.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.Flow

class SearchTasksUseCase(private val repository: ITaskRepository) {
    operator fun invoke(query: String): Flow<List<Task>> = repository.search(query)
}

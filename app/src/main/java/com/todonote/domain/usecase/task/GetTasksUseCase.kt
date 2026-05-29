package com.todonote.domain.usecase.task

import com.todonote.domain.model.Task
import com.todonote.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(private val repository: ITaskRepository) {
    operator fun invoke(listId: Long): Flow<List<Task>> = repository.getByListId(listId)
}

package com.todonote.domain.usecase.task

import com.todonote.domain.model.Task
import com.todonote.domain.repository.ITaskRepository

class DeleteTaskUseCase(private val repository: ITaskRepository) {
    suspend operator fun invoke(task: Task) = repository.delete(task)
}

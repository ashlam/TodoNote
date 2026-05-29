package com.todonote.domain.usecase.task

import com.todonote.domain.repository.ITaskRepository

class ToggleTaskUseCase(private val repository: ITaskRepository) {
    suspend operator fun invoke(taskId: Long) = repository.toggleCompleted(taskId)
}

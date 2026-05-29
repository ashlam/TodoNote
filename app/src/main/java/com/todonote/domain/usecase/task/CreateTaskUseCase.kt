package com.todonote.domain.usecase.task

import com.todonote.domain.model.Task
import com.todonote.domain.repository.ITaskRepository

class CreateTaskUseCase(private val repository: ITaskRepository) {
    suspend operator fun invoke(task: Task): Long = repository.insert(task)
}

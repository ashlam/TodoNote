package com.todonote.domain.usecase.list

import com.todonote.domain.model.TaskList
import com.todonote.domain.repository.IListRepository
import kotlinx.coroutines.flow.Flow

class GetListsUseCase(private val repository: IListRepository) {
    operator fun invoke(): Flow<List<TaskList>> = repository.getAll()
}

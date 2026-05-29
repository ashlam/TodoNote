package com.todonote.domain.usecase.list

import com.todonote.domain.model.TaskList
import com.todonote.domain.repository.IListRepository

class UpdateListUseCase(private val repository: IListRepository) {
    suspend operator fun invoke(list: TaskList) = repository.update(list)
}

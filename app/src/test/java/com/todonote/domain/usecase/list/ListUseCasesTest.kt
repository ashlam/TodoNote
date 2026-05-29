package com.todonote.domain.usecase.list

import com.todonote.domain.model.TaskList
import com.todonote.domain.repository.IListRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListUseCasesTest {

    private lateinit var repository: IListRepository
    private val testList = TaskList(
        id = 1,
        title = "Test List",
        color = 0xFF0000,
        sortOrder = 0
    )

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `createList calls repository insert and returns id`() = runTest {
        coEvery { repository.insert(any()) } returns 42L

        val useCase = CreateListUseCase(repository)
        val result = useCase(testList)

        assertEquals(42L, result)
        coVerify { repository.insert(testList) }
    }

    @Test
    fun `getLists returns flow from repository`() = runTest {
        val lists = listOf(testList)
        coEvery { repository.getAll() } returns flowOf(lists)

        val useCase = GetListsUseCase(repository)
        val result = useCase().first()

        assertEquals(lists, result)
    }

    @Test
    fun `updateList calls repository update`() = runTest {
        coEvery { repository.update(any()) } just Runs

        val useCase = UpdateListUseCase(repository)
        useCase(testList.copy(title = "Updated"))

        coVerify { repository.update(any()) }
    }

    @Test
    fun `deleteList calls repository delete`() = runTest {
        coEvery { repository.delete(any()) } just Runs

        val useCase = DeleteListUseCase(repository)
        useCase(testList)

        coVerify { repository.delete(testList) }
    }
}

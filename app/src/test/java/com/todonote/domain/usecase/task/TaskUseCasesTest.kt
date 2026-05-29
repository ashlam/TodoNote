package com.todonote.domain.usecase.task

import com.todonote.domain.model.Priority
import com.todonote.domain.model.Task
import com.todonote.domain.repository.ITaskRepository
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
class TaskUseCasesTest {

    private lateinit var repository: ITaskRepository
    private val testTask = Task(
        id = 1,
        listId = 1,
        title = "Test Task",
        notes = "Notes",
        priority = Priority.HIGH,
        isCompleted = false
    )

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `createTask calls repository insert and returns id`() = runTest {
        coEvery { repository.insert(any()) } returns 42L

        val useCase = CreateTaskUseCase(repository)
        val result = useCase(testTask)

        assertEquals(42L, result)
        coVerify { repository.insert(testTask) }
    }

    @Test
    fun `getTasks returns flow from repository`() = runTest {
        val tasks = listOf(testTask)
        coEvery { repository.getByListId(1L) } returns flowOf(tasks)

        val useCase = GetTasksUseCase(repository)
        val result = useCase(1L).first()

        assertEquals(tasks, result)
    }

    @Test
    fun `updateTask calls repository update`() = runTest {
        coEvery { repository.update(any()) } just Runs

        val useCase = UpdateTaskUseCase(repository)
        useCase(testTask.copy(title = "Updated"))

        coVerify { repository.update(any()) }
    }

    @Test
    fun `deleteTask calls repository delete`() = runTest {
        coEvery { repository.delete(any()) } just Runs

        val useCase = DeleteTaskUseCase(repository)
        useCase(testTask)

        coVerify { repository.delete(testTask) }
    }

    @Test
    fun `toggleTask calls repository toggleCompleted`() = runTest {
        coEvery { repository.toggleCompleted(any()) } just Runs

        val useCase = ToggleTaskUseCase(repository)
        useCase(1L)

        coVerify { repository.toggleCompleted(1L) }
    }

    @Test
    fun `searchTasks returns flow from repository`() = runTest {
        val tasks = listOf(testTask)
        coEvery { repository.search("test") } returns flowOf(tasks)

        val useCase = SearchTasksUseCase(repository)
        val result = useCase("test").first()

        assertEquals(tasks, result)
    }
}

package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository

class DeleteTask(
    private val tasksRepository: TasksRepository
) {
    suspend fun execute(request: TaskRequest): Result<Unit, String> {
        return Success(tasksRepository.deleteTask(request.task))
    }
}

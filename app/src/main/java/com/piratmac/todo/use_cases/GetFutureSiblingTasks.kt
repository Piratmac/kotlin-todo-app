package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task


class GetFutureSiblingTasks(private val tasksRepository: TasksRepository) {
    suspend fun execute(request: TaskRequest): Result<List<Task>, String> {
        return Success(tasksRepository.getFutureDueSiblings(request.task))
    }
}

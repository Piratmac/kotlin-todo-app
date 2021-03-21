package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task

class GetTask(private val tasksRepository: TasksRepository) {
    class Request(
        val taskId: Long
    )

    suspend fun execute(request: Request): Result<Task, String> {
        val task = tasksRepository.getTask(request.taskId)
            ?: return Failure("No task found with ID %d".format(request.taskId))

        return Success(task)
    }
}


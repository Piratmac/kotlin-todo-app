package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task


class SaveTask(private val tasksRepository: TasksRepository) {
    suspend fun execute(request: TaskRequest): Result<Task, String> {
        val taskId = tasksRepository.saveTask(request.task)
        request.task.id = taskId
        return Success(request.task)
    }
}

package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task

class GetTasksForWidget(private val tasksRepository: TasksRepository) {
    suspend fun execute(): Result<List<Task>, String> =
        Success(tasksRepository.getTasksForWidget())

}
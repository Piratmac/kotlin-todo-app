package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task
import java.time.LocalDateTime

class GetTasksForNotification(private val tasksRepository: TasksRepository) {
    suspend fun execute(): Result<List<Task>, String> =
        Success(tasksRepository.getTasksForNotification().filter {
            it.due.isAfter(LocalDateTime.now())
        })

}
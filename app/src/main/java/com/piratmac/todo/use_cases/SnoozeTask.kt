package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task

class SnoozeTask(private val tasksRepository: TasksRepository) {
    class Request(
        val taskId: Long,
        val durationInSeconds: Long = 300L
    )

    suspend fun execute(request: Request): Result<Task, String> {
        return GetTask(tasksRepository).execute(GetTask.Request(request.taskId))
            .mapSuccess { task -> snooze(request, task) }
    }

    private fun snooze (request: Request, task: Task): Task {
        task.due = task.due.plusSeconds(request.durationInSeconds)
        return task
    }
}

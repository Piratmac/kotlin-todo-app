package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task

class BuildFutureSiblingTask(
    private val tasksRepository: TasksRepository
) {
    suspend fun execute(request: TaskRequest): Result<Pair<Task, Task?>, String> {
        if (!request.task.isRepeating)
            return Success(Pair(request.task, null))

        return GetFutureSiblingTasks(tasksRepository).execute(
            TaskRequest(request.task)
        )
            .mapSuccess { Pair(request.task, buildFutureTask(it, request.task)) }
    }

    private fun buildFutureTask(
        futureDueSiblings: List<Task>,
        task: Task
    ): Task? {
        if (futureDueSiblings.isEmpty()) {
            val newTask = task.copy()
            newTask.id = 0
            newTask.due += newTask.repeatFrequency
            return newTask
        }
        return null
    }

}
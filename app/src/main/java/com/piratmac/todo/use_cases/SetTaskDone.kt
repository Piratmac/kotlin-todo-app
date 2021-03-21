package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task

class SetTaskDone(private val tasksRepository: TasksRepository) {
    suspend fun execute(request: TaskRequest): Result<Pair<Task, Task?>, String> {
        return BuildFutureSiblingTask(tasksRepository).execute(request)
            .mapSuccess { (oldTask, newTask) -> Pair(markDone(oldTask), newTask) }
    }

    private fun markDone(task: Task): Task {
        task.done = true
        task.notifyWhenDue = false
        return task
    }
}

package com.piratmac.todo.use_cases

import com.piratmac.todo.models.Task
import java.time.LocalDateTime

class SetTaskDueDateTime {
    class Request(
        val task: Task,
        val newDateTime: LocalDateTime
    )

    fun execute(request: Request): Result<Task, String> {
        return setDueDateTime(request.task, request.newDateTime)
    }

    private fun setDueDateTime(task: Task, newDateTime: LocalDateTime): Result<Task, String> {
        task.due = newDateTime

        return Success(task)
    }
}
package com.piratmac.todo.use_cases

import com.piratmac.todo.models.Task
import java.time.LocalDate
import java.time.LocalDateTime

class SetTaskDueDate {
    class Request(
        val task: Task,
        val newDate: LocalDate
    )

    fun execute(request: Request): Result<Task, String> {
        return setDueDate(request.task, request.newDate)
    }

    private fun setDueDate(task: Task, newDate: LocalDate): Result<Task, String> {
        val newTime = task.due.toLocalTime()
        task.due = LocalDateTime.of(newDate, newTime)

        return Success(task)
    }
}
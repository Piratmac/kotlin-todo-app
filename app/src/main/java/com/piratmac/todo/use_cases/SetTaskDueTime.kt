package com.piratmac.todo.use_cases

import com.piratmac.todo.models.TASK_DATETIME_LATER
import com.piratmac.todo.models.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SetTaskDueTime {
    class Request(
        val task: Task,
        val newTime: LocalTime
    )

    fun execute(request: Request): Result<Task, String> {
        return setDueTime(request.task, request.newTime)
    }

    private fun setDueTime(task: Task, newTime: LocalTime): Result<Task, String> {
        var newDate = task.due.toLocalDate()
        if (newDate.isEqual(TASK_DATETIME_LATER.toLocalDate()))
            newDate = LocalDate.now()

        task.due = LocalDateTime.of(newDate, newTime)

        return Success(task)
    }
}
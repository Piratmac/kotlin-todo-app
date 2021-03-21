package com.piratmac.todo.use_cases

import com.piratmac.todo.models.Task


class SetTaskToggleNotification {
    fun execute(request:TaskRequest): Result<Task, String> {
        return toggleNotification(request.task)
    }

    private fun toggleNotification (task: Task): Result<Task, String> {
        task.notifyWhenDue = !task.notifyWhenDue

        return Success(task)
    }
}
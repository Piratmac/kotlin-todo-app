package com.piratmac.todo.use_cases

import com.piratmac.todo.models.Task

class SetTaskUndone {
        fun execute(request: TaskRequest): Result<Task, String> {
            return markUndone(request.task)
        }

        private fun markUndone(task: Task): Result<Task, String> {
            task.done = false
            return Success(task)
        }

}
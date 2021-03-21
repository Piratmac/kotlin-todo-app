package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task
import kotlinx.coroutines.runBlocking
import java.time.Period

class SetTaskRepetitionPeriod(private val tasksRepository: TasksRepository) {
    class Request(
        val task: Task,
        val newPeriod: Period
    )

    suspend fun execute(request: Request): Result<Task, String> {
        return setRepetitionPeriod(request.task, request.newPeriod)
            .andThen { runBlocking { Success(tasksRepository.getNextRepetitionGroup()) } }
            .andThen { setRepetitionParent(request.task, it) }
    }

    private fun setRepetitionPeriod(task: Task, newPeriod: Period): Result<Task, String> {
        if (newPeriod == Period.ZERO)
            task.repeatGroup = 0

        task.repeatFrequency = newPeriod

        return Success(task)
    }

    private fun setRepetitionParent(task: Task, repetitionGroup: Long): Result<Task, String> {
        if (task.repeatFrequency != Period.ZERO && task.repeatGroup == 0L)
            task.repeatGroup = repetitionGroup

        return Success(task)
    }
}
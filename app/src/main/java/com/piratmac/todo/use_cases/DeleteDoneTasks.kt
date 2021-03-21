package com.piratmac.todo.use_cases

import com.piratmac.todo.data.repository.TasksRepository

class DeleteDoneTasks(
    private val tasksRepository: TasksRepository
) {
    suspend fun execute(): Result<Unit, String> {
        return Success(tasksRepository.deleteDoneTasks())
    }
}

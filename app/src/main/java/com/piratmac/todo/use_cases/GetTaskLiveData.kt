package com.piratmac.todo.use_cases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task


class GetTaskLiveData(private val tasksRepository: TasksRepository) {
    class Request(
        val taskId: Long
    )

    fun execute(request: Request): LiveData<Task> {
        return if (request.taskId == 0L)
            MutableLiveData(Task(0))
        else
            tasksRepository.getTaskLiveData(request.taskId)
    }
}
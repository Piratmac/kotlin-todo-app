package com.piratmac.todo.view_models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.piratmac.todo.data.database.getDatabase
import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.ActionLiveData
import com.piratmac.todo.models.Task
import com.piratmac.todo.use_cases.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period

class TaskDetailsViewModel(application: Application, taskId: Long) : ViewModel() {
    // Navigation to TodoList
    private val _navigateToTodoList = ActionLiveData<Boolean?>()
    val navigateToTodoList: ActionLiveData<Boolean?>
        get() = _navigateToTodoList

    // Schedule notification for task
    private val _scheduleNotificationForTask = ActionLiveData<Task>()
    val scheduleNotificationForTask: ActionLiveData<Task>
        get() = _scheduleNotificationForTask

    // Delete notification for task
    private val _deleteNotificationForTask = ActionLiveData<Task>()
    val deleteNotificationForTask: ActionLiveData<Task>
        get() = _deleteNotificationForTask

    private val tasksRepository = TasksRepository(getDatabase(application))
    val taskLiveData = GetTaskLiveData(tasksRepository).execute(GetTaskLiveData.Request(taskId))
    var task: Task = Task(taskId)

    class TaskDetailsViewFactory(private val app: Application, private val taskId: Long = 0L) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskDetailsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskDetailsViewModel(app, taskId) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }

    fun setDueDate(newDate: LocalDate) {
        viewModelScope.launch {
            SetTaskDueDate().execute(SetTaskDueDate.Request(task, newDate))
                .mapSuccess { saveTask(); it }
        }
    }

    fun setDueTime(newTime: LocalTime) {
        viewModelScope.launch {
            SetTaskDueTime().execute(SetTaskDueTime.Request(task, newTime))
                .mapSuccess { saveTask(); it }
        }
    }

    fun setDueDateTime(newTime: LocalDateTime) {
        viewModelScope.launch {
            SetTaskDueDateTime().execute(SetTaskDueDateTime.Request(task, newTime))
                .mapSuccess { saveTask(); it }
        }
    }

    fun setRepetitionPeriod(newPeriod: Period) {
        viewModelScope.launch {
            SetTaskRepetitionPeriod(tasksRepository).execute(
                SetTaskRepetitionPeriod.Request(
                    task,
                    newPeriod
                )
            )
                .mapSuccess { saveTask(); it }
        }
    }

    fun toggleNotification() {
        viewModelScope.launch {
            SetTaskToggleNotification().execute(TaskRequest(task))
                .mapSuccess { saveTask(); it }
        }
    }

    private fun saveTask() {
        viewModelScope.launch {
            SaveTask(tasksRepository).execute(TaskRequest(task))
                .mapError { "Error while saving : $it" }
                .mapSuccess {
                    if (it.notifyWhenDue)
                        _scheduleNotificationForTask.value = it
                    else
                        _deleteNotificationForTask.value = it
                    it
                }
        }
    }

    private fun deleteTask() {
        viewModelScope.launch {
            DeleteTask(tasksRepository).execute(TaskRequest(task))
                .mapError { "Error while deleting : $it" }
                .mapSuccess { _deleteNotificationForTask.value = task }
        }
    }


    fun onSaveTaskClick() {
        saveTask()
        _navigateToTodoList.sendAction(true)
    }

    fun onDeleteTaskClick() {
        deleteTask()
        _navigateToTodoList.sendAction(true)
    }
}

package com.piratmac.todo.view_models

import android.app.Application
import androidx.lifecycle.*
import com.piratmac.todo.data.database.getDatabase
import com.piratmac.todo.models.Task
import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.ActionLiveData
import com.piratmac.todo.use_cases.*
import kotlinx.coroutines.launch


class TodoListViewModel(application: Application) : AndroidViewModel(application) {
    // Force update of RecyclerView based on moved task
    private val _taskMoved = ActionLiveData<Task?>()
    val taskMoved: ActionLiveData<Task?>
        get() = _taskMoved

    // Delete notification for task
    private val _deleteNotificationForTask = ActionLiveData<Task>()
    val deleteNotificationForTask: ActionLiveData<Task>
        get() = _deleteNotificationForTask

    // Schedule notification for task
    private val _scheduleNotificationForTask = ActionLiveData<Task>()
    val scheduleNotificationForTask: ActionLiveData<Task>
        get() = _scheduleNotificationForTask

    private val tasksRepository = TasksRepository(getDatabase(application))

    val visibleTasks = tasksRepository.allTasks

    fun deleteDoneTasks() {
        viewModelScope.launch {
            DeleteDoneTasks(tasksRepository).execute()
        }
    }
    
    fun onItemRadioButtonClick(task: Task) {
        // This is a hack: when using LiveData, the list in the memory of the adapter's DiffUtil is the same as the new list sent through submitList
        // This happens when checking and un-checking the same task twice in a row
        // Therefore the change is not detected, and the list doesn't update (and styles are not applied)
        // This value forces the re-calculation
        _taskMoved.value = task

        viewModelScope.launch {
            if (!task.done)
                viewModelScope.launch {
                    SetTaskDone(tasksRepository).execute(TaskRequest(task))
                        .mapSuccess { _deleteNotificationForTask.value = it.first; it }
                        .mapSuccess {
                            if (it.second != null) _scheduleNotificationForTask.value =
                                it.second; it
                        }
                        .mapSuccess { saveTask(it.first); it }
                        .mapSuccess { if (it.second != null) saveTask(it.second!!); it }
                }
            else SetTaskUndone().execute(TaskRequest(task))
                .mapSuccess { saveTask(task); task }
        }
    }

    private fun saveTask(task: Task) {
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
}

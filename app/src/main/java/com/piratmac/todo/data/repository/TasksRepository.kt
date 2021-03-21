package com.piratmac.todo.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.piratmac.todo.data.database.TasksDatabase
import com.piratmac.todo.data.database.asDatabaseModel
import com.piratmac.todo.data.database.asDomainModel
import com.piratmac.todo.models.Task

class TasksRepository(private val database: TasksDatabase) {
    val allTasks: LiveData<List<Task>> =
        Transformations.map(database.taskDao.getTasks()) {
            it.asDomainModel()
        }


    suspend fun getFutureDueSiblings(task: Task): List<Task> {
        return if (task.repeatGroup != null)
            database.taskDao.getFutureDueSiblings(task.repeatGroup!!, task.due).map { it.asDomainModel() }
        else listOf()
    }

    suspend fun getTasksForNotification(): List<Task> {
        return database.taskDao.getTasksForNotification().map {
            it.asDomainModel()
        }
    }

    suspend fun getTask(id: Long): Task? {
        return database.taskDao.getTask(id)?.asDomainModel()
    }

    fun getTaskLiveData(id: Long): LiveData<Task> {
        return Transformations.map(database.taskDao.getTaskLiveData(id)) {
            it.asDomainModel()
        }
    }

    suspend fun getTasksForWidget(): List<Task> {
        return database.taskDao.getTasksForWidget().map {
            it.asDomainModel()
        }
    }

    suspend fun getNextRepetitionGroup(): Long {
        return database.taskDao.getNextRepetitionGroup()
    }

    suspend fun saveTask(task: Task): Long {
        return if (task.id == 0L) {
            database.taskDao.insert(task.asDatabaseModel())
        } else {
            database.taskDao.update(task.asDatabaseModel())
            task.id
        }
    }

    suspend fun deleteTask(task: Task) {
        database.taskDao.delete(task.asDatabaseModel())
    }

    suspend fun deleteDoneTasks() {
        database.taskDao.deleteDoneTasks()
    }
}
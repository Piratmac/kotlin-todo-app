package com.piratmac.todo.views

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.piratmac.todo.R
import com.piratmac.todo.data.database.getDatabase
import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task
import com.piratmac.todo.use_cases.*
import com.piratmac.todo.views.helpers.NotificationGenerator
import com.piratmac.todo.views.helpers.TodoPendingIntent
import com.piratmac.todo.views.helpers.getNotificationGenerator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val IntentActionNotificationToSend = "com.piratmac.todo.send_notification"
const val IntentActionMarkTaskAsDone = "com.piratmac.todo.mark_done"
const val IntentActionToggleTaskDone = "com.piratmac.todo.toggle_done"
const val IntentActionSnoozeTask = "com.piratmac.todo.snooze_task"
const val IntentActionTaskOpenDetails = "com.piratmac.todo.open_details"

const val IntentNotificationId = "com.piratmac.todo.notification_id"
const val IntentNotificationContents = "com.piratmac.todo.notification_contents"
const val IntentTaskId = "com.piratmac.todo.task_id"

class ActionsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent.getIntExtra(IntentNotificationId, 0)

        when (intent.action) {
            // Re-creates all alarms related to notifications
            Intent.ACTION_BOOT_COMPLETED -> {
                GlobalScope.launch { scheduleNotifications(context) }
            }

            // Send notifications
            IntentActionNotificationToSend -> {
                val notification: Notification? =
                    intent.getParcelableExtra(IntentNotificationContents)

                notificationManager.notify(notificationId, notification)
            }

            // Open details of a given task (used from notification and from widget)
            IntentActionTaskOpenDetails -> {
                val taskId = intent.getLongExtra(IntentTaskId, 0)

                if (taskId == 0L)
                    return

                TodoPendingIntent().forTaskDetailsById(context, taskId).send()
                notificationManager.cancel(notificationId)
            }

            // Mark task as done (used from notification)
            IntentActionMarkTaskAsDone -> {
                val tasksRepository = TasksRepository(getDatabase(context.applicationContext))
                val notificationGenerator = getNotificationGenerator(context)
                val taskId = intent.getLongExtra(IntentTaskId, 0)
                if (taskId == 0L)
                    return

                runBlocking {
                    GetTask(tasksRepository).execute(GetTask.Request(taskId))
                        .mapSuccess {
                            runBlocking {
                                markTaskDone(context, tasksRepository, notificationGenerator, it)
                            }
                        }
                        .mapSuccess { TodoPendingIntent().forWidgetUpdate(context).send(); it }
                        .mapError { reportError(context, R.string.error_mark_task_done, it) }
                }
                notificationManager.cancel(notificationId)
            }

            // Mark task as done / undone (used from notification and from widget)
            IntentActionToggleTaskDone -> {
                val tasksRepository = TasksRepository(getDatabase(context.applicationContext))
                val notificationGenerator = getNotificationGenerator(context)
                val taskId = intent.getLongExtra(IntentTaskId, 0)
                if (taskId == 0L)
                    return

                runBlocking {
                    GetTask(tasksRepository).execute(GetTask.Request(taskId))
                        .andThen {
                            if (it.done) {
                                it.done = false
                                saveTaskAndHandleNotification(
                                    context,
                                    notificationGenerator,
                                    tasksRepository,
                                    it
                                )
                            } else
                                runBlocking {
                                    markTaskDone(
                                        context,
                                        tasksRepository,
                                        notificationGenerator,
                                        it
                                    )
                                }
                        }
                        .mapSuccess { TodoPendingIntent().forWidgetUpdate(context).send(); it }
                        .mapError { reportError(context, R.string.error_mark_task_done, it) }
                }
            }

            IntentActionSnoozeTask -> {
                val tasksRepository = TasksRepository(getDatabase(context.applicationContext))
                val notificationGenerator = getNotificationGenerator(context)
                val taskId = intent.getLongExtra(IntentTaskId, 0)
                GlobalScope.launch {
                    SnoozeTask(tasksRepository).execute(SnoozeTask.Request(taskId, 5 * 60))
                        .andThen {
                            saveTaskAndHandleNotification(
                                context, notificationGenerator, tasksRepository, it
                            )
                        }
                        .mapSuccess { TodoPendingIntent().forWidgetUpdate(context).send(); it }
                        .mapError { reportError(context, R.string.error_snooze_task, it) }
                }
                notificationManager.cancel(notificationId)
            }
        }
    }

    private suspend fun markTaskDone(
        context: Context,
        tasksRepository: TasksRepository,
        notificationGenerator: NotificationGenerator,
        task: Task
    ): Result<Pair<Task, Task?>, String> {
        return SetTaskDone(tasksRepository).execute(TaskRequest(task))
            .andThen { (oldTask, newTask) ->
                saveTaskAndHandleNotification(
                    context, notificationGenerator, tasksRepository, oldTask
                )
                    .mapSuccess { Pair(it, newTask) }
            }
            .andThen { (oldTask, newTask) ->
                if (newTask == null)
                    Success(Pair(oldTask, null))
                else
                    saveTaskAndHandleNotification(
                        context, notificationGenerator, tasksRepository, newTask
                    )
                        .mapSuccess { Pair(oldTask, it) }
            }
            .mapError { reportError(context, R.string.error_mark_task_done, it); it }
    }

    private fun saveTaskAndHandleNotification(
        context: Context,
        notificationGenerator: NotificationGenerator,
        tasksRepository: TasksRepository,
        it: Task
    ): Result<Task, String> {
        return runBlocking {
            SaveTask(tasksRepository).execute(TaskRequest(it))
                .mapSuccess {
                    if (it.done)
                        notificationGenerator.deleteNotificationForTaskDue(context, it)
                    else {
                        notificationGenerator.scheduleNotificationForTaskDue(context, it)
                    }
                    it
                }
        }
    }

    private fun reportError(context: Context, messageId: Int, it: String) {
        Log.e("ActionsBroadcastReceiver", context.getString(messageId).format(it))
    }

    private suspend fun scheduleNotifications(context: Context) {
        val tasksRepository = TasksRepository(getDatabase(context))
        GetTasksForNotification(tasksRepository).execute()
            .mapSuccess { tasks ->
                context.let { context ->
                    val notificationGenerator = getNotificationGenerator(context)
                    tasks.forEach { task ->
                        notificationGenerator.scheduleNotificationForTaskDue(context, task)
                    }
                }
                tasks
            }
            .mapError {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_creating_notification_schedule).format(it),
                    Toast.LENGTH_SHORT
                )
            }
    }

}


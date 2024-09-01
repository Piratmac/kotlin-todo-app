package com.piratmac.todo.views.helpers

import android.app.Notification
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.piratmac.todo.R
import com.piratmac.todo.models.Task
import com.piratmac.todo.views.*
import com.piratmac.todo.views.widget.TodoListWidgetProvider


class TodoPendingIntent {
    // Pending Intent for marking a task as done
    fun forTaskMarkDone(context: Context, task: Task, notificationIdToDismiss: Int = 0): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            TodoIntent().forTaskMarkDone(context, task, notificationIdToDismiss),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    // Pending Intent for toggling the done/not done flag on a task
    fun forTaskToggleDone(context: Context, task: Task, notificationIdToDismiss: Int = 0): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            TodoIntent().forTaskToggleDone(context, task, notificationIdToDismiss),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Pending intent for snoozing a task
    fun forTaskSnooze(context: Context, task: Task, notificationIdToDismiss: Int = 0): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            TodoIntent().forTaskSnooze(context, task, notificationIdToDismiss),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    // Pending intent for opening the task details page
    fun forTaskDetails(context: Context, task: Task, notificationIdToDismiss: Int = 0): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            TodoIntent().forTaskDetails(context, task, notificationIdToDismiss),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Pending intent for opening the task details page
    fun forTasksDeleteDone(context: Context, notificationIdToDismiss: Int = 0): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            TodoIntent().forTasksDeleteDone(context, notificationIdToDismiss),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Pending intent for opening the task details page
    fun forTaskDetailsById(context: Context, taskId: Long): PendingIntent {
        return NavDeepLinkBuilder(context.applicationContext)
            .setGraph(R.navigation.navigation)
            .setDestination(R.id.taskDetailsFragment)
            .setArguments(bundleOf("taskId" to taskId))
            .createPendingIntent()
    }

    // Intent for the Alarm, so that it triggers a notification at the end
    fun forNotificationPublisher(
        context: Context,
        notificationId: Int,
        notification: Notification
    ): PendingIntent {
        val intent = Intent()
            .apply {
                setClass(context, ActionsBroadcastReceiver::class.java)
                identifier = notificationId.toString()
                action = IntentActionNotificationToSend
                putExtra(IntentNotificationId, notificationId)
                putExtra(IntentNotificationContents, notification)
            }
        return PendingIntent.getBroadcast(context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // Update widgets
    fun forWidgetUpdate(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(context, 0, TodoIntent().forWidgetUpdate(context),
            PendingIntent.FLAG_IMMUTABLE)
    }

    // Empty pending intent (used for the widget's ListView)
    fun forBroadcast(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            TodoIntent().forBroadcast(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}


class TodoIntent : Intent() {
    private fun forTaskAction (context: Context, task: Task, notificationIdToDismiss: Int = 0, intentAction: String): Intent {
        return this.apply {
            setClass(context, ActionsBroadcastReceiver::class.java)
            identifier = intentAction + task.id.toString()
            action = intentAction
            putExtra(IntentTaskId, task.id)
            if (notificationIdToDismiss != 0)
                putExtra(IntentNotificationId, notificationIdToDismiss)
        }
    }

    // Intent for marking a task as done
    fun forTaskMarkDone(context: Context, task: Task, notificationIdToDismiss: Int = 0): Intent {
        return this.forTaskAction(context, task, notificationIdToDismiss, IntentActionMarkTaskAsDone)
    }

    // Intent for marking a task as done
    fun forTaskToggleDone(context: Context, task: Task, notificationIdToDismiss: Int = 0): Intent {
        return this.forTaskAction(context, task, notificationIdToDismiss, IntentActionToggleTaskDone)
    }

    // Intent for snoozing a task
    fun forTaskSnooze(context: Context, task: Task, notificationIdToDismiss: Int = 0): Intent {
        return this.forTaskAction(context, task, notificationIdToDismiss, IntentActionSnoozeTask)
    }

    // Intent for opening the task details page
    fun forTaskDetails(context: Context, task: Task, notificationIdToDismiss: Int = 0): Intent {
        return this.forTaskAction(context, task, notificationIdToDismiss, IntentActionTaskOpenDetails)
    }

    // Intent for deleting completed tasks
    fun forTasksDeleteDone(context: Context, notificationIdToDismiss: Int = 0): Intent {
        return this.forTaskAction(context, Task(0), notificationIdToDismiss, IntentActionTasksDeleteDone)
    }

    // Update widgets
    fun forWidgetUpdate(context: Context): Intent {
        val ids: IntArray = AppWidgetManager.getInstance(context.applicationContext)
            .getAppWidgetIds(
                ComponentName(context.applicationContext, TodoListWidgetProvider::class.java)
            )
        return Intent(context, TodoListWidgetProvider::class.java)
            .apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
    }

    // Empty intent (used for the widget's ListView)
    fun forBroadcast(context: Context): Intent {
        return this.setClass(context, ActionsBroadcastReceiver::class.java)
    }
}


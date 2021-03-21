package com.piratmac.todo.views.helpers

import android.app.*
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.piratmac.todo.R
import com.piratmac.todo.models.Task
import java.time.LocalDateTime
import java.time.ZoneId

class NotificationGenerator(context: Context) {

    private var alarmMgr: AlarmManager? = null

    init {
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun createNotificationChannel(
        context: Context,
        channelId: String,
        name: String,
        description: String, importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        showBadge: Boolean = false
    ) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotificationTemplate(
        context: Context,
        channelId: String,
        title: String,
        message: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_alarm)
            setContentTitle(title)
            setContentText(message)
        }
    }

    fun scheduleNotificationForTaskDue(context: Context, task: Task) {
        // Task due in the past ==> ignore
        if (!task.due.isAfter(LocalDateTime.now())) return

        //  Build the notification from a template
        val notificationBuilder = buildNotificationTemplate(
            context,
            context.getString(R.string.notification_task_due_channel_id),
            task.label,
            task.details
        )

        val notificationId = task.id.toInt()

        // Get the task's due time in the phone's UTC offset
        val triggerTimeMilli = task.due.toInstant(
            ZoneId.systemDefault().rules.getOffset(
                LocalDateTime.now()
            )
        ).toEpochMilli()

        // Update the notification template with the specific needs of this task
        notificationBuilder.apply {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setCategory(NotificationCompat.CATEGORY_EVENT)
            setAutoCancel(true)
            setWhen(triggerTimeMilli)

            // Determines what will be opened when clicking on the notification
            setContentIntent(TodoPendingIntent().forTaskDetails(context, task, notificationId))
            addAction(0, context.getString(R.string.task_mark_done), TodoPendingIntent().forTaskMarkDone(context, task, notificationId))
            addAction(0, context.getString(R.string.task_snooze), TodoPendingIntent().forTaskSnooze(context, task, notificationId))
        }

        // Create the intent for the alarm
        val notification = notificationBuilder.build()
        val notificationIntent =
            TodoPendingIntent().forNotificationPublisher(context, notificationId, notification)

        // Triggers the alarm
        alarmMgr?.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMilli,
            notificationIntent
        )
    }

    fun deleteNotificationForTaskDue(context: Context, task: Task) {
        val notificationBuilder = buildNotificationTemplate(
            context,
            context.getString(R.string.notification_task_due_channel_id),
            task.label,
            task.details
        )
        val notification = notificationBuilder.build()

        // Remove the alarm
        alarmMgr?.cancel(
            TodoPendingIntent().forNotificationPublisher(context, task.id.toInt(), notification)
        )
    }
}

private lateinit var INSTANCE: NotificationGenerator

fun getNotificationGenerator(context: Context): NotificationGenerator {
    if (!::INSTANCE.isInitialized) {
        INSTANCE = NotificationGenerator(context)
    }

    return INSTANCE
}

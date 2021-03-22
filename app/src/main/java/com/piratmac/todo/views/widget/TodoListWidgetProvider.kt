package com.piratmac.todo.views.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.piratmac.todo.R
import com.piratmac.todo.views.MainActivity
import com.piratmac.todo.views.helpers.TodoPendingIntent

class TodoListWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            // Create an intent for creation of new tasks
            val pendingIntentForNewTask: PendingIntent =
                TodoPendingIntent().forTaskDetailsById(context, 0L)

            // Create an intent for deleting done tasks
            val pendingIntentForTaskDelete: PendingIntent =
                TodoPendingIntent().forTasksDeleteDone(context)

            // Create an intent for opening task details
            val pendingIntentForTaskDetails = TodoPendingIntent().forBroadcast(context)

            // Create an intent for opening full screen
            val pendingIntentForFullscreen: PendingIntent =
                Intent(context, MainActivity::class.java)
                    .let { intent ->
                        PendingIntent.getActivity(context, 0, intent, 0)
                    }

            // Create an intent for refreshing the widget -> Not needed as it works well without it
            /*val pendingIntentForRefresh: PendingIntent =
                TodoPendingIntent().forWidgetUpdate(context)*/

            // Create an intent for updating the data
            val pendingIntentForTodoList =
                Intent(context, TodoListWidgetService::class.java).apply {
                    // Add the app widget ID to the intent extras.
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }

            // Assign intents to the buttons
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget_list
            ).apply {
                setRemoteAdapter(R.id.todoList, pendingIntentForTodoList)
                setPendingIntentTemplate(R.id.todoList, pendingIntentForTaskDetails)
               // setOnClickPendingIntent(R.id.refresh_widget, pendingIntentForRefresh)
                setOnClickPendingIntent(R.id.delete_done_tasks, pendingIntentForTaskDelete)
                setOnClickPendingIntent(R.id.add_task, pendingIntentForNewTask)
                setOnClickPendingIntent(R.id.open_fullscreen, pendingIntentForFullscreen)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.todoList)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}



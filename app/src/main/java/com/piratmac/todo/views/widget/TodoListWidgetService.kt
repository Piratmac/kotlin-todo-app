package com.piratmac.todo.views.widget

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.Toast
import com.piratmac.todo.R
import com.piratmac.todo.data.database.getDatabase
import com.piratmac.todo.data.repository.TasksRepository
import com.piratmac.todo.models.Task
import com.piratmac.todo.models.TodoListItem
import com.piratmac.todo.use_cases.GetTasksForWidget
import com.piratmac.todo.use_cases.mapError
import com.piratmac.todo.use_cases.mapSuccess
import com.piratmac.todo.views.helpers.TodoIntent
import com.piratmac.todo.views.helpers.listHeaders
import kotlinx.coroutines.runBlocking


class TodoListWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodoListRemoteViewsFactory(this.applicationContext)
    }

    class TodoListRemoteViewsFactory(
        private val context: Context
    ) : RemoteViewsFactory {

        private val tasksRepository = TasksRepository(getDatabase(context))
        private lateinit var visibleTasks: List<Task>
        private lateinit var listOfItems: MutableList<TodoListItem>

        override fun onCreate() {
            listOfItems =
                listHeaders.keys.map { TodoListItem.TodoListItemHeader(it) }.toMutableList()
        }

        override fun onDestroy() {
        }

        override fun onDataSetChanged() {
            val listHeadersAsHeaders =
                listHeaders.keys.map { TodoListItem.TodoListItemHeader(it) }
            listOfItems = listHeadersAsHeaders.toMutableList()

            runBlocking {
                GetTasksForWidget(tasksRepository).execute()
                    .mapSuccess {
                        visibleTasks = it
                        val listTasksAsTodoListItems =
                            it.map { t -> TodoListItem.TodoListItemTask(t) }
                        listOfItems = listOfItems.plus(listTasksAsTodoListItems).toMutableList()

                        listOfItems.sort()
                        it
                    }
                    .mapError {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_load_tasks).format(it),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

        override fun getCount(): Int {
            return listOfItems.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            return when (listOfItems[position]) {
                is TodoListItem.TodoListItemTask -> {
                    val task = (listOfItems[position] as TodoListItem.TodoListItemTask).taskAsTask

                    val remoteView =
                        RemoteViews(context.packageName, R.layout.widget_list_item_task)

                    remoteView.setTextViewText(R.id.widgetTaskLabel, getViewLabel(task))

                    setViewIfTaskDone(task, remoteView)
                    setTaskClickListeners(task, remoteView)

                    remoteView
                }
                is TodoListItem.TodoListItemHeader -> {
                    val remoteView =
                        RemoteViews(context.packageName, R.layout.widget_list_item_header)
                    (listOfItems[position] as TodoListItem.TodoListItemHeader).setLabel(context)

                    remoteView.setTextViewText(
                        R.id.headerLabel,
                        (listOfItems[position] as TodoListItem.TodoListItemHeader).label
                    )

                    remoteView
                }
            }
        }

        private fun setTaskClickListeners(
            task: Task,
            remoteView: RemoteViews
        ) {
            val intentForTaskToggleDone = TodoIntent().forTaskToggleDone(context, task)
            val intentForTaskDetails = TodoIntent().forTaskDetails(context, task)

            remoteView.setOnClickFillInIntent(R.id.widgetTaskButton, intentForTaskToggleDone)
            remoteView.setOnClickFillInIntent(R.id.widgetTaskLabel, intentForTaskDetails)
        }

        private fun getViewLabel(
            task: Task
        ): String {
            return if (task.isRepeating) task.label + "  ↺"
            else task.label

        }

        private fun setViewIfTaskDone(
            task: Task,
            remoteView: RemoteViews
        ) {
            // Apply specific "theme" for task completed (we can't use styles.xml)
            if (!task.done) {
                remoteView.apply {
                    setImageViewResource(R.id.widgetTaskButton, R.drawable.ic_checkbox_off)

                    setTextColor(
                        R.id.widgetTaskLabel,
                        context.resources.getColor(R.color.white, context.theme)
                    )

                    setInt(
                        R.id.widgetTaskLabel,
                        "setPaintFlags",
                        Paint.ANTI_ALIAS_FLAG
                    )
                }
            } else {
                remoteView.apply {
                    setImageViewResource(R.id.widgetTaskButton, R.drawable.ic_checkbox_on)

                    setTextColor(
                        R.id.widgetTaskLabel,
                        context.resources.getColor(R.color.gray, context.theme)
                    )
                    setInt(
                        R.id.widgetTaskLabel,
                        "setPaintFlags",
                        Paint.STRIKE_THRU_TEXT_FLAG
                    )
                }
            }
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 2
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }
    }
}
package com.piratmac.todo.models

import android.content.Context
import com.piratmac.todo.views.helpers.aLongTimeAgo
import com.piratmac.todo.views.helpers.listHeaders
import java.time.LocalDate
import java.util.*


sealed class TodoListItem: Comparable<TodoListItem> {
    abstract val id: String
    abstract val dayGroup: LocalDate

    // Overloading operators for easier sort
    override fun compareTo(other: TodoListItem): Int {
        when {
            // Both items are headers, so sort by date
            (this is TodoListItemHeader) and (other is TodoListItemHeader) -> {
                return if (this.dayGroup < other.dayGroup) -1 else 1
            }
            // this is a Header, so it must be sorted before all tasks of the same date
            (this is TodoListItemHeader) and (other is TodoListItemTask) -> {
                return when {
                    this.dayGroup.isBefore(other.dayGroup) -> -1
                    this.dayGroup.isAfter(other.dayGroup) -> 1
                    else -> -1
                }
            }
            // This is the opposite of the previous one
            // other is a Header, so it must be sorted before all tasks of the same date
            (this is TodoListItemTask) and (other is TodoListItemHeader) -> {
                return when {
                    this.dayGroup.isBefore(other.dayGroup) -> -1
                    this.dayGroup.isAfter(other.dayGroup) -> 1
                    else -> 1 // 1 instead of -1 because other should be before if the date is identical
                }
            }
            // Both this and other are tasks
            (this is TodoListItemTask) and (other is TodoListItemTask) -> {
                this as TodoListItemTask
                other as TodoListItemTask
                // Tasks are due on the same group of days ==> sort by done/not done, then time, then label
                if (this.dayGroup == other.dayGroup) {
                    return when {
                        !this.task.done and other.task.done -> -1
                        this.task.done and !other.task.done -> 1
                        this.task.label < other.task.label -> -1
                        this.task.label > other.task.label -> 1
                        else -> 0
                    }
                }
                return when {
                    // Both items are undone tasks, so we sort by date then label
                    this.task.due < other.task.due -> -1
                    this.task.due > other.task.due -> 1
                    this.task.label < other.task.label -> -1
                    this.task.label > other.task.label -> 1
                    else -> 0
                }
            }
            else -> return 0
        }
    }

    data class TodoListItemTask(val task: Task) : TodoListItem() {
        override val id = task.id.toString()
        override lateinit var dayGroup: LocalDate
        val taskAsTask = task

        private var previousDate: LocalDate = aLongTimeAgo

        init {
            // By default, it goes to "Later"
            dayGroup = Collections.max(listHeaders.keys)
            for (date in listHeaders.keys) {
                if (task.due.toLocalDate().isBefore(date)) {
                    this.dayGroup = previousDate
                    break
                }
                previousDate = date
            }
        }
    }

    data class TodoListItemHeader(val due: LocalDate) : TodoListItem() {
        override val id = due.toString()
        override var dayGroup: LocalDate = due
        var label: String = ""

        fun setLabel(context: Context) {
            label = context.getString(listHeaders[dayGroup]!!)
        }
    }
}

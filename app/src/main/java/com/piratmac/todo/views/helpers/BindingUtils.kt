package com.piratmac.todo.views.helpers

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.piratmac.todo.R
import com.piratmac.todo.models.TASK_DATETIME_LATER
import com.piratmac.todo.models.Task
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

/********************************* Bindings used in TodoListFragment ************************************/

// Label + Due date/time + Recurrence
@BindingAdapter("taskLabel")
fun TextView.setTaskLabel(task: Task) {
    val taskLabel = SpannableStringBuilder(task.label)

    if (task.isRepeating) {
        // Get image - the size was empirically determined to fit
        val imageSpan = context?.let {
            ImageSpan(it, R.drawable.ic_recurrence_on_small, DynamicDrawableSpan.ALIGN_CENTER)
        }

        taskLabel.append("   ")
        taskLabel.setSpan(
            imageSpan,
            taskLabel.length - 1,
            taskLabel.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }

    if (task.notifyWhenDue) {
        // First, determine how to display the due time
        val dueDay = task.due.truncatedTo(ChronoUnit.DAYS)
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)

        var dueTimeText = when {
            dueDay.isBefore(today) -> ""
            dueDay.isEqual(today) -> task.due.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            dueDay.isBefore(today.plusDays(7)) -> task.due.format(DateTimeFormatter.ofPattern("E ")) + task.due.format(
                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            )
            else -> task.due.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        }

        if (dueTimeText != "") {
            dueTimeText = "     ($dueTimeText)"

            val dueTimeLabel = SpannableStringBuilder(dueTimeText)

            // Then, apply text appearance
            val timeLength = dueTimeText.length
            dueTimeLabel.setSpan(
                StyleSpan(Typeface.ITALIC),
                0,
                timeLength,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            dueTimeLabel.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.gray)),
                0,
                timeLength,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            dueTimeLabel.setSpan(
                RelativeSizeSpan(0.8f),
                0,
                timeLength,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            taskLabel.append(dueTimeLabel)
        }
    }

    text = taskLabel
}


// Defines the style of tasks depending on whether they're done or not
@BindingAdapter("taskDoneStyling")
fun TextView.taskDoneStyling(item: Task) {
    if (item.done) {
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        setTextAppearance(R.style.TodoList_Task_Done)
    } else {
        paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        setTextAppearance(R.style.TodoList_Task_NotDone)
    }
}


/********************************* Bindings used in TaskDetailsFragment ************************************/

// Due date
@BindingAdapter("taskDueDate")
fun TextView.setTaskDueDate(item: Task) {
    text = if (item.due == TASK_DATETIME_LATER)
        ""
    else
        item.due.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
}

// Due time
@BindingAdapter("taskDueTime")
fun TextView.setTaskDueTime(item: Task) {
    text = if (item.due == TASK_DATETIME_LATER)
        ""
    else
        item.due.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
}

// Delete Date Time button
@BindingAdapter("taskDueDateTimeDelete")
fun Button.setTaskDueDateTimeDelete(item: Task) {
    visibility = if (item.due == TASK_DATETIME_LATER)
        View.INVISIBLE
    else
        View.VISIBLE
}

// Is notification active?
@BindingAdapter("taskNotify")
fun ImageButton.setTaskNotify(item: Task) {
    if (item.notifyWhenDue) {
        setImageState(
            intArrayOf(-R.attr.alarm_add),
            true)
        setImageState(
            intArrayOf(R.attr.alarm_on),
            true)
    } else {
        setImageState(
            intArrayOf(R.attr.alarm_add),
            true)
        setImageState(
            intArrayOf(-R.attr.alarm_on),
            true)
    }
}

// Is recurrence active?
@BindingAdapter("taskRecurrence")
fun ImageButton.setTaskRecurrence(item: Task) {
    if (item.isRepeating) {
        setImageState(
            intArrayOf(R.attr.recurrence_on),
            true)
    } else {
        setImageState(
            intArrayOf(-R.attr.recurrence_on),
            true)
    }
}

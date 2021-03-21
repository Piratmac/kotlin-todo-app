package com.piratmac.todo.views.helpers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.piratmac.todo.R
import com.piratmac.todo.databinding.ListItemHeaderBinding
import com.piratmac.todo.databinding.ListItemTaskBinding
import com.piratmac.todo.models.TASK_DATETIME_LATER
import com.piratmac.todo.models.Task
import com.piratmac.todo.models.TodoListItem
import java.time.LocalDate

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

// Note: All past due tasks are "merged" into Today (I don't care if tasks are late)
val aLongTimeAgo: LocalDate = LocalDate.MIN
private val tomorrowAtMidnight = LocalDate.now().plusDays(1)
private val in2daysAtMidnight = tomorrowAtMidnight.plusDays(1)
private val in3daysAtMidnight = tomorrowAtMidnight.plusDays(2)
private val inALongTime = TASK_DATETIME_LATER.minusDays(1).toLocalDate()
val listHeaders = mapOf(
    aLongTimeAgo to R.string.header_today,
    tomorrowAtMidnight to R.string.header_tomorrow,
    in2daysAtMidnight to R.string.header_in2days,
    in3daysAtMidnight to R.string.header_later,
    inALongTime to R.string.header_one_day,
)

class TodoListItemAdapter(
    private val onItemRadioButtonClick: (Task) -> Unit,
    private val onItemLabelClick: (Task) -> Unit
) : ListAdapter<TodoListItem, RecyclerView.ViewHolder>(TodoListItemDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val task = getItem(position) as TodoListItem.TodoListItemTask
                holder.bind(task, onItemRadioButtonClick, onItemLabelClick)
            }
            is TextViewHolder -> holder.bind(getItem(position) as TodoListItem.TodoListItemHeader)
            else -> throw ClassCastException("Unknown viewType")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TodoListItem.TodoListItemHeader -> ITEM_VIEW_TYPE_HEADER
            is TodoListItem.TodoListItemTask -> ITEM_VIEW_TYPE_ITEM
        }
    }

    fun addHeaders(tasksList: List<Task>): MutableList<TodoListItem> {
        val listHeadersAsHeaders = listHeaders.keys.map { TodoListItem.TodoListItemHeader(it) }
        val listTasksAsTodoListItems = tasksList.map { TodoListItem.TodoListItemTask(it) }

        val listOfItems =
            (listHeadersAsHeaders + listTasksAsTodoListItems) as MutableList<TodoListItem>

        listOfItems.sort()
        return listOfItems
    }

    class TextViewHolder private constructor(private val binding: ListItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: TodoListItem.TodoListItemHeader
        ) {
            item.setLabel(itemView.context)
            binding.header = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemHeaderBinding.inflate(layoutInflater, parent, false)
                return TextViewHolder(binding)
            }
        }
    }

    class ViewHolder private constructor(private val binding: ListItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: TodoListItem.TodoListItemTask,
            onItemRadioButtonClick: (Task) -> Unit?,
            onItemLabelClick: (Task) -> Unit?
        ) {
            val task = item.taskAsTask
            binding.task = task
            binding.executePendingBindings()
            binding.taskButton.setOnClickListener { onItemRadioButtonClick(task) }
            binding.taskLabel.setOnClickListener { onItemLabelClick(task) }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemTaskBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}


class TodoListItemDiffCallback : DiffUtil.ItemCallback<TodoListItem>() {
    override fun areItemsTheSame(oldItem: TodoListItem, newItem: TodoListItem): Boolean {
        return oldItem.id == newItem.id
    }


    override fun areContentsTheSame(oldItem: TodoListItem, newItem: TodoListItem): Boolean {
        return oldItem == newItem
    }
}

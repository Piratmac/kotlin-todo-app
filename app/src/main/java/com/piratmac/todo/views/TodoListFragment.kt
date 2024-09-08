package com.piratmac.todo.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.piratmac.todo.R
import com.piratmac.todo.databinding.FragmentTodoListBinding
import com.piratmac.todo.models.Task
import com.piratmac.todo.views.helpers.getNotificationGenerator
import com.piratmac.todo.views.helpers.TodoListItemAdapter
import com.piratmac.todo.view_models.TodoListViewModel
import com.piratmac.todo.views.dialogs.SettingsFragment
import com.piratmac.todo.views.helpers.TodoIntent

class TodoListFragment : Fragment() {

    private val todoListViewModel: TodoListViewModel by lazy {
        requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this)[TodoListViewModel::class.java]
    }

    private lateinit var fragmentTodoListBinding: FragmentTodoListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTodoListBinding.inflate(
            inflater,
            container,
            false)

        fragmentTodoListBinding = binding

        binding.lifecycleOwner = viewLifecycleOwner
        binding.todoListViewModel = todoListViewModel

        binding.deleteDoneTasks.setOnClickListener { onDeleteDoneTasksClick() }
        binding.settings.setOnClickListener { onSettingsClick() }
        binding.addNew.setOnClickListener { onAddNewTaskClick() }

        // Bind the RecyclerView to the data
        val adapter =
            TodoListItemAdapter({ item -> todoListViewModel.onItemRadioButtonClick(item) },
                { item -> onItemLabelClick(item) })
        binding.tasksList.adapter = adapter


        // List of tasks is updated ==> update the view
        todoListViewModel.visibleTasks.observe(viewLifecycleOwner) {
            it?.let {
                val todoListItems = adapter.addHeaders(it)
                adapter.submitList(todoListItems)
            }
            context?.let { it1 -> it1.sendBroadcast(TodoIntent().forWidgetUpdate(it1)) }
        }

        // Task is moved (most likely marked as done) ==> update view
        todoListViewModel.taskMoved.observe(viewLifecycleOwner) { taskIt ->
            taskIt?.let { task ->
                val newTodoListItems = adapter.addHeaders(todoListViewModel.visibleTasks.value!!)
                val fromPosition = adapter.currentList.indexOfFirst { task.id.toString() == it.id }
                val toPosition = newTodoListItems.indexOfFirst { task.id.toString() == it.id }

                adapter.notifyItemChanged(fromPosition)
                adapter.notifyItemChanged(toPosition)
                adapter.notifyItemMoved(fromPosition, toPosition)
            }
        }

        // Schedule notification for new tasks (for repeating tasks, when marked done & new task created)
        todoListViewModel.scheduleNotificationForTask.observe(viewLifecycleOwner) { task ->
            context?.let { context ->
                val notificationGenerator = getNotificationGenerator(context)
                notificationGenerator.scheduleNotificationForTaskDue(
                    context,
                    task
                )
            }
        }

        // Remove notifications from done tasks
        todoListViewModel.deleteNotificationForTask.observe(viewLifecycleOwner) { task ->
            context?.let { context ->
                val notificationGenerator = getNotificationGenerator(context)
                notificationGenerator.deleteNotificationForTaskDue(
                    context,
                    task
                )
            }
        }

        return binding.root
    }


    private fun onDeleteDoneTasksClick() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(R.string.confirm_tasks_deletion_title)
            .setMessage(R.string.confirm_tasks_deletion_message)
            .setPositiveButton(android.R.string.ok) { _, _ -> todoListViewModel.deleteDoneTasks() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun onSettingsClick() {
        val settingsFragment = SettingsFragment()
        settingsFragment.show(activity?.supportFragmentManager!!, "settingsDialog")
    }

    private fun onAddNewTaskClick() {
        this.findNavController()
            .navigate(
                TodoListFragmentDirections.actionTodoListFragmentToTaskDetailsFragment(0L)
            )
    }

    private fun onItemLabelClick(item: Task) {
        this.findNavController()
            .navigate(
                TodoListFragmentDirections.actionTodoListFragmentToTaskDetailsFragment(item.id)
            )
    }
}
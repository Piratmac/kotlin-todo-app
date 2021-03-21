package com.piratmac.todo.views

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.piratmac.todo.R
import com.piratmac.todo.databinding.FragmentTaskDetailsBinding
import com.piratmac.todo.models.TASK_DATETIME_LATER
import com.piratmac.todo.views.helpers.getNotificationGenerator
import com.piratmac.todo.views.dialogs.DatePickerFragment
import com.piratmac.todo.views.dialogs.TimePeriodFragment
import com.piratmac.todo.views.dialogs.TimePickerFragment
import com.piratmac.todo.view_models.TaskDetailsViewModel
import com.piratmac.todo.views.helpers.TodoPendingIntent

class TaskDetailsFragment : Fragment() {

    private val taskDetailsViewModel: TaskDetailsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, TaskDetailsViewModel.Factory(
                activity.application, TaskDetailsFragmentArgs.fromBundle(
                    requireArguments()
                ).taskId
            )
        )
            .get(TaskDetailsViewModel::class.java)
    }

    private lateinit var binding: FragmentTaskDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_task_details,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.taskDetailsViewModel = taskDetailsViewModel

        binding.taskDueDate.setOnClickListener { onTaskDueDateClick() }
        binding.taskDueTime.setOnClickListener { onTaskDueTimeClick() }
        binding.taskDueDateTimeDelete.setOnClickListener { onTaskDeleteDueDateTimeClick() }
        binding.taskNotify.setOnClickListener { onTaskNotifyClick() }
        binding.taskRepetition.setOnClickListener { onTaskRepetitionClick() }

        binding.taskSave.setOnClickListener { taskDetailsViewModel.onSaveTaskClick() }
        binding.taskDelete.setOnClickListener { onTaskDeleteClick() }

        // Task is loaded or modified
        taskDetailsViewModel.taskLiveData.observe(viewLifecycleOwner, {
            taskDetailsViewModel.task = it
            context?.let { it1 -> TodoPendingIntent().forWidgetUpdate(it1).send() }
            binding.invalidateAll()
        })

        // "Save" and "Back" button navigation
        taskDetailsViewModel.navigateToTodoList.observe(viewLifecycleOwner, {
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.taskLabel.windowToken, 0)

            this.findNavController()
                .navigate(TaskDetailsFragmentDirections.actionTaskDetailsFragmentToTodoListFragment())
        })


        // Task needs to schedule notifications
        taskDetailsViewModel.scheduleNotificationForTask.observe(viewLifecycleOwner, { task ->
            context?.let { context ->
                val notificationGenerator = getNotificationGenerator(context)
                notificationGenerator.scheduleNotificationForTaskDue(context, task)
            }
        })

        // Task's scheduled notification to be deleted
        taskDetailsViewModel.deleteNotificationForTask.observe(viewLifecycleOwner, { task ->
            context?.let { context ->
                val notificationGenerator = getNotificationGenerator(context)
                notificationGenerator.deleteNotificationForTaskDue(context, task)
            }
        })
        return binding.root
    }

    // Hide / display keyboard and grant focus
    override fun onResume() {
        super.onResume()

        if (taskDetailsViewModel.task.label == "") {
            binding.taskLabel.requestFocus()

            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.taskLabel, InputMethodManager.SHOW_FORCED)
        } else {
            binding.taskLabel.clearFocus()
        }
    }

    // Click on Due date
    private fun onTaskDueDateClick() {
        val datePickerFragment = DatePickerFragment()
        datePickerFragment.dateChosen.observe(this, { newDate ->
            taskDetailsViewModel.setDueDate(newDate)
            binding.invalidateAll()
        })

        datePickerFragment.show(activity?.supportFragmentManager!!, "datePicker")
    }

    // Click on Due time
    private fun onTaskDueTimeClick() {
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.timeChosen.observe(this, { newTime ->
            taskDetailsViewModel.setDueTime(newTime)
            binding.invalidateAll()
        })

        timePickerFragment.show(activity?.supportFragmentManager!!, "timePicker")
    }
    // Click on Delete due date / time
    private fun onTaskDeleteDueDateTimeClick() {
        taskDetailsViewModel.setDueDateTime(TASK_DATETIME_LATER)
        binding.invalidateAll()
    }

    // Click on Notification
    private fun onTaskNotifyClick() {
        taskDetailsViewModel.toggleNotification()
        binding.invalidateAll()
    }

    // Click on Repetition
    private fun onTaskRepetitionClick() {
        val timePeriodFragment = TimePeriodFragment()
        timePeriodFragment.show(activity?.supportFragmentManager!!, "periodPicker")
        taskDetailsViewModel.task.repeatFrequency?.let { timePeriodFragment.setPeriod(it) }

        timePeriodFragment.periodChosen.observe(this, { period ->
            taskDetailsViewModel.setRepetitionPeriod(period)
            binding.invalidateAll()
        })
    }

    // Click on Deletion
    private fun onTaskDeleteClick() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(R.string.confirm_task_deletion_title)
            .setMessage(R.string.confirm_task_deletion_message)
            .setPositiveButton(android.R.string.ok) { _, _ -> taskDetailsViewModel.onDeleteTaskClick() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            .show()
    }
}

package com.piratmac.todo.views.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.piratmac.todo.models.ActionLiveData
import java.time.LocalTime

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private var _timeChosen = ActionLiveData<LocalTime>()
    val timeChosen: ActionLiveData<LocalTime>
        get() = _timeChosen

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        _timeChosen.value = LocalTime.of(hourOfDay, minute)
    }
}

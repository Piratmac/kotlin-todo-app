package com.piratmac.todo.views.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.piratmac.todo.models.ActionLiveData
import java.time.LocalDate

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private var _dateChosen = ActionLiveData<LocalDate>()
    val dateChosen: ActionLiveData<LocalDate>
        get() = _dateChosen

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        val datePickerDialog = DatePickerDialog(requireContext(), this, year, month, day)
        datePickerDialog.datePicker.minDate = c.timeInMillis

        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        // Month is counted from 0 to 11, so we need to add 1 month to get the real date
        _dateChosen.value = LocalDate.of(year, month + 1, day)
    }
}

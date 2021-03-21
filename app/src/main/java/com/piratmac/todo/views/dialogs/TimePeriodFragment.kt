package com.piratmac.todo.views.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.piratmac.todo.R
import com.piratmac.todo.models.ActionLiveData
import java.time.Period

class TimePeriodFragment : DialogFragment() {
    private var _periodChosen = ActionLiveData<Period>()
    val periodChosen: ActionLiveData<Period>
        get() = _periodChosen

    private lateinit var finalView: View

    private var initPeriodValue: Period = Period.ZERO


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            @SuppressLint("InflateParams")
            finalView = LayoutInflater.from(context).inflate(R.layout.dialog_period_picker, null)

            // Sets the default values
            if (initPeriodValue.years != 0)
                finalView.findViewById<EditText>(R.id.years_value)
                    ?.setText(initPeriodValue.years.toString())
            if (initPeriodValue.months != 0)
                finalView.findViewById<EditText>(R.id.months_value)
                    ?.setText(initPeriodValue.months.toString())
            if (initPeriodValue.days != 0)
                finalView.findViewById<EditText>(R.id.days_value)
                    ?.setText(initPeriodValue.days.toString())

            // Build the dialog
            builder.apply {
                setView(finalView)
                setTitle(R.string.repetition_choose_frequency)

                // "OK" ==> set the selected value and close
                setPositiveButton(
                    android.R.string.ok
                ) { dialog, _ -> onSetPositiveButton(dialog) }

                // "Stop repetition" ==> set the period to 0
                setNeutralButton(
                    R.string.repetition_stop
                ) { dialog, _ ->
                    periodChosen.value = Period.ZERO
                    dialog.cancel()
                }

                // "Cancel" ==> just close
                setNegativeButton(
                    android.R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
            }

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setPeriod(period: Period) {
        initPeriodValue = period
    }

    private fun onSetPositiveButton(dialog: DialogInterface) {
        val yearsEditText =
            finalView.findViewById<EditText>(R.id.years_value).text.toString()
        val monthsEditText =
            finalView.findViewById<EditText>(R.id.months_value).text.toString()
        val daysEditText =
            finalView.findViewById<EditText>(R.id.days_value).text.toString()

        var years = 0
        var months = 0
        var days = 0

        if (yearsEditText != "") {
            years = yearsEditText.toInt()
        }
        if (monthsEditText != "") {
            months = monthsEditText.toInt()
        }
        if (daysEditText != "") {
            days = daysEditText.toInt()
        }


        periodChosen.value = Period.of(years, months, days)
        dialog.dismiss()
    }
}

package com.piratmac.todo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.Period

val TASK_DATETIME_LATER: LocalDateTime = LocalDateTime.of(9999, 12, 31, 12, 0)

@Parcelize
data class Task(
    var id: Long,
    var label: String = "",
    var due: LocalDateTime = LocalDateTime.now(),
    var notifyWhenDue: Boolean = false,
    var done: Boolean = false,
    var details: String = "",
    var repeatFrequency: Period? = Period.ZERO,
    var repeatGroup: Long? = 0,
) : Parcelable {
    val isRepeating
        get() = repeatFrequency != Period.ZERO

}
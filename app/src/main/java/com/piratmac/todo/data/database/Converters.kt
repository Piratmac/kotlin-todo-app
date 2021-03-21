package com.piratmac.todo.data.database

import android.text.TextUtils.split
import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }


    @TypeConverter
    fun periodToString(period: Period?): String {
        if (period == null)
            return "0-0-0"
        return period.years.toString() + "-" + period.months.toString() + "-" + period.days.toString()
    }

    @TypeConverter
    fun stringToPeriod(period: String?): Period {
        if (period == null)
            return Period.ZERO
        val periodAsList = split(period, "-").map { it.toInt() }
        return Period.of(periodAsList[0], periodAsList[1], periodAsList[2])

    }
}

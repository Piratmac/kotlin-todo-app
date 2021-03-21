package com.piratmac.todo.data.database

import androidx.room.*
import com.piratmac.todo.models.Task
import java.time.LocalDateTime
import java.time.Period

@Entity
data class DatabaseTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val label: String,

    val due: LocalDateTime,

    val notifyWhenDue: Boolean = false,

    val done: Boolean = false,

    val details: String,

    @ColumnInfo(name = "repeat_frequency")
    val repeatFrequency: Period?,

    @ColumnInfo(name = "repeat_group")
    val repeatGroup: Long?,
)


fun List<DatabaseTask>.asDomainModel(): List<Task> {
    return map {
        it.asDomainModel()
    }
}

fun DatabaseTask.asDomainModel(): Task {
    return Task(
        id = this.id,
        label = this.label,
        due = this.due,
        notifyWhenDue = this.notifyWhenDue,
        done = this.done,
        details = this.details,
        repeatFrequency = this.repeatFrequency,
        repeatGroup = this.repeatGroup,
    )
}

fun Task.asDatabaseModel(): DatabaseTask {
    return DatabaseTask(
        id = this.id,
        label = this.label,
        notifyWhenDue = this.notifyWhenDue,
        due = this.due,
        done = this.done,
        details = this.details,
        repeatFrequency = this.repeatFrequency,
        repeatGroup = this.repeatGroup,
    )
}

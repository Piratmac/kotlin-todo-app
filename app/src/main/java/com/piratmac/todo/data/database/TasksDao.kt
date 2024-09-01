package com.piratmac.todo.data.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.ABORT
import androidx.sqlite.db.SupportSQLiteQuery
import java.time.LocalDateTime


@Dao
interface TasksDao {
    @Query("SELECT * FROM DatabaseTask")
    fun getTasks(): LiveData<List<DatabaseTask>>

    @Query("SELECT * FROM DatabaseTask WHERE id = :id")
    fun getTaskLiveData(id: Long): LiveData<DatabaseTask>

    @Query("SELECT COUNT(id) FROM DatabaseTask")
    suspend fun getTasksCount(): Long

    @Query("SELECT * FROM DatabaseTask WHERE id = :id")
    suspend fun getTask(id: Long): DatabaseTask?

    @Query("SELECT * FROM DatabaseTask")
    suspend fun getTasksForWidget(): List<DatabaseTask>

    @Query("SELECT * FROM DatabaseTask WHERE repeat_group = :group_id and due > :due")
    suspend fun getFutureDueSiblings(group_id: Long, due: LocalDateTime): List<DatabaseTask>

    @Query("SELECT * FROM DatabaseTask WHERE done = 0 AND notifyWhenDue = 1")
    suspend fun getTasksForNotification(): List<DatabaseTask>

    @Query("SELECT MAX(repeat_group)+1 FROM DatabaseTask")
    suspend fun getNextRepetitionGroup(): Long

    @Query("DELETE FROM DatabaseTask WHERE done = 1")
    suspend fun deleteDoneTasks()

    @Insert(onConflict = ABORT)
    suspend fun insert(task: DatabaseTask): Long

    @Update
    suspend fun update(task: DatabaseTask)

    @Delete
    suspend fun delete(task: DatabaseTask)

    // Used for making sure the DB is consistent before backup
    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
    // Used with myDAO.checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
}

@Database(entities = [DatabaseTask::class], version = 1)
@TypeConverters(Converters::class)
abstract class TasksDatabase : RoomDatabase() {
    abstract val taskDao: TasksDao
}

private lateinit var INSTANCE: TasksDatabase

fun getDatabase(context: Context): TasksDatabase {
    synchronized(TasksDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                TasksDatabase::class.java,
                "tasks"
            ).build()
        }
    }
    return INSTANCE
}

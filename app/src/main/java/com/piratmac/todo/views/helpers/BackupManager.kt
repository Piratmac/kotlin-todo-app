@file:Suppress("unused")

package com.piratmac.todo.views.helpers

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.piratmac.todo.data.database.getDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.*


class BackupManager(val context: Context) {
    private var defaultBackupFolder: File = File(context.getExternalFilesDir(null), "backup")
    private var defaultBackupFile: File = File(defaultBackupFolder, "backup.db")

    private var backupFolder: File = defaultBackupFolder
    private var backupFile: File = defaultBackupFile

    fun setBackupFolder(customBackupFolder: String) {
        backupFolder = File(customBackupFolder)
    }

    fun setBackupFile(customBackupFile: String) {
        backupFile = File(backupFolder, customBackupFile)
    }

    fun backupDatabaseToFile(databaseName: String): Boolean {
        // Get and close DB to get it ready for backup
        val databaseFile = context.getDatabasePath(databaseName)
        val appDatabase = getDatabase(context)

        appDatabase.taskDao.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

        // Create / cleanup existing files
        if (!backupFolder.exists()) {
            backupFolder.mkdirs()
        }
        if (backupFile.exists()) {
            backupFile.delete()
        }

        // Actually backup the DB
        try {
            if (backupFile.createNewFile()) {
                val bufferSize = 8 * 1024
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int
                val backupFileStream = FileOutputStream(backupFile)
                val databaseFileStream = FileInputStream(databaseFile)
                while (databaseFileStream.read(buffer, 0, bufferSize).also { bytesRead = it } > 0) {
                    backupFileStream.write(buffer, 0, bytesRead)
                }
                backupFileStream.flush()
                databaseFileStream.close()
                backupFileStream.close()

                return true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.d("backupDatabaseToFile", "exception: $e")
            return false
        }
        return false
    }

    fun restoreDatabase(databaseName: String): Boolean {
        val backupFileStream = context.contentResolver.openInputStream(backupFile.toUri())!!
        val appDatabase = getDatabase(context)
        appDatabase.close()

        //Delete the existing restoreFile and create a new one.
        val databaseFile = context.getDatabasePath(databaseName)
        return try {
            (backupFileStream).copyTo(FileOutputStream(databaseFile))
            backupFileStream.close()
            true
        } catch (e: IOException) {
            Log.d("restoreDatabase", "Error during restore: $e")
            e.printStackTrace()
            false
        }
    }

    fun deleteRestoreBackupFile() {
        if (backupFile.exists()) {
            backupFile.delete()
        }
    }

    fun validFile(): Boolean {
        val uri = Uri.fromFile(backupFile)

        val test = Files.probeContentType(backupFile.toPath())

        val mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            if (fileExtension.toLowerCase(Locale.ROOT) == "db")
                return true
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.toLowerCase(Locale.ROOT)
            )
        }

        return "application/octet-stream" == mimeType
    }
}
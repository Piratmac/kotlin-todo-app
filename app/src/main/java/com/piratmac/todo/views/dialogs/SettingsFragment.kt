package com.piratmac.todo.views.dialogs

import android.annotation.SuppressLint
import android.app.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.piratmac.todo.R
import com.piratmac.todo.data.database.getDatabase
import com.piratmac.todo.views.helpers.BackupManager
import kotlinx.coroutines.*
import java.io.IOException
import kotlin.system.exitProcess


class SettingsFragment : DialogFragment() {
    private lateinit var finalView: View

    private lateinit var backupManager: BackupManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            @SuppressLint("InflateParams")
            finalView = layoutInflater.inflate(R.layout.dialog_settings, null)

            val backupButton = finalView.findViewById<Button>(R.id.backup)
            backupButton.setOnClickListener { onBackupClick() }

            val restoreButton = finalView.findViewById<Button>(R.id.restore)
            restoreButton.setOnClickListener { onRestoreClick() }

            backupManager = BackupManager(requireContext())
            backupManager.setBackupFile("tasks.db")

            // Build the dialog
            builder.apply {
                setView(finalView)
                setTitle(R.string.settings)

                // "Close" ==> set the selected value and close
                setPositiveButton(
                    R.string.close
                ) { dialog, _ -> dialog.dismiss() }
            }

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun onBackupClick() {
        if (context == null)
            return

        GlobalScope.launch(Dispatchers.IO) {
            val result =
                withContext(Dispatchers.IO) { backupManager.backupDatabaseToFile("tasks") }
            activity?.runOnUiThread {
                if (result)
                    Toast.makeText(context, getString(R.string.backup_success), Toast.LENGTH_SHORT)
                        .show()
                else
                    Toast.makeText(context, getString(R.string.backup_failed), Toast.LENGTH_LONG)
                        .show()
            }
        }
    }

    private fun onRestoreClick() {
        try {
            backupManager.setBackupFile("tasks.db")

            // Check if file exists and is valid
            if (!backupManager.validFile()) {
                Toast.makeText(context, getString(R.string.restore_failed), Toast.LENGTH_LONG)
                    .show()
                return
            }

            GlobalScope.launch(Dispatchers.IO) {
                // First, take a backup of the DB before we restore anything
                backupManager.setBackupFile("tasks_before_restore.db")
                backupManager.backupDatabaseToFile("tasks")

                // Then restore the DB
                backupManager.setBackupFile("tasks.db")
                val result = backupManager.restoreDatabase("tasks")
                if (!result) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            getString(R.string.restore_failed),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                    return@launch
                }
                activity?.runOnUiThread {
                    Toast.makeText(
                        context,
                        getString(R.string.restore_success),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                // Then delete the temporary backup file
                backupManager.setBackupFile("tasks_before_restore.db")
                backupManager.deleteRestoreBackupFile()
                closeApplication()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun closeApplication() {
        val appDatabase = getDatabase(requireContext())
        GlobalScope.launch(Dispatchers.IO) {
            if (appDatabase.taskDao.getTasksCount() > 0) {
                exitProcess(0)
            }
        }
    }

}

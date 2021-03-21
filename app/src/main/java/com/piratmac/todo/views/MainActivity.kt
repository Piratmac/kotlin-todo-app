package com.piratmac.todo.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.piratmac.todo.R
import com.piratmac.todo.views.helpers.getNotificationGenerator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Generate the notification generator and the channel for due tasks
        val notificationGenerator = getNotificationGenerator(applicationContext)
        notificationGenerator.createNotificationChannel(
            applicationContext,
            getString(R.string.notification_task_due_channel_id),
            getString(R.string.notification_task_due_channel_name),
            getString(R.string.notification_task_due_channel_description)
        )
    }
}


//TODO: Image for preview in res/xml/appwidget.xml, attribute android:previewImage
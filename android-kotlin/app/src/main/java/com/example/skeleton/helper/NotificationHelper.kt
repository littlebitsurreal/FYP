package com.example.skeleton.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.example.skeleton.MainActivity
import com.example.skeleton.R

object NotificationHelper {
    fun show(context: Context, title: String, content: String, id: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("default", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Channel Description"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val icon = BitmapFactory.decodeResource(context.resources, R.drawable.elephant)

            val notification = NotificationCompat.Builder(context, "default")
                    .setTicker("Exceed Usage Limit")
                    .setLargeIcon(icon)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setContentIntent(pendingIntent)
                    .build()
            with(NotificationManagerCompat.from(context)) {
                notify(id, notification)
            }
        } else {
            val notification = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.elephant)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setContentIntent(pendingIntent)
                    .build()
            with(NotificationManagerCompat.from(context)) {
                notify(id, notification)
            }
        }
    }
}

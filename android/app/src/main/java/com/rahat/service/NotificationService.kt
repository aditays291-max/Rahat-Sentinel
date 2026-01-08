package com.rahat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rahat.data.model.Alert
import com.rahat.R // Using standard R class, might need to fix package if R is not generated yet

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "rahat_alerts"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Emergency Alerts"
            val descriptionText = "Notifications for nearby disasters"
            val importance = NotificationManager.IMPORTANCE_HIGH // High importance for alerts
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showAlertNotification(alert: Alert) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Generic icon for now
            .setContentTitle("Emergency Alert: ${alert.severity}")
            .setContentText(alert.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            
        // TODO: specific PendingIntent to open AlertFeed

        try {
            notificationManager.notify(alert.id.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Handle missing POST_NOTIFICATIONS permission
        }
    }
}

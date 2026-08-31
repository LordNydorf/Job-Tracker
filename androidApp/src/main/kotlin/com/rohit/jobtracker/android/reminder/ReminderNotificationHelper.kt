package com.rohit.jobtracker.android.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rohit.jobtracker.android.MainActivity
import com.rohit.jobtracker.shared.model.Application

object ReminderNotificationHelper {
    const val CHANNEL_ID = "job_followup_reminders"
    private const val CHANNEL_NAME = "Follow-up Reminders"
    private const val CHANNEL_DESC = "Notifications reminding you to follow up on job applications."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showFollowUpNotification(context: Context, application: Application, daysSinceUpdate: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("applicationId", application.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            application.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Follow up: ${application.company}")
            .setContentText("No update for ${application.role} in $daysSinceUpdate days. Time to send a follow-up?")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "You applied for ${application.role} at ${application.company} via ${application.source.name}. " +
                            "It has been $daysSinceUpdate days with no status update. Tap to view notes or update status."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(application.id.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission not granted yet on Android 13+
        }
    }
}

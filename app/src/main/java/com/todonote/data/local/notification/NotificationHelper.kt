package com.todonote.data.local.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.todonote.MainActivity
import com.todonote.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_TASK_REMINDER = "task_reminder"
        const val CHANNEL_NAME_TASK_REMINDER = "任务提醒"
        const val CHANNEL_DESC_TASK_REMINDER = "任务到期提醒通知"

        const val CHANNEL_ID_HABIT_REMINDER = "habit_reminder"
        const val CHANNEL_NAME_HABIT_REMINDER = "习惯打卡提醒"
        const val CHANNEL_DESC_HABIT_REMINDER = "每日习惯打卡提醒"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val taskChannel = NotificationChannel(
            CHANNEL_ID_TASK_REMINDER,
            CHANNEL_NAME_TASK_REMINDER,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC_TASK_REMINDER
        }

        val habitChannel = NotificationChannel(
            CHANNEL_ID_HABIT_REMINDER,
            CHANNEL_NAME_HABIT_REMINDER,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESC_HABIT_REMINDER
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(listOf(taskChannel, habitChannel))
    }

    fun showTaskReminderNotification(taskId: Long, title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASK_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId.toInt(), notification)
    }

    fun showHabitReminderNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigateTo", "habits")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            10001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_HABIT_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("习惯打卡提醒")
            .setContentText("今天还有习惯没有完成，记得打卡哦！")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(10001, notification)
    }

    fun cancelNotification(taskId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId.toInt())
    }
}

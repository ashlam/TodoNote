package com.todonote.presentation.pomodoro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.todonote.MainActivity
import com.todonote.R

class PomodoroService : Service() {

    companion object {
        const val CHANNEL_ID = "pomodoro_timer"
        const val CHANNEL_NAME = "番茄钟计时器"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.todonote.pomodoro.START"
        const val ACTION_STOP = "com.todonote.pomodoro.STOP"
        const val EXTRA_SESSION_TYPE = "session_type"
        const val EXTRA_TIME_REMAINING = "time_remaining"

        fun startService(context: Context, sessionType: String, timeRemaining: Int) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SESSION_TYPE, sessionType)
                putExtra(EXTRA_TIME_REMAINING, timeRemaining)
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionType = intent.getStringExtra(EXTRA_SESSION_TYPE) ?: "专注"
                val timeRemaining = intent.getIntExtra(EXTRA_TIME_REMAINING, 25 * 60)
                startForeground(NOTIFICATION_ID, buildNotification(sessionType, timeRemaining))
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "番茄钟计时期间保持前台运行"
            setSound(null, null)
            enableVibration(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(sessionType: String, timeRemaining: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        val timeText = "%02d:%02d".format(minutes, seconds)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$sessionType - $timeText")
            .setContentText("番茄钟正在运行中")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }
}

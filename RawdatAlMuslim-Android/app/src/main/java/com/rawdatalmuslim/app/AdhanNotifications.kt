package com.rawdatalmuslim.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object AdhanNotifications {
    const val CHANNEL_ID = "rawdatalmuslim_adhan"
    const val PLAYBACK_ID = 7301

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "الأذان والتنبيهات", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "تنبيهات الصلاة وتشغيل الأذان"
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun playback(context: Context, prayer: String) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle("حان وقت $prayer")
        .setContentText("روضة المسلم - جاري تشغيل الأذان")
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setOngoing(true)
        .setContentIntent(openApp(context))
        .build()

    fun showReminder(context: Context, prayer: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("اقترب موعد الصلاة")
            .setContentText("باقي 10 دقائق على $prayer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp(context))
            .build()
        context.getSystemService(NotificationManager::class.java).notify(7400 + prayer.hashCode().and(0x7ff), n)
    }

    private fun openApp(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(context, 7400, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

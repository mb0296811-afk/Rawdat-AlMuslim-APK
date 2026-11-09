package com.rawdatalmuslim.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("prayer_name") ?: "الصلاة"
        val audio = intent.getStringExtra("audio_file") ?: "اذان عبدالباسط.mp3"
        val reminder = intent.getBooleanExtra("is_reminder", false)
        if (reminder) {
            AdhanNotifications.showReminder(context, name)
        } else {
            val service = Intent(context, AdhanPlaybackService::class.java).apply {
                putExtra("prayer_name", name)
                putExtra("audio_file", audio)
            }
            ContextCompat.startForegroundService(context, service)
            PrayerScheduler.scheduleAll(context)
        }
    }
}

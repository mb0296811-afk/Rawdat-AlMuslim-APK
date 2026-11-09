package com.rawdatalmuslim.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.json.JSONArray
import java.util.Calendar

object PrayerScheduler {
    private const val PREFS = "rawdatalmuslim"
    private const val ADHAN_BASE = 4100
    private const val REMINDER_BASE = 5100

    fun scheduleAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString("prayers_json", "[]") ?: "[]"
        val muted = prefs.getBoolean("mute_mode", false)
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prayers = try { JSONArray(json) } catch (_: Exception) { JSONArray() }

        for (i in 0 until 5) {
            cancel(context, ADHAN_BASE + i)
            cancel(context, REMINDER_BASE + i)
            if (i >= prayers.length()) continue
            val prayer = prayers.optJSONObject(i) ?: continue
            val name = prayer.optString("name", "الصلاة")
            val hour12 = prayer.optInt("hour", 0)
            val minute = prayer.optInt("minute", 0)
            val period = prayer.optString("period", "AM")
            val audio = prayer.optString("audio", "اذان عبدالباسط.mp3")
            val hour24 = when {
                period.equals("PM", true) && hour12 < 12 -> hour12 + 12
                period.equals("AM", true) && hour12 == 12 -> 0
                else -> hour12
            }
            if (hour24 !in 0..23 || minute !in 0..59) continue
            val trigger = nextTime(hour24, minute)

            if (!muted) {
                schedule(context, alarms, ADHAN_BASE + i, trigger, name, audio, false)
                schedule(context, alarms, REMINDER_BASE + i, trigger - 10 * 60 * 1000L, name, audio, true)
            }
        }
    }

    private fun nextTime(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    private fun schedule(context: Context, alarms: AlarmManager, id: Int, at: Long, name: String, audio: String, reminder: Boolean) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra("prayer_name", name)
            putExtra("audio_file", audio)
            putExtra("is_reminder", reminder)
            putExtra("alarm_id", id)
        }
        val pending = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarms.canScheduleExactAlarms()) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }
    }

    private fun cancel(context: Context, id: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pending != null) (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
    }
}

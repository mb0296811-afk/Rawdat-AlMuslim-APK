package com.rawdatalmuslim.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == AlarmManagerAction.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            PrayerScheduler.scheduleAll(context)
        }
    }
}

private object AlarmManagerAction {
    const val SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED = "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
}

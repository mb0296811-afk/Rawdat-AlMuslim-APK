package com.rawdatalmuslim.app

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.os.IBinder
import android.widget.Toast

class AdhanPlaybackService : Service() {
    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayer = intent?.getStringExtra("prayer_name") ?: "الصلاة"
        val file = intent?.getStringExtra("audio_file") ?: "اذان عبدالباسط.mp3"
        AdhanNotifications.createChannel(this)
        startForeground(AdhanNotifications.PLAYBACK_ID, AdhanNotifications.playback(this, prayer))
        try {
            player?.release()
            val afd = assets.openFd("www/$file")
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setOnCompletionListener { stopSelf() }
                setOnErrorListener { _, _, _ -> stopSelf(); true }
                prepare()
                start()
            }
        } catch (e: Exception) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.release()
        player = null
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

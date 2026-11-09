package com.rawdatalmuslim.app

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdhanNotifications.createChannel(this)
        requestNotificationPermission()
        requestExactAlarmAccessIfNeeded()
        webView = WebView(this)
        setContentView(webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(AppBridge(), "AndroidPrayer")
        webView.loadUrl("file:///android_asset/www/index.html")
    }


    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 501)
        }
    }

    private fun requestExactAlarmAccessIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!manager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("تفعيل الأذان في موعده")
                    .setMessage("اسمح لروضة المسلم بالمنبهات والتذكيرات الدقيقة حتى يعمل الأذان في موعده حتى عند إغلاق التطبيق.")
                    .setPositiveButton("فتح الإعدادات") { _, _ ->
                        try {
                            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                        } catch (_: Exception) {
                            Toast.makeText(this, "افتح إعدادات التطبيق وفعّل المنبهات الدقيقة", Toast.LENGTH_LONG).show()
                        }
                    }
                    .setNegativeButton("لاحقاً", null)
                    .show()
            }
        }
    }

    inner class AppBridge {
        @JavascriptInterface
        fun openQuranPdf() {
            runOnUiThread {
                try {
                    startActivity(android.content.Intent(this@MainActivity, QuranPdfActivity::class.java))
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "تعذر فتح المصحف", Toast.LENGTH_LONG).show()
                }
            }
        }

        // The HTML sends the current manually selected prayer times and audio files to native Android scheduling.
        @JavascriptInterface
        fun syncPrayerData(prayersJson: String, ignored: Boolean, muted: Boolean) {
            getSharedPreferences("rawdatalmuslim", MODE_PRIVATE).edit()
                .putString("prayers_json", prayersJson)
                .putBoolean("mute_mode", muted)
                .apply()
            PrayerScheduler.scheduleAll(this@MainActivity)
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}

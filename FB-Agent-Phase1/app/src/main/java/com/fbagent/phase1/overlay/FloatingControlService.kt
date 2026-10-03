package com.fbagent.phase1.overlay

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.fbagent.phase1.preferences.PreferenceStore

class FloatingControlService : Service() {
    private var windowManager: WindowManager? = null
    private var control: TextView? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        val channelId = "fb_agent_control"
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(channelId, "FB-Agent Control", NotificationManager.IMPORTANCE_LOW)
            )
        }

        val notification = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, channelId)
                .setContentTitle("FB-Agent running")
                .setContentText("Facebook automation control is available")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
                .setContentTitle("FB-Agent running")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .build()
        }
        startForeground(9, notification)

        val prefs = PreferenceStore(this)
        control = TextView(this).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(225, 24, 119, 242))
            setPadding(22, 14, 22, 14)
            text = label(prefs.isEnabled())
            setOnClickListener {
                val enabled = !prefs.isEnabled()
                prefs.setEnabled(enabled)
                text = label(enabled)
            }
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val type = if (Build.VERSION.SDK_INT >= 26) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 180
        }

        try {
            windowManager?.addView(control, params)
        } catch (_: WindowManager.BadTokenException) {
            stopSelf()
        } catch (_: SecurityException) {
            stopSelf()
        }
    }

    private fun label(enabled: Boolean) = if (enabled) "FB\nON" else "FB\nOFF"

    override fun onDestroy() {
        try { control?.let { windowManager?.removeView(it) } } catch (_: Exception) { }
        control = null
        windowManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

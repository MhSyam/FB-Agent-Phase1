package com.fbagent.phase1.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.fbagent.phase1.databinding.ActivityMainBinding
import com.fbagent.phase1.overlay.FloatingControlService
import com.fbagent.phase1.preferences.PreferenceStore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferenceStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferenceStore(this)

        binding.enabled.isChecked = prefs.isEnabled()
        binding.dryRun.isChecked = prefs.isDryRun()
        refreshStatus()

        binding.enabled.setOnCheckedChangeListener { _, checked ->
            prefs.setEnabled(checked)
            refreshStatus()
        }
        binding.dryRun.setOnCheckedChangeListener { _, checked -> prefs.setDryRun(checked) }

        binding.access.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.overlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        }
        binding.startOverlay.setOnClickListener { startFloatingSafely() }
    }

    private fun startFloatingSafely() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        val intent = Intent(this, FloatingControlService::class.java)
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
        } catch (_: SecurityException) {
            // Permission/state changed between check and service start.
        }
    }

    private fun refreshStatus() {
        binding.status.text = if (prefs.isEnabled()) "Status: ACTIVE" else "Status: OFF"
    }
}

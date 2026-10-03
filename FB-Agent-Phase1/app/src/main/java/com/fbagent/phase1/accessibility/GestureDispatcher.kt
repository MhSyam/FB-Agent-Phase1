package com.fbagent.phase1.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import kotlin.math.sin
import kotlin.random.Random

/** Controlled gesture dispatcher. Variation is for usability, not for bypassing platform detection. */
class GestureDispatcher(private val service: AccessibilityService) {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    fun isRunning(): Boolean = running

    fun swipeUp(onFinished: (Boolean) -> Unit = {}) {
        if (running) return
        running = true

        val delayMs = Random.nextLong(300L, 751L)
        handler.postDelayed({
            try {
                val dm = service.resources.displayMetrics
                val x = dm.widthPixels * 0.50f
                val startY = dm.heightPixels * 0.78f
                val endY = dm.heightPixels * 0.27f
                val bend = dm.widthPixels * 0.035f

                val path = Path()
                path.moveTo(x, startY)
                // Smooth, modest curvature; not intended as anti-bot evasion.
                path.cubicTo(
                    x - bend, startY - (startY - endY) * 0.30f,
                    x + bend * 0.6f, startY - (startY - endY) * 0.72f,
                    x + bend * sin(1.0), endY
                )

                val duration = Random.nextLong(180L, 301L)
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0L, duration))
                    .build()

                service.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        running = false
                        onFinished(true)
                    }
                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        running = false
                        onFinished(false)
                    }
                }, handler)
            } catch (_: RuntimeException) {
                running = false
                onFinished(false)
            }
        }, delayMs)
    }

    fun cancelPending() {
        handler.removeCallbacksAndMessages(null)
        running = false
    }
}

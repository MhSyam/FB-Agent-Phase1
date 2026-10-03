package com.fbagent.phase1.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.fbagent.phase1.ai.DecisionEngine
import com.fbagent.phase1.data.AgentAction
import com.fbagent.phase1.data.DecisionLog
import com.fbagent.phase1.data.AgentDatabase
import com.fbagent.phase1.preferences.PreferenceStore
import com.fbagent.phase1.tracking.DwellTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.security.MessageDigest

class FbAccessibilityService : AccessibilityService() {
    companion object {
        private const val FB = "com.facebook.katana"
        private const val FB_LITE = "com.facebook.lite"
        private const val DECISION_COOLDOWN_MS = 1400L
    }

    private lateinit var prefs: PreferenceStore
    private lateinit var gestures: GestureDispatcher
    private lateinit var database: AgentDatabase
    private val tracker = DwellTracker()
    private val engine = DecisionEngine()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var lastFingerprint = ""
    private var lastActionFingerprint = ""
    private var lastDecisionAt = 0L
    private var foregroundFacebook = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PreferenceStore(this)
        gestures = GestureDispatcher(this)
        database = AgentDatabase.get(this)
        tracker.start()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::prefs.isInitialized) return
        val packageName = event.packageName?.toString() ?: return
        val isFacebook = packageName == FB || packageName == FB_LITE

        if (!isFacebook) {
            foregroundFacebook = false
            return
        }
        foregroundFacebook = true
        if (!prefs.isEnabled()) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            tracker.start()
            lastFingerprint = ""
            lastActionFingerprint = ""
        }

        // The root may legitimately be null during fast Facebook UI transitions.
        val root = rootInActiveWindow ?: return
        val text = try {
            NodeInspector.extract(root)
        } catch (_: RuntimeException) {
            return
        }
        if (text.isBlank()) return

        val fingerprint = fingerprint(text)
        if (fingerprint == lastFingerprint) return
        lastFingerprint = fingerprint

        val now = System.currentTimeMillis()
        if (now - lastDecisionAt < DECISION_COOLDOWN_MS) return
        lastDecisionAt = now

        scope.launch {
            val decision = engine.decide(text, tracker.seconds())
            logDecision(decision, fingerprint)

            if (prefs.isDryRun()) return@launch
            if (decision.action != AgentAction.SWIPE_UP || decision.confidence < 0.90f) return@launch
            if (fingerprint == lastActionFingerprint) return@launch
            if (!foregroundFacebook) return@launch

            lastActionFingerprint = fingerprint
            gestures.swipeUp { success ->
                if (!success) lastActionFingerprint = ""
            }
        }
    }

    private suspend fun logDecision(decision: com.fbagent.phase1.data.Decision, fingerprint: String) {
        try {
            database.logs().insert(
                DecisionLog(
                    timestamp = System.currentTimeMillis(),
                    action = decision.action.name,
                    confidence = decision.confidence,
                    reason = "${decision.reason}; content=$fingerprint"
                )
            )
        } catch (_: Exception) {
            // Logging must never bring down the accessibility service.
        }
    }

    private fun fingerprint(text: String): String {
        val normalized = text.lowercase().replace(Regex("\\s+"), " ").trim().take(6000)
        val bytes = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onInterrupt() {
        if (::gestures.isInitialized) gestures.cancelPending()
    }

    override fun onDestroy() {
        if (::gestures.isInitialized) gestures.cancelPending()
        scope.cancel()
        super.onDestroy()
    }
}

package com.fbagent.phase1.ai

import com.fbagent.phase1.data.AgentAction
import com.fbagent.phase1.data.Decision
import com.fbagent.phase1.detection.LocalRuleEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decision layer. Network/AI providers can be added behind this boundary later.
 * All potentially heavier decision work is kept off the main thread.
 */
class DecisionEngine(private val local: LocalRuleEngine = LocalRuleEngine()) {
    suspend fun decide(text: String, dwellSeconds: Long): Decision = withContext(Dispatchers.Default) {
        local.classify(text)?.let { return@withContext it }
        if (dwellSeconds in 0..1) {
            Decision(AgentAction.SWIPE_UP, 0.70f, "Very short dwell")
        } else {
            Decision(AgentAction.WAIT, 0.60f, "No local ad indicator")
        }
    }
}

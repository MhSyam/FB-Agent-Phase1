package com.fbagent.phase1.detection

import com.fbagent.phase1.data.*

class LocalRuleEngine {
 private val adTerms=listOf("sponsored","sponsored link","promoted","স্পন্সরড","বিজ্ঞাপন","sponsor")
 fun classify(text:String):Decision? { val t=text.lowercase(); val hit=adTerms.firstOrNull{t.contains(it)} ?: return null; return Decision(AgentAction.SWIPE_UP,0.99f,"Local ad indicator: $hit") }
}

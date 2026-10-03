package com.fbagent.phase1.data

data class ContentSnapshot(val text:String, val appPackage:String, val timestamp:Long=System.currentTimeMillis())
enum class AgentAction { WAIT, SWIPE_UP, FAST_FORWARD }
data class Decision(val action:AgentAction,val confidence:Float,val reason:String)

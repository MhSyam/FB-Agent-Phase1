package com.fbagent.phase1.preferences

import android.content.Context

class PreferenceStore(ctx:Context){ private val p=ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE)
 fun isEnabled()=p.getBoolean("enabled",false); fun setEnabled(v:Boolean){p.edit().putBoolean("enabled",v).apply()}
 fun isDryRun()=p.getBoolean("dry_run",true); fun setDryRun(v:Boolean){p.edit().putBoolean("dry_run",v).apply()}
 fun dwellSeconds()=p.getLong("dwell",10); fun setDwellSeconds(v:Long){p.edit().putLong("dwell",v.coerceIn(2,120)).apply()}
}

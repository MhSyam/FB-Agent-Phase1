package com.fbagent.phase1.tracking

class DwellTracker { private var started=0L; fun start(){started=System.currentTimeMillis()}; fun seconds():Long=if(started==0L)0 else (System.currentTimeMillis()-started)/1000 }

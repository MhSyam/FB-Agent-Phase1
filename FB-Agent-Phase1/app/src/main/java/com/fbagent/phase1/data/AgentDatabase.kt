package com.fbagent.phase1.data

import android.content.Context
import androidx.room.*

@Entity(tableName="decision_logs")
data class DecisionLog(@PrimaryKey(autoGenerate=true) val id:Long=0,val timestamp:Long,val action:String,val confidence:Float,val reason:String)
@Dao interface DecisionLogDao { @Insert suspend fun insert(v:DecisionLog); @Query("SELECT COUNT(*) FROM decision_logs") suspend fun count():Int; @Query("DELETE FROM decision_logs WHERE timestamp < :cutoff") suspend fun deleteOlderThan(cutoff:Long) }
@Database(entities=[DecisionLog::class],version=1,exportSchema=false)
abstract class AgentDatabase:RoomDatabase(){abstract fun logs():DecisionLogDao
 companion object { @Volatile private var INSTANCE:AgentDatabase?=null; fun get(c:Context)=INSTANCE?:synchronized(this){INSTANCE?:Room.databaseBuilder(c.applicationContext,AgentDatabase::class.java,"fb_agent.db").build().also{INSTANCE=it}}}}

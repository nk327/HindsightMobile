package com.connor.hindsightmobile.obj

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.connor.hindsightmobile.services.IngestScreenshotsService

class IngestAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("IngestAlarmReceiver", "Alarm received, starting IngestScreenshotsService")
        if (!IngestScreenshotsService.isRunning){
            val ingestIntent = Intent(context, IngestScreenshotsService::class.java)
            ContextCompat.startForegroundService(context, ingestIntent)
        }
    }
}

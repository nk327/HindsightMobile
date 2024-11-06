package com.connor.hindsightmobile.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.connor.hindsightmobile.obj.UserActivityState

class UserActivityTrackingService : AccessibilityService() {
    private val excludedPackages = listOf(
        "com-android-systemui",      // System UI
        "com-android-launcher",      // Default launcher
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        UserActivityState.userActive = true
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            try {
                event.packageName?.let {
                    val packageName = event.packageName.toString().replace(".", "-")
                    if (excludedPackages.contains(packageName)) {
                        return
                    }
                    UserActivityState.currentApplication = packageName
                    Log.d("UserActivityTrackingService", "onAccessibilityEvent: $packageName ${event.eventType} ${event.className}")
                }
            } catch(e: Error){
                Log.d("UserActivityTrackingService", "Error getting packageName", e)
            }
        }
    }

    override fun onCreate() {
        Log.d("UserActivityTrackingService", "onCreate")
        super.onCreate()
    }
    override fun onInterrupt() {
    }
}

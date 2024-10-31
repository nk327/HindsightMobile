package com.connor.hindsightmobile

import android.app.Application
import com.connor.hindsightmobile.obj.ObjectBoxStore
import com.connor.hindsightmobile.utils.NotificationHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Preferences.init(this)
        NotificationHelper.buildNotificationChannels(this)
        ObjectBoxStore.init(this)
    }
}
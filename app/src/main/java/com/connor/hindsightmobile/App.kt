package com.connor.hindsightmobile

import android.app.Application
import com.connor.hindsightmobile.di.AppModule
import com.connor.hindsightmobile.obj.ObjectBoxStore
import com.connor.hindsightmobile.utils.NotificationHelper
import com.connor.llamacpp.LlamaCpp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class App : Application() {
    val llamaCpp = LlamaCpp()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(AppModule().module)
        }
        // Preferences.init(this)
        NotificationHelper.buildNotificationChannels(this)
        ObjectBoxStore.init(this)
        llamaCpp.init()
    }
}
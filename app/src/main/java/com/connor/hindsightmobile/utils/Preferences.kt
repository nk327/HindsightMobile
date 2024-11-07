package com.connor.hindsightmobile.utils

import android.content.Context
import android.content.SharedPreferences

object Preferences {
    private const val PREF_FILE_NAME = "Hindsight"
    lateinit var prefs: SharedPreferences

    const val screenrecordingenabled = "ScreenRecordingEnabled"
    const val recordwhenactive = "RecordWhenActive"
    const val autoingestenabled = "AutoIngest"
    const val autoingesttime = "AutoIngestTime"
    const val audoingestinterval = "AutoIngestInterval"
    const val defaultllmname = "DefaultLLMName"
    const val defaultrecordapps = "DefaultRecordApps"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

        if (!prefs.contains(screenrecordingenabled)) {
            prefs.edit().putBoolean(screenrecordingenabled, false).apply()
        }

        if (!prefs.contains(recordwhenactive)) {
            prefs.edit().putBoolean(recordwhenactive, true).apply()
        }

        if (!prefs.contains(autoingestenabled)) {
            prefs.edit().putBoolean(autoingestenabled, true).apply()
        }

        if (!prefs.contains(autoingesttime)) {
            prefs.edit().putInt(autoingesttime, 2).apply()
        }

        if (!prefs.contains(defaultrecordapps)) {
            prefs.edit().putBoolean(defaultrecordapps, false).apply()
        }
    }
}
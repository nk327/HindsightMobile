package com.connor.hindsightmobile.obj

object UserActivityState {
    @Volatile var userActive: Boolean = false

    @Volatile var currentApplication: String? = "screenshot"
}

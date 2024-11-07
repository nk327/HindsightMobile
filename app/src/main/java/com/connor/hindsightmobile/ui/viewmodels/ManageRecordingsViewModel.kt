package com.connor.hindsightmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.connor.hindsightmobile.ui.elements.AppInfo

class ManageRecordingsViewModel: ViewModel() {
    private val _appList = mutableStateListOf(
        AppInfo(packageName = "com.example.app1", appName = "App 1"),
        AppInfo(packageName = "com.example.app2", appName = "App 2"),
        AppInfo(packageName = "com.example.app3", appName = "App 3")
    )
    val appList: List<AppInfo> = _appList

    // Handles app selection/unselection
    fun toggleAppSelection(app: AppInfo, isSelected: Boolean) {
        _appList.find { it.packageName == app.packageName }?.isSelected = isSelected
    }

    // Deletes data for a specific app
    fun deleteAppData(app: AppInfo) {
        _appList.remove(app)
    }

    // Deletes data for all selected apps
    fun deleteAllData() {
        _appList.clear()
    }
}
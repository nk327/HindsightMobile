package com.connor.hindsightmobile.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.connor.hindsightmobile.ui.elements.AppInfo
import androidx.compose.ui.Modifier
import com.connor.hindsightmobile.ui.elements.AppItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.connor.hindsightmobile.ui.viewmodels.ManageRecordingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.connor.hindsightmobile.ui.elements.DeleteConfirmationDialog


@Composable
fun ManageRecordingsScreen(
    navController: NavController,
) {
    val viewModel: ManageRecordingsViewModel = viewModel()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appToDelete by remember { mutableStateOf<AppInfo?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Manage App Recording",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.appList) { app ->
                AppItem(
                    app = app,
                    onAppSelected = { selectedApp ->
                        viewModel.toggleAppIsRecording(selectedApp)
                    },
                    onDeleteAppData = { selectedApp ->
                        appToDelete = selectedApp
                        showDeleteDialog = true
                    }
                )
            }
        }
        if (showDeleteDialog && appToDelete != null) {
            DeleteConfirmationDialog(
                app = appToDelete!!,
                onConfirm = {
                    viewModel.deleteAppData(appToDelete!!)
                    showDeleteDialog = false
                    appToDelete = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    appToDelete = null
                }
            )
        }
    }
}


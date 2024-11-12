package com.connor.hindsightmobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.connor.hindsightmobile.ui.elements.AppInfo
import androidx.compose.ui.Modifier
import com.connor.hindsightmobile.ui.elements.AppItem
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.connor.hindsightmobile.ui.viewmodels.ManageRecordingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.connor.hindsightmobile.ui.elements.DeleteConfirmationDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRecordingsScreen(
    navController: NavController,
) {
    val viewModel: ManageRecordingsViewModel = viewModel()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appToDelete by remember { mutableStateOf<AppInfo?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopAppBar(
            title = { Text(text = "Manage App Recording") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
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


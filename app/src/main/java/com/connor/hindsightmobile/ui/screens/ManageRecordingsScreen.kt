package com.connor.hindsightmobile.ui.screens

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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.connor.hindsightmobile.ui.viewmodels.ManageRecordingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ManageRecordingsScreen(
    viewModel: ManageRecordingsViewModel = viewModel()
) {
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
                    onAppSelected = { selectedApp, isSelected ->
                        viewModel.toggleAppSelection(selectedApp, isSelected)
                    },
                    onDeleteAppData = { selectedApp ->
                        viewModel.deleteAppData(selectedApp)
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppSelectionScreenPreview() {
    val viewModel = ManageRecordingsViewModel()
    ManageRecordingsScreen(viewModel = viewModel)
}

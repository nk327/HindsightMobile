package com.connor.hindsightmobile.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.connor.hindsightmobile.MainActivity
import com.connor.hindsightmobile.ui.elements.ToggleButton
import com.connor.hindsightmobile.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(navController: NavController,
                   settingsViewModel: SettingsViewModel = viewModel(),
){
    val context = LocalContext.current

    LaunchedEffect(key1 = settingsViewModel) {
        settingsViewModel.events.collect { event ->
            when (event) {
                SettingsViewModel.UIEvent.RequestScreenCapturePermission -> {
                    if (context is MainActivity) {
                        context.requestScreenCapturePermission()
                    }
                }
                SettingsViewModel.UIEvent.StopScreenRecording -> {
                    if (context is MainActivity) {
                        Log.d("MainScreen", "Stopping screen recording")
                        context.stopScreenRecording()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        val screenRecordingEnabled = settingsViewModel.screenRecordingEnabled.collectAsState()
        val autoIngestEnabled = settingsViewModel.autoIngestEnabled.collectAsState()
        val autoIngestTime = settingsViewModel.autoIngestTime.collectAsState()

        Button(
            onClick = {
                if (context is MainActivity) {
                    context.ingestScreenshots()
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(
                    16.dp
                )
        ) {
            Text("Ingest Screenshots")
        }
        ToggleButton(
            checked = screenRecordingEnabled.value,
            text = "Screen Recording",
            onToggleOn = settingsViewModel::toggleScreenRecording,
            onToggleOff = settingsViewModel::toggleScreenRecording,
            onClickSettings = { /* Open settings for keystroke tracking */ }
        )
        ToggleButton(
            checked = autoIngestEnabled.value,
            text = "Auto Ingest",
            onToggleOn = settingsViewModel::toggleAutoIngest,
            onToggleOff = settingsViewModel::toggleAutoIngest,
            onClickSettings = { /* Open settings for keystroke tracking */ }
        )
        TextField(
            value = autoIngestTime.value.toString(),
            onValueChange = { newValue ->
                val newIntValue = newValue.toIntOrNull()
                if (newIntValue != null) {
                    settingsViewModel.updateAutoIngestTime(newIntValue)
                }
            },
            label = { Text("Hour to Auto Ingest (military time)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {navController.navigate("chat")},
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(
                    16.dp
                )
        ) {
            Text("Chat")
        }
    }
}
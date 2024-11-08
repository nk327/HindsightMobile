package com.connor.hindsightmobile.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.connor.hindsightmobile.MainActivity
import com.connor.hindsightmobile.R
import com.connor.hindsightmobile.ui.elements.ToggleButton
import com.connor.hindsightmobile.ui.viewmodels.SettingsViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

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

    Surface(color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize())
    {
        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                val screenRecordingEnabled = settingsViewModel.screenRecordingEnabled.collectAsState()
                val isIngesting = settingsViewModel.isIngesting.collectAsState()
                val defaultRecordApps = settingsViewModel.defaultRecordApps.collectAsState()
                val autoIngestEnabled = settingsViewModel.autoIngestEnabled.collectAsState()
                val autoIngestTime = settingsViewModel.autoIngestTime.collectAsState()

                MarkdownText(
                    markdown = """
                    |### Ingest Screenshots
                    |* Run a manual ingestion of screenshots with options to add to the database, perform OCR, and embed the results.
                    
                """.trimMargin(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)

                    )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (context is MainActivity) {
                                context.ingestScreenshots()
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Ingest Screenshots")
                    }
                    if (settingsViewModel.isIngesting.collectAsState().value) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                MarkdownText(
                    markdown = """
                    |### Screen Recording
                    |* Start a screen recording background process. Stop it through the notification bar.
                    
                """.trimMargin(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)

                    )

                ToggleButton(
                    checked = settingsViewModel.screenRecordingEnabled.collectAsState().value,
                    text = "Screen Recording",
                    onToggleOn = settingsViewModel::toggleScreenRecording,
                    onToggleOff = settingsViewModel::toggleScreenRecording,
                    onClickSettings = { /* Open settings for keystroke tracking */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                MarkdownText(
                    markdown = """
                    ### Auto Ingest
                    * Automatically run ingestion at a specified hour.
                    
                """.trimIndent(),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,

                    )


                ToggleButton(
                    checked = settingsViewModel.autoIngestEnabled.collectAsState().value,
                    text = "Auto Ingest",
                    onToggleOn = settingsViewModel::toggleAutoIngest,
                    onToggleOff = settingsViewModel::toggleAutoIngest,
                    onClickSettings = { /* Open settings for keystroke tracking */ }
                )

                TextField(
                    value = settingsViewModel.autoIngestTime.collectAsState().value.toString(),
                    onValueChange = { newValue ->
                        val newIntValue = newValue.toIntOrNull()
                        if (newIntValue != null) {
                            settingsViewModel.updateAutoIngestTime(newIntValue)
                        }
                    },
                    label = { Text("Hour to Auto Ingest (military time)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                MarkdownText(
                    markdown = """
                    ## Record New Apps By Default
                    * Automatically start recording new apps when they are accessed for the first time.
                    
                """.trimIndent(),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,

                    )

                ToggleButton(
                    checked = settingsViewModel.defaultRecordApps.collectAsState().value,
                    text = "Record New Apps By Default",
                    onToggleOn = settingsViewModel::toggleDefaultRecordApps,
                    onToggleOff = settingsViewModel::toggleDefaultRecordApps,
                    onClickSettings = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                MarkdownText(
                    markdown = """
                    ### Manage Recordings
                    * Navigate to manage recordings, select apps to record, and delete all associated content for an app.
                    
                """.trimIndent(),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,

                    )


                Button(
                    onClick = { navController.navigate("manageRecordings") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Manage Recordings")
                }

                Spacer(modifier = Modifier.height(16.dp))

                MarkdownText(
                    markdown = """
                    ### Chat
                    * Go to the chat screen.
                    
                """.trimIndent(),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,

                    )

                Button(
                    onClick = { navController.navigate("chat") },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Chat")
                }
            }
        }
    }
}
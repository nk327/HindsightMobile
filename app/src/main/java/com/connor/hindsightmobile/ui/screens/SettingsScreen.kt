package com.connor.hindsightmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.connor.hindsightmobile.MainActivity

@Composable
fun SettingsScreen(navController: NavController){
    val context = LocalContext.current

    Row(
        modifier = Modifier.padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                if (context is MainActivity) {
                    context.ingestScreenshots()
                }
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(top = 16.dp)
                .padding(
                    16.dp
                )
        ) {
            Text("Ingest Screenshots")
        }
    }
}
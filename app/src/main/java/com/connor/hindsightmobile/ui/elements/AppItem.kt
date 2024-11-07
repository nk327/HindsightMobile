package com.connor.hindsightmobile.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


data class AppInfo(
    val packageName: String,
    val appName: String,
    var isRecording: Boolean = false,
    var numFrames: Int = 0,
)

@Composable
fun AppItem(
    app: AppInfo,
    onAppSelected: (AppInfo) -> Unit,
    onDeleteAppData: (AppInfo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.appName, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${app.numFrames} frames", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Checkbox(
            checked = app.isRecording,
            onCheckedChange = {
                onAppSelected(app)
            }
        )

        IconButton(onClick = { onDeleteAppData(app) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete ${app.appName} data"
            )
        }
    }
}

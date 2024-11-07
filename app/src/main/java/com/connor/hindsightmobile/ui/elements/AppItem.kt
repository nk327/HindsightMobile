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
    var isSelected: Boolean = false
)

@Composable
fun AppItem(
    app: AppInfo,
    onAppSelected: (AppInfo, Boolean) -> Unit,
    onDeleteAppData: (AppInfo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.appName, style = MaterialTheme.typography.bodyMedium)
        }

        Checkbox(
            checked = app.isSelected,
            onCheckedChange = { isChecked ->
                onAppSelected(app, isChecked)
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

package com.connor.hindsightmobile.ui.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkdownSection(title: String, description: String) {
    Surface(color = MaterialTheme.colorScheme.background) {
        MarkdownText(
            markdown = """
            ### **$title**
            * $description
            
            """.trimIndent(),
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
    }
}
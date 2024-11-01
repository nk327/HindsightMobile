package com.connor.hindsightmobile.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.connor.hindsightmobile.R
import com.connor.hindsightmobile.ui.theme.HindsightMobileTheme
import com.connor.hindsightmobile.ui.viewmodels.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    HindsightMobileTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(text = "Chat", style = MaterialTheme.typography.headlineSmall) },
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxWidth()) {
                    val chatViewModel: ChatViewModel = koinViewModel()
                    Column {
                        QALayout(chatViewModel)
                        Spacer(modifier = Modifier.height(8.dp))
                        QueryInput(chatViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun QALayout(chatViewModel: ChatViewModel = viewModel()) {
    val question by remember { chatViewModel.questionState }
    val response by remember { chatViewModel.responseState }
    val isGeneratingResponse by remember { chatViewModel.isGeneratingResponseState }
    val retrievedContextList by remember { chatViewModel.retrievedContextListState }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (question.trim().isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(75.dp),
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.LightGray
                )
                Text(
                    text = "Enter a query to see answers",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        } else {
            LazyColumn {
                item {
                    Text(text = question, style = MaterialTheme.typography.headlineLarge)
                    if (isGeneratingResponse) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    if (!isGeneratingResponse) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier =
                            Modifier.background(Color.White, RoundedCornerShape(16.dp))
                                .padding(24.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = response,
                                style =
                                TextStyle(
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        val sendIntent: Intent =
                                            Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, response)
                                                type = "text/plain"
                                            }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share the response",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Context", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                if (!isGeneratingResponse) {
                    items(retrievedContextList) { retrievedContext ->
                        Column(
                            modifier =
                            Modifier.padding(8.dp)
                                .background(Color.Cyan, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "\"${retrievedContext.context}\"",
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            )
                            Text(
                                text = retrievedContext.frameId.toString(),
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueryInput(chatViewModel: ChatViewModel) {
    var questionText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            modifier = Modifier.fillMaxWidth().weight(1f),
            value = questionText,
            onValueChange = { questionText = it },
            shape = RoundedCornerShape(16.dp),
            colors =
            TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                disabledTextColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = { Text(text = "Ask documents...") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            modifier = Modifier.background(Color.Blue, CircleShape),
            onClick = {
                keyboardController?.hide()
                if (questionText.trim().isEmpty()) {
                    Toast.makeText(context, "Enter a query to execute", Toast.LENGTH_LONG).show()
                    return@IconButton
                }

                chatViewModel.questionState.value = questionText
                questionText = ""
                chatViewModel.isGeneratingResponseState.value = true
                chatViewModel.qaUseCase.getAnswer(
                    chatViewModel.questionState.value,
                    context.getString(R.string.prompt_1)
                ) {
                    chatViewModel.isGeneratingResponseState.value = false
                    chatViewModel.responseState.value = it.response
                    chatViewModel.retrievedContextListState.value = it.contextList
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Send query",
                tint = Color.White
            )
        }
    }
}
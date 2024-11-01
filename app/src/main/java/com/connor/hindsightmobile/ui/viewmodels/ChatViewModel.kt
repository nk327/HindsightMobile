package com.connor.hindsightmobile.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.connor.hindsightmobile.obj.QAAgent
import com.connor.hindsightmobile.obj.RetrievedContext
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatViewModel(val qaUseCase: QAAgent) : ViewModel() {

    val questionState = mutableStateOf("")
    val responseState = mutableStateOf("")
    val isGeneratingResponseState = mutableStateOf(false)
    val retrievedContextListState = mutableStateOf(emptyList<RetrievedContext>())
}
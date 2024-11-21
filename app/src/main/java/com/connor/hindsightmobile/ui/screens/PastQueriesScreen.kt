package com.connor.hindsightmobile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.connor.hindsightmobile.models.SelectModelDialog
import com.connor.hindsightmobile.ui.elements.ConversationBar
import com.connor.hindsightmobile.ui.elements.Messages
import com.connor.hindsightmobile.ui.theme.HindsightMobileTheme
import com.connor.hindsightmobile.ui.viewmodels.ConversationViewModel
import com.connor.hindsightmobile.ui.elements.UserInput
import com.connor.hindsightmobile.ui.elements.UserInputStatus
import com.connor.hindsightmobile.ui.viewmodels.Message
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.connor.hindsightmobile.ui.viewmodels.PastQueriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastQueriesScreen(navController: NavController, viewModel: PastQueriesViewModel = viewModel()) {

    val messages = viewModel.getQueryList()

    HindsightMobileTheme {

        val scrollState = rememberLazyListState()
        val topBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
        val scope = rememberCoroutineScope()

        val colorScheme = MaterialTheme.colorScheme

        Scaffold(
            topBar = {
                ConversationBar(
                    modelInfo = null,
                    onSettingsIconPressed = {
                    },
                    scrollBehavior = scrollBehavior,
                    onSelectModelPressed = {
                    },
                    onUnloadModelPressed = {
                    }
                )
            },
            // Exclude ime and navigation bar padding so this can be added by the UserInput composable
            contentWindowInsets = ScaffoldDefaults
                .contentWindowInsets
                .exclude(WindowInsets.navigationBars)
                .exclude(WindowInsets.ime),
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Messages(
                    messages = messages,
                    navigateToProfile = { },
                    showAssistantPrompt = { message ->
                        navController.navigateToAssistantPrompt(message)
                    },
                    modifier = Modifier.weight(1f),
                    scrollState = scrollState
                )
            }
        }
    }
}

package com.connor.hindsightmobile.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.connor.hindsightmobile.ui.screens.ConversationScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    NavHost(navController = navController, startDestination = "chat") {
        composable("mainSettings") {
            SettingsScreen(navController)
        }
        composable("chat") {
            ConversationScreen(navController)
        }
        composable("manageRecordings"){
            ManageRecordingsScreen(navController)
        }
    }
}
package com.connor.hindsightmobile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.connor.hindsightmobile.services.IngestScreenshotsService
import com.connor.hindsightmobile.ui.screens.AppNavigation
import com.connor.hindsightmobile.ui.theme.HindsightMobileTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HindsightMobileTheme {
                AppNavigation()
            }
        }
    }

    fun ingestScreenshots(){
        Log.d("MainActivity", "Ingesting screenshots")
        val uploadIntent = Intent(this@MainActivity, IngestScreenshotsService::class.java)
        ContextCompat.startForegroundService(this@MainActivity, uploadIntent)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HindsightMobileTheme {
        AppNavigation()
    }
}
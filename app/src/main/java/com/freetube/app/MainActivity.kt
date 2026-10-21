package com.freetube.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.freetube.app.ui.PlayerStateManager
import com.freetube.app.ui.screens.YouTubeScreen
import com.freetube.app.ui.theme.FreeTubeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FreeTubeTheme {
                val systemUiController = rememberSystemUiController()
                val isFullscreen by PlayerStateManager.isFullscreen.collectAsState()

                SideEffect {
                    if (isFullscreen) {
                        systemUiController.isSystemBarsVisible = false
                    } else {
                        systemUiController.isSystemBarsVisible = true
                        systemUiController.setStatusBarColor(
                            color = Color(0xFF0F0F0F),
                            darkIcons = false
                        )
                        systemUiController.setNavigationBarColor(
                            color = Color(0xFF0F0F0F),
                            darkIcons = false
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0F0F)
                ) {
                    YouTubeScreen()
                }
            }
        }
    }
}

package com.freetube.app

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.freetube.app.ui.PlayerStateManager
import com.freetube.app.navigation.AppNavigation
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
                val darkTheme = isSystemInDarkTheme()
                // Read background from MaterialTheme so it tracks dynamic color
                val bgColor = MaterialTheme.colorScheme.background

                SideEffect {
                    if (isFullscreen) {
                        systemUiController.isSystemBarsVisible = false
                    } else {
                        systemUiController.isSystemBarsVisible = true
                        // Status & nav bar match app background; icons contrast automatically
                        systemUiController.setStatusBarColor(
                            color     = bgColor,
                            darkIcons = !darkTheme
                        )
                        systemUiController.setNavigationBarColor(
                            color     = bgColor,
                            darkIcons = !darkTheme
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (!isInPictureInPictureMode) {
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(Rational(16, 9))
                        .build()
                    enterPictureInPictureMode(params)
                }
            } catch (_: Exception) {}
        }
    }
}

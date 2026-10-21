package com.freetube.app

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.freetube.app.navigation.FreeTubeNavHost
import com.freetube.app.navigation.Screen
import com.freetube.app.ui.components.FreeTubeBottomNav
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.freetube.app.ui.PlayerStateManager
import com.freetube.app.ui.theme.FreeTubeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var shouldEnterPipOnLeave = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FreeTubeTheme {
                val systemUiController = rememberSystemUiController()
                val isFullscreen by PlayerStateManager.isFullscreen.collectAsState()
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val showBottomBar = !isFullscreen &&
                    currentRoute != Screen.Splash.route &&
                    currentRoute != Screen.Onboarding.route

                SideEffect {
                    shouldEnterPipOnLeave = currentRoute == Screen.Home.route && !isFullscreen
                }

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
                    Scaffold(
                        containerColor = Color(0xFF0F0F0F),
                        bottomBar = {
                            if (showBottomBar) {
                                FreeTubeBottomNav(navController)
                            }
                        }
                    ) { paddingValues ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            FreeTubeNavHost(navController)
                        }
                    }
                }

                override fun onUserLeaveHint() {
                    super.onUserLeaveHint()
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        shouldEnterPipOnLeave &&
                        !isInPictureInPictureMode
                    ) {
                        val params = PictureInPictureParams.Builder()
                            .setAspectRatio(Rational(16, 9))
                            .build()
                        enterPictureInPictureMode(params)
                    }
                }
            }
        }
    }
}

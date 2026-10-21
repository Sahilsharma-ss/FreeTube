package com.freetube.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.freetube.app.navigation.FreeTubeNavHost
import com.freetube.app.navigation.Screen
import com.freetube.app.ui.components.FreeTubeBottomNav
import com.freetube.app.ui.theme.Black
import com.freetube.app.ui.theme.BlackSurface
import com.freetube.app.ui.theme.FreeTubeTheme
import dagger.hilt.android.AndroidEntryPoint

// Routes that should NOT show the bottom nav bar
private val noNavBarRoutes = setOf(
    Screen.Splash.route,
    Screen.Onboarding.route
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FreeTubeTheme {
                val navController      = rememberNavController()
                val systemUiController = rememberSystemUiController()
                val backStackEntry     by navController.currentBackStackEntryAsState()
                val currentRoute       = backStackEntry?.destination?.route
                val showBottomNav      = currentRoute !in noNavBarRoutes

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color     = Color.Transparent,
                        darkIcons = false
                    )
                    systemUiController.setNavigationBarColor(
                        color     = if (showBottomNav) BlackSurface else Black,
                        darkIcons = false
                    )
                }

                Scaffold(
                    containerColor = Black,
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomNav,
                            enter   = slideInVertically { it } + fadeIn(),
                            exit    = slideOutVertically { it } + fadeOut()
                        ) {
                            FreeTubeBottomNav(navController)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else androidx.compose.ui.unit.Dp(0f))
                    ) {
                        FreeTubeNavHost(navController = navController)
                    }
                }
            }
        }
    }
}

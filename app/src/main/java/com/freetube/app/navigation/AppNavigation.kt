package com.freetube.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.freetube.app.ui.screens.OnboardingScreen
import com.freetube.app.ui.screens.SplashScreen
import com.freetube.app.ui.screens.YouTubeScreen

/**
 * Splash -> YouTube. No native tab bar: YouTube's own mobile UI already has
 * Home / Shorts / Subscriptions / Library and search, so a second bar was just clutter.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController      = navController,
        startDestination   = Screen.Splash.route,
        modifier           = Modifier.fillMaxSize(),
        enterTransition    = { fadeIn(animationSpec = tween(200)) },
        exitTransition     = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition  = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable(Screen.Splash.route)     { SplashScreen(navController) }
        composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
        composable(Screen.Home.route)       { YouTubeScreen() }
    }
}

package com.freetube.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.freetube.app.ui.screens.*

@Composable
fun FreeTubeNavHost(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route)      { SplashScreen(navController) }
        composable(Screen.Onboarding.route)  { OnboardingScreen(navController) }
        composable(Screen.Home.route)        { HomeScreen(navController) }
        composable(Screen.Search.route)      { SearchScreen(navController) }
        composable(Screen.Library.route)     { LibraryScreen(navController) }
        composable(Screen.Settings.route)    { SettingsScreen(navController) }
    }
}

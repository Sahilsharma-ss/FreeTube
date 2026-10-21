package com.freetube.app.navigation

sealed class Screen(val route: String) {
    object Splash      : Screen("splash")
    object Onboarding  : Screen("onboarding")
    object Home        : Screen("home")
    object Search      : Screen("search")
    object Library     : Screen("library")
    object Settings    : Screen("settings")
}

package com.freetube.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.freetube.app.ui.PlayerStateManager
import com.freetube.app.ui.screens.LibraryScreen
import com.freetube.app.ui.screens.OnboardingScreen
import com.freetube.app.ui.screens.SearchScreen
import com.freetube.app.ui.screens.SettingsScreen
import com.freetube.app.ui.screens.SplashScreen
import com.freetube.app.ui.screens.YouTubeScreen
import com.freetube.app.ui.theme.BlackSurface
import com.freetube.app.ui.theme.FTRed
import com.freetube.app.ui.theme.FTRedGlow
import com.freetube.app.ui.theme.WhiteMuted

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

private val bottomNavItems = listOf(
    BottomNavItem("Home",     Icons.Filled.Home,        Screen.Home),
    BottomNavItem("Search",   Icons.Filled.Search,      Screen.Search),
    BottomNavItem("Library",  Icons.Filled.VideoLibrary, Screen.Library),
    BottomNavItem("Settings", Icons.Filled.Settings,    Screen.Settings),
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isFullscreen by PlayerStateManager.isFullscreen.collectAsState()

    val tabRoutes = bottomNavItems.map { it.screen.route }
    val showBottomBar = currentRoute in tabRoutes && !isFullscreen

    Scaffold(
        containerColor = BlackSurface,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(tween(200)),
                exit  = slideOutVertically { it } + fadeOut(tween(200))
            ) {
                NavigationBar(
                    containerColor = BlackSurface,
                    tonalElevation = 0.dp,
                    // navigationBarsPadding ensures the bar sits ABOVE the system nav bar
                    modifier       = Modifier.navigationBarsPadding()
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy
                            ?.any { it.route == item.screen.route } == true

                        val scale by animateFloatAsState(
                            targetValue   = if (selected) 1.15f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label         = "navScale_${item.label}"
                        )

                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                if (item.screen == Screen.Home && currentRoute == Screen.Home.route) {
                                    // Re-tapping Home reloads the YouTube homepage
                                    PlayerStateManager.loadUrl("https://m.youtube.com")
                                } else {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            },
                            icon = {
                                Box(
                                    modifier         = Modifier.size(28.dp).scale(scale),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector        = item.icon,
                                        contentDescription = item.label,
                                        tint               = if (selected) FTRed else WhiteMuted
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text  = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) FTRed else WhiteMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = FTRed,
                                selectedTextColor   = FTRed,
                                unselectedIconColor = WhiteMuted,
                                unselectedTextColor = WhiteMuted,
                                indicatorColor      = FTRedGlow
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController       = navController,
            startDestination    = Screen.Splash.route,
            modifier            = Modifier.padding(paddingValues),
            enterTransition     = { fadeIn(animationSpec = tween(200)) },
            exitTransition      = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition  = { fadeIn(animationSpec = tween(200)) },
            popExitTransition   = { fadeOut(animationSpec = tween(200)) }
        ) {
            composable(Screen.Splash.route)     { SplashScreen(navController) }
            composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
            composable(Screen.Home.route)       { YouTubeScreen() }
            composable(Screen.Search.route)     { SearchScreen(navController) }
            composable(Screen.Library.route)    { LibraryScreen(navController) }
            composable(Screen.Settings.route)   { SettingsScreen(navController) }
        }
    }
}

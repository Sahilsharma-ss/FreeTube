package com.freetube.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freetube.app.navigation.Screen
import com.freetube.app.ui.theme.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home",     Icons.Filled.Home,        Screen.Home.route),
    BottomNavItem("Search",   Icons.Filled.Search,      Screen.Search.route),
    BottomNavItem("Library",  Icons.Filled.VideoLibrary, Screen.Library.route)
)

@Composable
fun FreeTubeBottomNav(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current   = backStack?.destination?.route

    NavigationBar(
        containerColor = BlackSurface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = current == item.route

            // Pulse animation for selected item
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "navScale"
            )

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (item.route == Screen.Home.route && current == Screen.Home.route) {
                        com.freetube.app.ui.PlayerStateManager.loadUrl("https://m.youtube.com")
                    } else if (current != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon = {
                    Box(
                        modifier        = Modifier.scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FTRedGlow)
                            )
                        }
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
                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    }
}

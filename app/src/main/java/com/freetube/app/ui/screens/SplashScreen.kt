package com.freetube.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.freetube.app.navigation.Screen
import com.freetube.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Logo scale animation
    val logoScale  = remember { Animatable(0.5f) }
    val logoAlpha  = remember { Animatable(0f) }
    val textAlpha  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate logo in
        logoAlpha.animateTo(1f, animationSpec = tween(400))
        logoScale.animateTo(
            targetValue   = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
        // Fade in text
        textAlpha.animateTo(1f, animationSpec = tween(400))
        // Hold for polish
        delay(1200)
        // Navigate to onboarding
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // Pulse ring behind logo
    val ringAnim = rememberInfiniteTransition(label = "ring")
    val ringScale by ringAnim.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.3f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )
    val ringAlpha by ringAnim.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1a0000), Black),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(contentAlignment = Alignment.Center) {
                // Pulsing ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(ringScale)
                        .alpha(ringAlpha)
                        .clip(RoundedCornerShape(30.dp))
                        .background(FTRed.copy(alpha = 0.2f))
                )

                // Main logo
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(listOf(FTRed, FTRedDim))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint     = White,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // App name
            Text(
                "FreeTube",
                fontSize   = 36.sp,
                fontWeight = FontWeight.Bold,
                color      = White,
                modifier   = Modifier.alpha(textAlpha.value)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Your videos. Your experience.",
                style    = MaterialTheme.typography.bodyMedium,
                color    = WhiteMuted,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }

        // Bottom loading dots
        Row(
            modifier             = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(textAlpha.value),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                val dotAnim = rememberInfiniteTransition(label = "dot$index")
                val dotAlpha by dotAnim.animateFloat(
                    initialValue  = 0.2f,
                    targetValue   = 1f,
                    animationSpec = infiniteRepeatable(
                        animation   = tween(600, delayMillis = index * 200),
                        repeatMode  = RepeatMode.Reverse
                    ),
                    label = "dotAlpha$index"
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .alpha(dotAlpha)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(FTRed)
                )
            }
        }
    }
}

package com.freetube.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.freetube.app.navigation.Screen
import com.freetube.app.ui.theme.*
import com.freetube.app.util.BraveHelper

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var braveInstalled by remember {
        mutableStateOf(BraveHelper.isBraveInstalled(context))
    }

    // Auto-detect when returning to the app from Play Store or background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                braveInstalled = BraveHelper.isBraveInstalled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val navigateToHome = {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    // Continuous pulse animation for the Brave logo ring
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.12f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Floating particle positions
    val particle1 by pulseAnim.animateFloat(
        initialValue   = 0f,
        targetValue    = 12f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )
    val particle2 by pulseAnim.animateFloat(
        initialValue   = 0f,
        targetValue    = -10f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors  = listOf(Color(0xFF1a0000), Black),
                    radius  = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Floating particles (decorative)
        Box(
            modifier = Modifier
                .size(8.dp)
                .offset(x = (-120).dp, y = particle1.dp - 180.dp)
                .clip(CircleShape)
                .background(FTRed.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .offset(x = 140.dp, y = particle2.dp - 220.dp)
                .clip(CircleShape)
                .background(FTRed.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .offset(x = 80.dp, y = particle1.dp + 200.dp)
                .clip(CircleShape)
                .background(FTRed.copy(alpha = 0.2f))
        )

        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(0.dp)
        ) {

            // ── FreeTube Logo ────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(FTRed, FTRedDim)
                        )
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

            Spacer(Modifier.height(20.dp))

            Text(
                "FreeTube",
                fontSize   = 34.sp,
                fontWeight = FontWeight.Bold,
                color      = White
            )
            Text(
                "Your videos. Your experience.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = WhiteMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // ── Status card ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = if (braveInstalled) Color(0xFF001a00) else BlackCard
                ),
                shape    = RoundedCornerShape(20.dp),
                border   = BorderStroke(
                    1.dp,
                    if (braveInstalled) Color(0xFF00C853) else FTRed
                )
            ) {
                Column(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment   = Alignment.CenterHorizontally
                ) {
                    // Icon with pulse if not installed
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(if (braveInstalled) 1f else pulseScale)
                            .clip(CircleShape)
                            .background(
                                if (braveInstalled) Color(0xFF00C853).copy(alpha = 0.15f)
                                else FTRedGlow
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = if (braveInstalled) Icons.Filled.CheckCircle
                                                  else              Icons.Filled.Shield,
                            contentDescription = null,
                            tint               = if (braveInstalled) Color(0xFF00C853) else FTRed,
                            modifier           = Modifier.size(34.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text       = if (braveInstalled) "Brave is installed ✓"
                                      else              "Brave Browser Recommended",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (braveInstalled) Color(0xFF00C853) else White,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text      = if (braveInstalled)
                            "Ad-free YouTube is ready! FreeTube will open all videos in Brave to block ads automatically."
                        else
                            "FreeTube opens YouTube in Brave Browser to block all ads automatically. You can install it now or continue anyway.",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = WhiteDim,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── CTA buttons ───────────────────────────────────
                    if (braveInstalled) {
                        Button(
                            onClick  = navigateToHome,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape    = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.RocketLaunch, contentDescription = null, tint = White)
                            Spacer(Modifier.width(10.dp))
                            Text("Launch FreeTube", fontWeight = FontWeight.Bold, color = White, fontSize = 16.sp)
                        }
                    } else {
                        // Install Brave button
                        Button(
                            onClick  = { BraveHelper.openBravePlayStore(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = FTRed),
                            shape    = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = White)
                            Spacer(Modifier.width(10.dp))
                            Text("Install Brave — It's Free", fontWeight = FontWeight.Bold, color = White, fontSize = 16.sp)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Already installed / Continue to FreeTube
                        OutlinedButton(
                            onClick  = {
                                // Recheck Brave, and enter the app!
                                braveInstalled = BraveHelper.isBraveInstalled(context)
                                navigateToHome()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape  = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DividerColor)
                        ) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = White)
                            Spacer(Modifier.width(8.dp))
                            Text("I already have Brave / Continue", color = White, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Feature pills ────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                FeaturePill("🚫 Zero ads")
                FeaturePill("🔒 Private")
                FeaturePill("⚡ Fast")
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "100% free · No account needed",
                style = MaterialTheme.typography.labelSmall,
                color = WhiteMuted
            )
        }
    }
}

@Composable
private fun FeaturePill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(ChipBackground)
            .border(1.dp, DividerColor, RoundedCornerShape(50.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = WhiteDim)
    }
}

package com.freetube.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.freetube.app.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top bar ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(BlackSurface, Black)))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                "Settings",
                style      = MaterialTheme.typography.titleLarge,
                color      = White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Appearance ───────────────────────────────────────────────
        SettingsSection("Appearance") {
            SettingsInfoRow(
                icon    = Icons.Filled.DarkMode,
                title   = "Dark Mode",
                value   = "AMOLED Black",
                iconTint = FTRed
            )
            SettingsDivider()
            SettingsInfoRow(
                icon    = Icons.Filled.Palette,
                title   = "Accent Color",
                value   = "FreeTube Red",
                iconTint = FTRed
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Browser ──────────────────────────────────────────────────
        SettingsSection("Browser") {
            SettingsInfoRow(
                icon     = Icons.Filled.OpenInBrowser,
                title    = "Default Browser",
                value    = "Brave",
                iconTint = FTRed
            )
            SettingsDivider()
            SettingsClickRow(
                icon     = Icons.Filled.Shield,
                title    = "Open Brave Settings",
                iconTint = WhiteMuted,
                onClick  = {
                    runCatching {
                        val intent = context.packageManager
                            .getLaunchIntentForPackage("com.brave.browser")
                        if (intent != null) context.startActivity(intent)
                    }
                }
            )
            SettingsDivider()
            SettingsClickRow(
                icon     = Icons.Filled.Download,
                title    = "Download Brave Browser",
                iconTint = WhiteMuted,
                onClick  = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW,
                               Uri.parse("https://play.google.com/store/apps/details?id=com.brave.browser"))
                    )
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Privacy ──────────────────────────────────────────────────
        SettingsSection("Privacy") {
            SettingsInfoRow(
                icon     = Icons.Filled.HistoryToggleOff,
                title    = "History",
                value    = "Saved locally",
                iconTint = WhiteMuted
            )
            SettingsDivider()
            SettingsInfoRow(
                icon     = Icons.Filled.Lock,
                title    = "Data Storage",
                value    = "On-device only",
                iconTint = WhiteMuted
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── About ────────────────────────────────────────────────────
        SettingsSection("About") {
            SettingsInfoRow(
                icon     = Icons.Filled.Info,
                title    = "Version",
                value    = "1.0.0",
                iconTint = WhiteMuted
            )
            SettingsDivider()
            SettingsClickRow(
                icon     = Icons.Filled.Code,
                title    = "Built with Kotlin + Jetpack Compose",
                iconTint = WhiteMuted,
                onClick  = {}
            )
        }

        Spacer(Modifier.height(80.dp))

        // ── Footer ───────────────────────────────────────────────────
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("▶  FreeTube", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FTRed)
            Text("Your videos. Your experience.", style = MaterialTheme.typography.bodySmall, color = WhiteMuted)
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Settings section card ─────────────────────────────────────────────────────
@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text     = title,
            style    = MaterialTheme.typography.labelLarge,
            color    = FTRed,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = BlackCard),
            shape  = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = White, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = WhiteMuted)
    }
}

@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = White, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = WhiteMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 52.dp),
        color     = DividerColor,
        thickness = 0.5.dp
    )
}

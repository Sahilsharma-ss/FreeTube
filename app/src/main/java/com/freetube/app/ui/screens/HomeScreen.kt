package com.freetube.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freetube.app.ui.theme.*

// Quick-launch categories shown as horizontal chips
private val categories = listOf(
    "All", "Music", "Gaming", "News", "Live", "Tech", "Sports", "Podcasts", "Cooking"
)

// Curated quick-launch links (open in Brave)
private data class QuickLink(
    val label: String,
    val url: String,
    val emoji: String
)

private val quickLinks = listOf(
    QuickLink("YouTube",     "https://www.youtube.com",             "▶"),
    QuickLink("Trending",    "https://www.youtube.com/feed/trending","🔥"),
    QuickLink("Subscriptions","https://www.youtube.com/feed/subscriptions","🔔"),
    QuickLink("History",     "https://www.youtube.com/feed/history", "🕐"),
    QuickLink("Shorts",      "https://www.youtube.com/shorts",       "⚡"),
    QuickLink("Library",     "https://www.youtube.com/feed/library", "📚")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context         = LocalContext.current
    val focusManager    = LocalFocusManager.current
    val searchQuery     by viewModel.searchQuery.collectAsState()
    val showBraveDialog by viewModel.showBraveDialog.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    // ── Brave not installed dialog ────────────────────────────────────────
    if (showBraveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBraveDialog() },
            containerColor   = com.freetube.app.ui.theme.BlackCard,
            icon = {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint     = com.freetube.app.ui.theme.FTRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Brave Browser Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = com.freetube.app.ui.theme.White
                )
            },
            text = {
                Text(
                    "FreeTube uses Brave Browser to open YouTube ad-free. Install Brave — it's free and takes 30 seconds.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.freetube.app.ui.theme.WhiteDim
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.openBraveOnPlayStore(context) },
                    colors  = ButtonDefaults.buttonColors(containerColor = com.freetube.app.ui.theme.FTRed),
                    shape   = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, tint = com.freetube.app.ui.theme.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Install Brave", color = com.freetube.app.ui.theme.White)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.openPendingInDefaultBrowser(context) }) {
                        Text("Open Anyway", color = com.freetube.app.ui.theme.WhiteDim)
                    }
                    TextButton(onClick = { viewModel.dismissBraveDialog() }) {
                        Text("Cancel", color = com.freetube.app.ui.theme.WhiteMuted)
                    }
                }
            }
        )
    }

    Scaffold(
        containerColor = Black,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BlackSurface, Black)
                        )
                    )
            ) {
                // ── App bar ──────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(FTRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint               = White,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "FreeTube",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color      = White
                        )
                    }

                    // Notification icon
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.NotificationsNone,
                            contentDescription = "Notifications",
                            tint               = WhiteDim
                        )
                    }
                }

                // ── Search bar ───────────────────────────────────────
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp),
                    placeholder   = { Text("Search YouTube…", color = WhiteMuted) },
                    leadingIcon   = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = WhiteMuted)
                    },
                    trailingIcon  = {
                        AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.searchOnYouTube(context, searchQuery)
                                focusManager.clearFocus()
                            }) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "Go", tint = FTRed)
                            }
                        }
                    },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.searchOnYouTube(context, searchQuery)
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    shape  = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = BlackCard,
                        unfocusedContainerColor = BlackCard,
                        focusedBorderColor      = FTRed,
                        unfocusedBorderColor    = DividerColor,
                        cursorColor             = FTRed,
                        focusedTextColor        = White,
                        unfocusedTextColor      = White
                    )
                )

                // ── Category chips ──────────────────────────────────
                LazyRow(
                    contentPadding    = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick  = { selectedCategory = cat },
                            label    = { Text(cat) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor   = FTRed,
                                selectedLabelColor       = White,
                                containerColor           = ChipBackground,
                                labelColor               = WhiteDim
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled          = true,
                                selected         = isSelected,
                                borderColor      = DividerColor,
                                selectedBorderColor = FTRed
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->

        LazyColumn(
            contentPadding = PaddingValues(
                top    = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 80.dp,
                start  = 16.dp,
                end    = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Quick Launch Section ─────────────────────────────────
            item {
                Text(
                    "Quick Launch",
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(quickLinks) { link ->
                        QuickLaunchChip(
                            emoji = link.emoji,
                            label = link.label,
                            onClick = {
                                viewModel.openInBrave(
                                    context   = context,
                                    url       = link.url,
                                    title     = link.label,
                                    thumbnail = "",
                                    channel   = "YouTube"
                                )
                            }
                        )
                    }
                }
            }

            // ── Trending Section ─────────────────────────────────────
            item {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint     = FTRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Trending", style = MaterialTheme.typography.titleMedium, color = White)
                    }
                    TextButton(onClick = {
                        viewModel.openInBrave(context, "https://www.youtube.com/feed/trending", "Trending")
                    }) {
                        Text("See all", color = FTRed)
                    }
                }
            }

            // ── Curated cards ────────────────────────────────────────
            items(sampleVideos) { video ->
                HomeVideoCard(
                    title      = video.title,
                    channel    = video.channel,
                    views      = video.views,
                    url        = video.url,
                    onOpen     = {
                        viewModel.openInBrave(context, video.url, video.title, channel = video.channel)
                    },
                    onBookmark = {
                        viewModel.bookmarkVideo(video.title, video.url, channel = video.channel)
                    }
                )
            }
        }
    }
}

// ── Quick-launch chip ────────────────────────────────────────────────────────
@Composable
private fun QuickLaunchChip(emoji: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier         = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(BlackCard)
                .border(1.dp, DividerColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = WhiteDim)
    }
}

// ── Home video row card ──────────────────────────────────────────────────────
@Composable
private fun HomeVideoCard(
    title: String,
    channel: String,
    views: String,
    url: String,
    onOpen: () -> Unit,
    onBookmark: () -> Unit
) {
    var bookmarked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        shape  = RoundedCornerShape(12.dp)
    ) {
        // Thumbnail placeholder (gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PlayCircle,
                contentDescription = null,
                tint     = White.copy(alpha = 0.3f),
                modifier = Modifier.size(52.dp)
            )
            // Duration badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Black.copy(alpha = 0.85f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("12:34", style = MaterialTheme.typography.labelSmall, color = White)
            }
        }

        // Info row
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel avatar
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FTRedDim),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    channel.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color      = White,
                    fontSize   = 14.sp
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = title,
                    style    = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    color    = White
                )
                Text(
                    text  = "$channel · $views views",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteMuted
                )
            }
            IconButton(onClick = {
                bookmarked = !bookmarked
                onBookmark()
            }) {
                Icon(
                    imageVector        = if (bookmarked) Icons.Filled.BookmarkAdded
                                         else            Icons.Filled.BookmarkAdd,
                    contentDescription = "Bookmark",
                    tint               = if (bookmarked) FTRed else WhiteMuted
                )
            }
        }
    }
}

// ── Sample video data (static — no API key needed) ───────────────────────────
private data class SampleVideo(val title: String, val channel: String, val views: String, val url: String)

private val sampleVideos = listOf(
    SampleVideo("10 Android Tips You Didn't Know",         "Android Developers", "2.1M",  "https://www.youtube.com/results?search_query=android+tips"),
    SampleVideo("Jetpack Compose Full Course 2024",         "Philipp Lackner",    "890K",  "https://www.youtube.com/results?search_query=jetpack+compose+course"),
    SampleVideo("How to Build an APK Without Android Studio","CodingWithMitch",   "430K",  "https://www.youtube.com/results?search_query=build+apk+android+studio"),
    SampleVideo("AMOLED Wallpapers That Actually Look Good", "TechBurner",        "1.3M",  "https://www.youtube.com/results?search_query=amoled+wallpaper"),
    SampleVideo("Brave Browser vs Chrome — Who Wins in 2024","ThioJoe",           "670K",  "https://www.youtube.com/results?search_query=brave+browser+vs+chrome"),
    SampleVideo("Material You Design Deep Dive",             "Google Design",     "220K",  "https://www.youtube.com/results?search_query=material+you+design")
)

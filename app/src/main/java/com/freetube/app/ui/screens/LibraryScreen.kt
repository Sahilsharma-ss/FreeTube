package com.freetube.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freetube.app.data.model.Bookmark
import com.freetube.app.data.model.HistoryItem
import com.freetube.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private enum class LibraryTab { BOOKMARKS, HISTORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    libraryVM:     LibraryViewModel = hiltViewModel(),
    homeVM:        HomeViewModel    = hiltViewModel()
) {
    val context     = LocalContext.current
    var activeTab   by remember { mutableStateOf(LibraryTab.BOOKMARKS) }
    val bookmarks   by libraryVM.bookmarks.collectAsState()
    val history     by libraryVM.history.collectAsState()
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest   = { showClearHistoryDialog = false },
            containerColor     = BlackCard,
            title              = { Text("Clear History", color = White) },
            text               = { Text("Remove all watched history?", color = WhiteDim) },
            confirmButton      = {
                TextButton(onClick = {
                    libraryVM.clearHistory()
                    showClearHistoryDialog = false
                }) { Text("Clear", color = FTRed) }
            },
            dismissButton      = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = WhiteDim)
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
                    .background(Brush.verticalGradient(listOf(BlackSurface, Black)))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp)
            ) {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    Text("Library", style = MaterialTheme.typography.titleLarge, color = White)
                    AnimatedVisibility(visible = activeTab == LibraryTab.HISTORY && history.isNotEmpty()) {
                        IconButton(onClick = { showClearHistoryDialog = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear history", tint = WhiteMuted)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Tab row
                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor   = Black,
                    contentColor     = FTRed,
                    indicator        = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier  = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                            height    = 2.dp,
                            color     = FTRed
                        )
                    }
                ) {
                    LibraryTab.values().forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick  = { activeTab = tab },
                            text = {
                                Text(
                                    text  = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (activeTab == tab) FTRed else WhiteMuted
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->

        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                slideInHorizontally { if (targetState == LibraryTab.HISTORY) it else -it } togetherWith
                slideOutHorizontally { if (targetState == LibraryTab.HISTORY) -it else it }
            },
            label = "libraryTab"
        ) { tab ->
            when (tab) {
                LibraryTab.BOOKMARKS -> BookmarksList(
                    bookmarks   = bookmarks,
                    padding     = padding,
                    onOpen      = { bm -> homeVM.openInBrave(context, bm.url, bm.title) },
                    onDelete    = { bm -> libraryVM.deleteBookmark(bm) }
                )
                LibraryTab.HISTORY   -> HistoryList(
                    history     = history,
                    padding     = padding,
                    onOpen      = { item -> homeVM.openInBrave(context, item.url, item.title) },
                    onDelete    = { item -> libraryVM.deleteHistoryItem(item) }
                )
            }
        }
    }
}

// ── Bookmarks list ────────────────────────────────────────────────────────────
@Composable
private fun BookmarksList(
    bookmarks: List<Bookmark>,
    padding: PaddingValues,
    onOpen: (Bookmark) -> Unit,
    onDelete: (Bookmark) -> Unit
) {
    if (bookmarks.isEmpty()) {
        EmptyState(
            emoji   = "🔖",
            title   = "No bookmarks yet",
            message = "Tap the bookmark icon on any video to save it here."
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(
            top    = padding.calculateTopPadding() + 12.dp,
            bottom = padding.calculateBottomPadding() + 80.dp,
            start  = 16.dp,
            end    = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(bookmarks, key = { it.id }) { bm ->
            LibraryItemCard(
                title       = bm.title,
                subtitle    = bm.channelName,
                date        = formatDate(bm.savedAt),
                leadingIcon = Icons.Filled.Bookmark,
                iconTint    = FTRed,
                onOpen      = { onOpen(bm) },
                onDelete    = { onDelete(bm) }
            )
        }
    }
}

// ── History list ──────────────────────────────────────────────────────────────
@Composable
private fun HistoryList(
    history: List<HistoryItem>,
    padding: PaddingValues,
    onOpen: (HistoryItem) -> Unit,
    onDelete: (HistoryItem) -> Unit
) {
    if (history.isEmpty()) {
        EmptyState(
            emoji   = "🕐",
            title   = "No history yet",
            message = "Videos you open in Brave will appear here."
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(
            top    = padding.calculateTopPadding() + 12.dp,
            bottom = padding.calculateBottomPadding() + 80.dp,
            start  = 16.dp,
            end    = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(history, key = { it.id }) { item ->
            LibraryItemCard(
                title       = item.title,
                subtitle    = item.channelName,
                date        = formatDate(item.watchedAt),
                leadingIcon = Icons.Filled.History,
                iconTint    = WhiteMuted,
                onOpen      = { onOpen(item) },
                onDelete    = { onDelete(item) }
            )
        }
    }
}

// ── Reusable card ─────────────────────────────────────────────────────────────
@Composable
private fun LibraryItemCard(
    title:       String,
    subtitle:    String,
    date:        String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint:    androidx.compose.ui.graphics.Color,
    onOpen:      () -> Unit,
    onDelete:    () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BlackElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(leadingIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = title,
                    style    = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color    = White
                )
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WhiteMuted)
                }
                Text(date, style = MaterialTheme.typography.labelSmall, color = WhiteMuted)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = WhiteMuted)
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(emoji: String, title: String, message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 52.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = White)
            Spacer(Modifier.height(6.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = WhiteMuted)
        }
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Suppress("UNUSED")
private fun Modifier.tabIndicatorOffset(
    tabPosition: androidx.compose.material3.TabPosition
) = this.then(
    Modifier
        .wrapContentSize(Alignment.BottomCenter)
        .width(tabPosition.width)
        .offset(x = tabPosition.left)
)

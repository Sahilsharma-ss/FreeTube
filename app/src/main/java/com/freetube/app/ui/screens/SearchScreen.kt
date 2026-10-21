package com.freetube.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freetube.app.navigation.Screen
import com.freetube.app.ui.theme.*

private val searchSuggestions = listOf(
    "Jetpack Compose tutorial",
    "Android 14 features",
    "Brave browser tips",
    "Kotlin coroutines explained",
    "Best AMOLED wallpapers",
    "YouTube vanced alternative",
    "Material You apps 2024",
    "Android hidden settings"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context      = LocalContext.current
    val focusManager = LocalFocusManager.current
    val searchQuery  by viewModel.searchQuery.collectAsState()
    var showSuggestions by remember { mutableStateOf(true) }

    val performSearch = { query: String ->
        if (query.isNotBlank()) {
            viewModel.searchOnYouTube(context, query)
            focusManager.clearFocus()
            navController.navigate(Screen.Home.route)
        }
    }

    Scaffold(
        containerColor = Black,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(BlackSurface, Black))
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    "Search",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = {
                        viewModel.onSearchQueryChange(it)
                        showSuggestions = it.isBlank()
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("What do you want to watch?", color = WhiteMuted) },
                    leadingIcon   = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = WhiteMuted)
                    },
                    trailingIcon  = {
                        Row {
                            AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.onSearchQueryChange("")
                                    showSuggestions = true
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = WhiteMuted)
                                }
                            }
                            IconButton(onClick = { performSearch(searchQuery) }) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "Search", tint = FTRed)
                            }
                        }
                    },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
                    shape  = RoundedCornerShape(14.dp),
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
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Suggestions or "Search on YouTube" header ────────────
            item {
                AnimatedContent(targetState = showSuggestions) { showSug ->
                    if (showSug) {
                        Text(
                            "🔍  Suggested searches",
                            style = MaterialTheme.typography.titleSmall,
                            color = WhiteDim,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.searchOnYouTube(context, searchQuery)
                                    focusManager.clearFocus()
                                }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint     = FTRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Search \"$searchQuery\" on YouTube",
                                style = MaterialTheme.typography.bodyLarge,
                                color = White
                            )
                        }
                    }
                }
            }

            // ── Suggestion list ──────────────────────────────────────
            if (showSuggestions) {
                items(searchSuggestions) { suggestion ->
                    SuggestionRow(
                        text    = suggestion,
                        onClick = {
                            viewModel.onSearchQueryChange(suggestion)
                            performSearch(suggestion)
                        }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                }
            } else {
                // Filtered suggestions
                val filtered = searchSuggestions.filter {
                    it.contains(searchQuery, ignoreCase = true)
                }
                items(filtered) { suggestion ->
                    SuggestionRow(
                        text    = suggestion,
                        onClick = {
                            viewModel.onSearchQueryChange(suggestion)
                            performSearch(suggestion)
                        }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                }
            }

            // ── Popular topics ───────────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    "🔥  Popular Topics",
                    style = MaterialTheme.typography.titleSmall,
                    color = WhiteDim
                )
                Spacer(Modifier.height(12.dp))
            }

            items(popularTopics) { topic ->
                TopicCard(
                    emoji   = topic.first,
                    label   = topic.second,
                    onClick = { performSearch(topic.second) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SuggestionRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.TrendingUp,
            contentDescription = null,
            tint     = WhiteMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = WhiteDim)
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Filled.NorthWest,
            contentDescription = null,
            tint     = WhiteMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun TopicCard(emoji: String, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BlackCard),
        shape  = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.width(14.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = White)
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = WhiteMuted
            )
        }
    }
}

private val popularTopics = listOf(
    "🎮" to "Gaming",
    "🎵" to "Music",
    "📱" to "Technology",
    "🍕" to "Food & Cooking",
    "⚽" to "Sports",
    "🎬" to "Movies & TV",
    "💪" to "Fitness",
    "🌍" to "Travel"
)

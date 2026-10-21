package com.freetube.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freetube.app.data.local.BookmarkDao
import com.freetube.app.data.local.HistoryDao
import com.freetube.app.data.model.Bookmark
import com.freetube.app.data.model.HistoryItem
import com.freetube.app.ui.PlayerStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val historyDao:  HistoryDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }

    /**
     * Plays the video or loads the URL directly in the in-app YouTube player.
     * Records to history automatically.
     */
    fun openVideo(
        url:       String,
        title:     String = "",
        thumbnail: String = "",
        channel:   String = ""
    ) {
        viewModelScope.launch {
            historyDao.insertHistoryItem(
                HistoryItem(
                    title       = title.ifBlank { "YouTube Video" },
                    url         = url,
                    thumbnail   = thumbnail,
                    channelName = channel
                )
            )
        }
        PlayerStateManager.loadUrl(url)
    }

    // Keep backwards compatibility for any callers passing context
    fun openInBrave(
        context:   Context,
        url:       String,
        title:     String,
        thumbnail: String = "",
        channel:   String = ""
    ) {
        openVideo(url, title, thumbnail, channel)
    }

    fun bookmarkVideo(title: String, url: String, thumbnail: String = "", channel: String = "") =
        viewModelScope.launch {
            bookmarkDao.insertBookmark(
                Bookmark(title = title, url = url, thumbnail = thumbnail, channelName = channel)
            )
        }

    fun searchOnYouTube(context: Context, query: String) {
        val searchUrl = "https://m.youtube.com/results?search_query=${Uri.encode(query)}"
        openVideo(searchUrl, "Search: $query")
    }
}

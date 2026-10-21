package com.freetube.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freetube.app.data.local.BookmarkDao
import com.freetube.app.data.local.HistoryDao
import com.freetube.app.data.model.Bookmark
import com.freetube.app.data.model.HistoryItem
import com.freetube.app.util.BraveHelper
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

    // Dialog state and pending URL
    private val _showBraveDialog = MutableStateFlow(false)
    val showBraveDialog: StateFlow<Boolean> = _showBraveDialog

    private var pendingUrl: String? = null

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    fun dismissBraveDialog()          { 
        _showBraveDialog.value = false
        pendingUrl = null
    }

    /** Returns true if Brave is installed on this device. */
    fun isBraveInstalled(context: Context): Boolean = BraveHelper.isBraveInstalled(context)

    /**
     * Opens the given URL in Brave Browser.
     * Records watch history and launches Brave.
     * If Brave launch fails, opens the dialog allowing install or fallback.
     */
    fun openInBrave(
        context:   Context,
        url:       String,
        title:     String,
        thumbnail: String = "",
        channel:   String = ""
    ) {
        // Record to history
        viewModelScope.launch {
            historyDao.insertHistoryItem(
                HistoryItem(title = title, url = url, thumbnail = thumbnail, channelName = channel)
            )
        }

        val success = BraveHelper.launchUrlInBrave(context, url)
        if (!success) {
            pendingUrl = url
            _showBraveDialog.value = true
        }
    }

    /** Opens the pending URL in any available browser if user opts to continue without Brave. */
    fun openPendingInDefaultBrowser(context: Context) {
        pendingUrl?.let { url ->
            BraveHelper.launchUrlInAnyBrowser(context, url)
        }
        dismissBraveDialog()
    }

    /** Opens the Play Store page for Brave Browser. */
    fun openBraveOnPlayStore(context: Context) {
        BraveHelper.openBravePlayStore(context)
        _showBraveDialog.value = false
    }

    fun bookmarkVideo(title: String, url: String, thumbnail: String = "", channel: String = "") =
        viewModelScope.launch {
            bookmarkDao.insertBookmark(
                Bookmark(title = title, url = url, thumbnail = thumbnail, channelName = channel)
            )
        }

    fun searchOnYouTube(context: Context, query: String) {
        val searchUrl = "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
        openInBrave(context, searchUrl, "Search: $query")
    }
}

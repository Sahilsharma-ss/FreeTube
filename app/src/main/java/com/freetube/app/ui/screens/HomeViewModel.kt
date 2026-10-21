package com.freetube.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freetube.app.data.local.BookmarkDao
import com.freetube.app.data.local.HistoryDao
import com.freetube.app.data.model.Bookmark
import com.freetube.app.data.model.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val BRAVE_PACKAGE = "com.brave.browser"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val historyDao:  HistoryDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Whether to show the "Brave not installed" dialog
    private val _showBraveDialog = MutableStateFlow(false)
    val showBraveDialog: StateFlow<Boolean> = _showBraveDialog

    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    fun dismissBraveDialog()          { _showBraveDialog.value = false }

    /** Returns true if Brave is installed on this device. */
    fun isBraveInstalled(context: Context): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(BRAVE_PACKAGE, 0)
            true
        }.getOrDefault(false)

    /**
     * Opens the given URL in Brave Browser.
     * If Brave is not installed, shows the install dialog instead.
     */
    fun openInBrave(
        context:   Context,
        url:       String,
        title:     String,
        thumbnail: String = "",
        channel:   String = ""
    ) {
        // Record to history regardless
        viewModelScope.launch {
            historyDao.insertHistoryItem(
                HistoryItem(title = title, url = url, thumbnail = thumbnail, channelName = channel)
            )
        }

        if (!isBraveInstalled(context)) {
            _showBraveDialog.value = true
            return
        }

        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage(BRAVE_PACKAGE)
            }
        )
    }

    /** Opens the Play Store page for Brave Browser. */
    fun openBraveOnPlayStore(context: Context) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$BRAVE_PACKAGE"))
        )
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

package com.freetube.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freetube.app.data.local.BookmarkDao
import com.freetube.app.data.local.HistoryDao
import com.freetube.app.data.model.Bookmark
import com.freetube.app.data.model.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val historyDao:  HistoryDao
) : ViewModel() {

    val bookmarks: StateFlow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<HistoryItem>> = historyDao.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteBookmark(bookmark: Bookmark) = viewModelScope.launch {
        bookmarkDao.deleteBookmark(bookmark)
    }

    fun deleteHistoryItem(item: HistoryItem) = viewModelScope.launch {
        historyDao.deleteHistoryItem(item)
    }

    fun clearHistory() = viewModelScope.launch {
        historyDao.clearHistory()
    }
}

package com.freetube.app.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PlayerStateManager {
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen

    private val _loadUrlEvent = MutableStateFlow<String?>("https://m.youtube.com")
    val loadUrlEvent: StateFlow<String?> = _loadUrlEvent

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }

    fun loadUrl(url: String) {
        _loadUrlEvent.value = url
    }

    fun clearLoadUrlEvent() {
        _loadUrlEvent.value = null
    }
}

package com.freetube.app.ui

import android.view.View
import android.webkit.WebChromeClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PlayerStateManager {
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen

    private val _customView = MutableStateFlow<View?>(null)
    val customView: StateFlow<View?> = _customView
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    /** One-shot request to open a YouTube URL (deep link / shared link). */
    private val _loadUrlEvent = MutableStateFlow<String?>(null)
    val loadUrlEvent: StateFlow<String?> = _loadUrlEvent

    fun showCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        customViewCallback = callback
        _customView.value = view
        _isFullscreen.value = view != null
    }

    fun hideCustomView() {
        val cb = customViewCallback
        customViewCallback = null
        _customView.value = null
        _isFullscreen.value = false
        cb?.onCustomViewHidden()
    }

    fun loadUrl(url: String) {
        _loadUrlEvent.value = url
    }

    fun clearLoadUrlEvent() {
        _loadUrlEvent.value = null
    }
}

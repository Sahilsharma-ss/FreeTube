package com.freetube.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.freetube.app.ui.PlayerStateManager
import com.freetube.app.util.AdBlockEngine

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeScreen(
    initialUrl: String = "https://m.youtube.com"
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var webViewState by rememberSaveable { mutableStateOf<Bundle?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val pendingUrlToLoad by PlayerStateManager.loadUrlEvent.collectAsState()

    // Handle incoming URL change events
    LaunchedEffect(pendingUrlToLoad) {
        pendingUrlToLoad?.let { url ->
            webViewRef?.let { wv ->
                if (wv.url != url) {
                    wv.loadUrl(url)
                }
            }
            PlayerStateManager.clearLoadUrlEvent()
        }
    }

    // Android Hardware Back button handling
    BackHandler(enabled = customView != null || webViewRef?.canGoBack() == true) {
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
            customView = null
            customViewCallback = null
            PlayerStateManager.setFullscreen(false)
        } else if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        }
    }

    DisposableEffect(lifecycleOwner, webViewRef) {
        val webView = webViewRef ?: return@DisposableEffect onDispose { }
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                webView.onResume()
                webView.resumeTimers()
            }

            override fun onPause(owner: LifecycleOwner) {
                webView.onPause()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            val state = Bundle()
            webView.saveState(state)
            webViewState = state
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        if (customView != null) {
            // Fullscreen video player view
            AndroidView(
                factory = {
                    customView!!.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Pure In-App YouTube (NO URL bar, pure native web app)
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        setBackgroundColor(0xFF0F0F0F.toInt())

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            setSupportMultipleWindows(false)
                            userAgentString = AdBlockEngine.USER_AGENT
                            cacheMode = WebSettings.LOAD_DEFAULT
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = false
                            displayZoomControls = false
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                view?.let { AdBlockEngine.inject(it) }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.let { AdBlockEngine.inject(it) }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false

                                // Keep all YouTube, Google Auth, and internal media inside the app
                                if (url.contains("youtube.com") ||
                                    url.contains("youtu.be") ||
                                    url.contains("google.com/accounts") ||
                                    url.contains("accounts.google")
                                ) {
                                    return false
                                }

                                // External links (e.g. twitter, patron) open in system viewer
                                return try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    ctx.startActivity(intent)
                                    true
                                } catch (_: Exception) {
                                    false
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                customView = view
                                customViewCallback = callback
                                PlayerStateManager.setFullscreen(true)
                            }

                            override fun onHideCustomView() {
                                customView = null
                                customViewCallback = null
                                PlayerStateManager.setFullscreen(false)
                            }
                        }

                        val savedState = webViewState
                        if (savedState != null) {
                            restoreState(savedState)
                        } else {
                            loadUrl(initialUrl)
                        }
                        webViewRef = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            )
        }
    }
}

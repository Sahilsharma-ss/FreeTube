package com.freetube.app.ui.screens

import android.app.Activity
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.freetube.app.playback.YouTubeWebHolder
import com.freetube.app.ui.PlayerStateManager

/**
 * The whole app: YouTube's own mobile UI, ad-free, in a WebView that lives in
 * [YouTubeWebHolder] so it survives tab changes, rotation and backgrounding.
 */
@Composable
fun YouTubeScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val customView by PlayerStateManager.customView.collectAsState()
    val pendingUrl by PlayerStateManager.loadUrlEvent.collectAsState()

    val webView = YouTubeWebHolder.getOrCreate(context)

    // Lend the WebView an Activity context while we're on screen, hand it back afterwards.
    DisposableEffect(webView) {
        YouTubeWebHolder.attach(context)
        onDispose { YouTubeWebHolder.detach(context) }
    }

    // NOTE: deliberately no webView.onPause(): that is what suspends media in the background.
    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                webView.onResume()
                webView.resumeTimers()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(pendingUrl) {
        pendingUrl?.let {
            YouTubeWebHolder.load(it)
            PlayerStateManager.clearLoadUrlEvent()
        }
    }

    // Back: exit fullscreen -> navigate YouTube history -> otherwise send app to background
    // (not finish), so audio keeps playing like a browser would.
    BackHandler {
        when {
            customView != null -> PlayerStateManager.hideCustomView()
            webView.canGoBack() -> webView.goBack()
            else -> (context as? Activity)?.moveTaskToBack(true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        // The WebView stays in the tree even in fullscreen, so it is never detached
        // (detaching hides the page and would pause the video).
        AndroidView(
            factory = {
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webView
            },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        )

        customView?.let { view ->
            AndroidView(
                factory = {
                    (view.parent as? ViewGroup)?.removeView(view)
                    view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    view
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    }
}

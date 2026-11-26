package com.freetube.app.playback

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.MutableContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent
import com.freetube.app.ui.PlayerStateManager
import com.freetube.app.util.AdBlockEngine

/**
 * Owns the single WebView for the whole process.
 *
 * Why a singleton: if the WebView belonged to a composable, it was destroyed whenever the
 * user switched tab / rotated / the activity was recreated — and the audio died with it.
 * Here it outlives the Activity; the Activity only borrows it ([attach]/[detach]).
 */
object YouTubeWebHolder {

    const val HOME_URL = "https://m.youtube.com"

    private var webView: WebView? = null

    fun peek(): WebView? = webView

    /** True for https URLs that should stay inside the app. */
    fun isInternalUrl(uri: Uri): Boolean {
        if (uri.scheme != "https") return false
        val host = uri.host?.lowercase() ?: return false
        return host == "youtu.be" ||
            host == "youtube.com" || host.endsWith(".youtube.com") ||
            host == "accounts.google.com" || host.endsWith(".accounts.google.com") ||
            host == "consent.youtube.com" || host == "myaccount.google.com"
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getOrCreate(appContext: Context): WebView {
        webView?.let { return it }
        val wv = BackgroundSafeWebView(MutableContextWrapper(appContext.applicationContext))
        wv.setBackgroundColor(0xFF0F0F0F.toInt())

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(false)
            userAgentString = AdBlockEngine.USER_AGENT
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = false
            displayZoomControls = false
            // Match YouTube's dark theme (the page honours prefers-color-scheme).
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                forceDark = WebSettings.FORCE_DARK_ON
            }
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)
        wv.addJavascriptInterface(MediaBridge(), "FreeTubeBridge")

        wv.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                view?.let { AdBlockEngine.inject(it) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.let { AdBlockEngine.inject(it) }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                if (isInternalUrl(uri)) return false
                // Anything else (ads landing pages, external links) goes to the system browser.
                return try {
                    view?.context?.startActivity(
                        Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    true
                } catch (_: Exception) {
                    true
                }
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                PlayerStateManager.showCustomView(view, callback)
            }

            override fun onHideCustomView() {
                PlayerStateManager.hideCustomView()
            }
        }

        wv.loadUrl(HOME_URL)
        webView = wv
        return wv
    }

    /** Lend the WebView to an Activity so dialogs / file pickers get a real Activity context. */
    fun attach(activityContext: Context) {
        (webView?.context as? MutableContextWrapper)?.baseContext = activityContext
    }

    /** Give the Activity back so it can be garbage collected while audio keeps playing. */
    fun detach(appContext: Context) {
        (webView?.context as? MutableContextWrapper)?.baseContext = appContext.applicationContext
    }

    fun load(url: String) {
        webView?.let { if (it.url != url) it.loadUrl(url) }
    }

    fun release() {
        webView?.let {
            it.stopLoading()
            (it.parent as? android.view.ViewGroup)?.removeView(it)
            it.destroy()
        }
        webView = null
        PlaybackController.reset()
    }

    /**
     * A WebView hides its page when its window goes GONE (app backgrounded / screen off),
     * and Chromium then suspends media. Ignoring GONE makes the page believe it is still
     * on screen, so audio keeps flowing while the foreground service keeps the process alive.
     */
    private class BackgroundSafeWebView(context: Context) : WebView(context) {
        override fun onWindowVisibilityChanged(visibility: Int) {
            if (visibility != View.GONE) super.onWindowVisibilityChanged(visibility)
        }
    }
}

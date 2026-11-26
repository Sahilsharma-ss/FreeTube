package com.freetube.app.playback

import android.webkit.JavascriptInterface

/**
 * Exposed to the page as `window.FreeTubeBridge`. Methods are invoked on a WebView
 * background thread, and the page is untrusted input, so everything is length-capped.
 */
class MediaBridge {

    @JavascriptInterface
    fun onMediaState(
        hasMedia: Boolean,
        title: String?,
        artist: String?,
        artworkUrl: String?,
        playing: Boolean,
        positionMs: Double,
        durationMs: Double
    ) {
        PlaybackController.update(
            MediaState(
                hasMedia = hasMedia,
                title = title.orEmpty().take(200),
                artist = artist.orEmpty().take(200),
                artworkUrl = artworkUrl.orEmpty().take(500),
                playing = hasMedia && playing,
                positionMs = positionMs.coerceIn(0.0, 86_400_000.0).toLong(),
                durationMs = durationMs.coerceIn(0.0, 86_400_000.0).toLong()
            )
        )
    }
}

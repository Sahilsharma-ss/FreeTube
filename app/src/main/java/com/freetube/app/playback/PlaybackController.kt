package com.freetube.app.playback

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Snapshot of what the YouTube page's <video> element is doing right now. */
data class MediaState(
    val hasMedia: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val artworkUrl: String = "",
    val playing: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

/**
 * Bridge between the native world (notification, lock screen, headset buttons)
 * and the <video> element living inside the WebView.
 *
 *  page -> native : [MediaBridge] calls [update] about once a second.
 *  native -> page : [play]/[pause]/... run a tiny JS call on the main thread.
 */
object PlaybackController {

    private const val SEEK_STEP_SEC = 10

    private val _state = MutableStateFlow(MediaState())
    val state: StateFlow<MediaState> = _state

    private val main = Handler(Looper.getMainLooper())

    fun update(newState: MediaState) {
        _state.value = newState
    }

    fun reset() {
        _state.value = MediaState()
    }

    fun play() = control("play")
    fun pause() = control("pause")
    fun togglePlayPause() = control("toggle")
    fun next() = control("next")
    fun rewind() = control("seekBy", -SEEK_STEP_SEC)
    fun forward() = control("seekBy", SEEK_STEP_SEC)
    fun seekTo(positionMs: Long) = control("seekTo", positionMs / 1000.0)

    private fun control(action: String, arg: Number = 0) {
        // evaluateJavascript must run on the main thread; callers may be on binder/JS threads.
        main.post {
            YouTubeWebHolder.peek()?.evaluateJavascript(
                "window.__ftControl && window.__ftControl('$action', $arg);",
                null
            )
        }
    }
}

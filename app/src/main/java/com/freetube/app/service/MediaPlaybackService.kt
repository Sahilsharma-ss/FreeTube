package com.freetube.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.app.NotificationCompat.MediaStyle
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.freetube.app.MainActivity
import com.freetube.app.playback.MediaState
import com.freetube.app.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Keeps the process alive and the audio flowing while the app is in the background or the
 * screen is off, and exposes the video as a proper media session (notification, lock screen,
 * Bluetooth / headset buttons).
 */
class MediaPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "freetube_playback"
        const val NOTIFICATION_ID = 1001

        const val ACTION_TOGGLE = "com.freetube.app.ACTION_TOGGLE"
        const val ACTION_REWIND = "com.freetube.app.ACTION_REWIND"
        const val ACTION_FORWARD = "com.freetube.app.ACTION_FORWARD"
        const val ACTION_STOP = "com.freetube.app.ACTION_STOP"

        private const val NO_MEDIA_TIMEOUT_MS = 15_000L
        private const val PAUSED_TIMEOUT_MS = 10 * 60_000L
        private const val WAKELOCK_MAX_MS = 6 * 60 * 60_000L

        fun start(context: Context) {
            try {
                ContextCompat.startForegroundService(context, Intent(context, MediaPlaybackService::class.java))
            } catch (_: Exception) {
                // Background-start restrictions: playback just won't get a notification.
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MediaPlaybackService::class.java))
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var session: MediaSessionCompat
    private var wakeLock: PowerManager.WakeLock? = null
    private var collectJob: Job? = null
    private var idleJob: Job? = null
    private var artworkJob: Job? = null

    private var artwork: Bitmap? = null
    private var artworkUrl = ""
    private var lastNotified: MediaState? = null
    private var lastSessionState: MediaState? = null
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        session = MediaSessionCompat(this, "FreeTubeSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = PlaybackController.play()
                override fun onPause() = PlaybackController.pause()
                override fun onSkipToNext() = PlaybackController.next()
                override fun onFastForward() = PlaybackController.forward()
                override fun onRewind() = PlaybackController.rewind()
                override fun onSeekTo(pos: Long) = PlaybackController.seekTo(pos)
                override fun onStop() = shutDown()
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> PlaybackController.togglePlayPause()
            ACTION_REWIND -> PlaybackController.rewind()
            ACTION_FORWARD -> PlaybackController.forward()
            ACTION_STOP -> {
                shutDown()
                return START_NOT_STICKY
            }
        }

        // Must happen right away after startForegroundService(), or Android kills the app.
        val state = PlaybackController.state.value
        if (!isForeground) {
            promote(buildNotification(state))
        }

        if (collectJob == null) {
            collectJob = scope.launch {
                PlaybackController.state.collect { render(it) }
            }
        }
        return START_NOT_STICKY
    }

    private fun promote(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isForeground = true
    }

    private fun render(state: MediaState) {
        updateWakeLock(state.playing)
        scheduleIdleStop(state)
        loadArtworkIfNeeded(state.artworkUrl)
        updateSession(state)

        // Rebuild the notification only when something visible changed (not every second).
        val prev = lastNotified
        if (prev == null || prev.title != state.title || prev.artist != state.artist ||
            prev.playing != state.playing
        ) {
            lastNotified = state
            notifyNow(state)
        }
    }

    private fun notifyNow(state: MediaState) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun updateSession(state: MediaState) {
        val prev = lastSessionState
        val expected = if (prev != null && prev.playing) prev.positionMs + (System.currentTimeMillis() - lastSessionAt) else -1L
        val needsPlaybackUpdate = prev == null || prev.playing != state.playing ||
            prev.durationMs != state.durationMs || abs(state.positionMs - expected) > 2_000

        if (prev == null || prev.title != state.title || prev.artist != state.artist ||
            prev.durationMs != state.durationMs
        ) {
            setMetadata(state)
        }
        if (needsPlaybackUpdate) {
            val actions = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_FAST_FORWARD or PlaybackStateCompat.ACTION_REWIND or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_STOP
            session.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(actions)
                    .setState(
                        if (state.playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                        state.positionMs,
                        if (state.playing) 1f else 0f
                    )
                    .build()
            )
            lastSessionAt = System.currentTimeMillis()
        }
        lastSessionState = state
    }

    private var lastSessionAt = 0L

    private fun setMetadata(state: MediaState) {
        session.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, state.title.ifBlank { "FreeTube" })
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, state.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, state.durationMs)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                .build()
        )
    }

    private fun loadArtworkIfNeeded(url: String) {
        if (url == artworkUrl) return
        artworkUrl = url
        artwork = null
        artworkJob?.cancel()
        val uri = runCatching { Uri.parse(url) }.getOrNull()
        val host = uri?.host?.lowercase()
        // Only fetch thumbnails from Google's image CDNs; the URL came from a web page.
        val trusted = uri?.scheme == "https" && host != null &&
            (host.endsWith("ytimg.com") || host.endsWith("ggpht.com") || host.endsWith("googleusercontent.com"))
        if (!trusted) return

        artworkJob = scope.launch {
            val request = ImageRequest.Builder(this@MediaPlaybackService)
                .data(url)
                .allowHardware(false)
                .size(512)
                .build()
            val result = ImageLoader(this@MediaPlaybackService).execute(request)
            val bmp = (result as? SuccessResult)?.drawable?.toBitmap()
            if (bmp != null && url == artworkUrl) {
                artwork = bmp
                val s = PlaybackController.state.value
                setMetadata(s)
                notifyNow(s)
            }
        }
    }

    private fun updateWakeLock(playing: Boolean) {
        if (playing) {
            if (wakeLock?.isHeld != true) {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FreeTube:playback").apply {
                    setReferenceCounted(false)
                    acquire(WAKELOCK_MAX_MS)
                }
            }
        } else {
            wakeLock?.takeIf { it.isHeld }?.release()
        }
    }

    /** Stop ourselves if nothing is playing for long enough, so we never linger as a zombie. */
    private fun scheduleIdleStop(state: MediaState) {
        idleJob?.cancel()
        if (state.playing) return
        val timeout = if (state.hasMedia) PAUSED_TIMEOUT_MS else NO_MEDIA_TIMEOUT_MS
        idleJob = scope.launch {
            delay(timeout)
            shutDown()
        }
    }

    private fun shutDown() {
        PlaybackController.pause()
        stopForeground(STOP_FOREGROUND_REMOVE)
        isForeground = false
        stopSelf()
    }

    private fun buildNotification(state: MediaState): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun action(icon: Int, title: String, act: String, code: Int) = NotificationCompat.Action(
            icon, title,
            PendingIntent.getService(
                this, code,
                Intent(this, MediaPlaybackService::class.java).setAction(act),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(state.title.ifBlank { "FreeTube" })
            .setContentText(state.artist)
            .setLargeIcon(artwork)
            .setContentIntent(openIntent)
            .setOngoing(state.playing)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(action(android.R.drawable.ic_media_rew, "Back 10s", ACTION_REWIND, 1))
            .addAction(
                if (state.playing) action(android.R.drawable.ic_media_pause, "Pause", ACTION_TOGGLE, 2)
                else action(android.R.drawable.ic_media_play, "Play", ACTION_TOGGLE, 2)
            )
            .addAction(action(android.R.drawable.ic_media_ff, "Forward 10s", ACTION_FORWARD, 3))
            .addAction(action(android.R.drawable.ic_menu_close_clear_cancel, "Close", ACTION_STOP, 4))
            .setStyle(
                MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Media Playback", NotificationManager.IMPORTANCE_LOW).apply {
                description = "FreeTube background playback controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        wakeLock?.takeIf { it.isHeld }?.release()
        scope.cancel()
        session.isActive = false
        session.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

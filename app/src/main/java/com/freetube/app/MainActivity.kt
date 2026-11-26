package com.freetube.app

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.freetube.app.navigation.AppNavigation
import com.freetube.app.playback.PlaybackController
import com.freetube.app.playback.YouTubeWebHolder
import com.freetube.app.service.MediaPlaybackService
import com.freetube.app.ui.PlayerStateManager
import com.freetube.app.ui.theme.FreeTubeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Android 13+: without this the playback notification is silently not shown.
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        if (savedInstanceState == null) handleViewIntent(intent)

        setContent {
            FreeTubeTheme {
                val systemUiController = rememberSystemUiController()
                val isFullscreen by PlayerStateManager.isFullscreen.collectAsState()
                val darkTheme = isSystemInDarkTheme()
                val bgColor = MaterialTheme.colorScheme.background

                SideEffect {
                    if (isFullscreen) {
                        systemUiController.isSystemBarsVisible = false
                    } else {
                        systemUiController.isSystemBarsVisible = true
                        systemUiController.setStatusBarColor(color = bgColor, darkIcons = !darkTheme)
                        systemUiController.setNavigationBarColor(color = bgColor, darkIcons = !darkTheme)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleViewIntent(intent)
    }

    /** youtube.com / youtu.be links shared from other apps open inside FreeTube. */
    private fun handleViewIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return
        if (YouTubeWebHolder.isInternalUrl(uri)) PlayerStateManager.loadUrl(uri.toString())
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        // Back on screen: the page itself is the player, so the notification isn't needed.
        MediaPlaybackService.stop(this)
    }

    override fun onPause() {
        super.onPause()
        // Leaving the app (home, app switch, screen off): hand playback to the foreground service.
        // Skip when finishing so closing the app actually stops it.
        if (!isFinishing && PlaybackController.state.value.let { it.hasMedia && it.playing }) {
            MediaPlaybackService.start(this)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Picture-in-picture only makes sense for fullscreen video. Otherwise the user wants
        // background audio, not a tiny window showing the whole app.
        val state = PlaybackController.state.value
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            PlayerStateManager.isFullscreen.value && state.playing
        ) {
            try {
                if (!isInPictureInPictureMode) {
                    enterPictureInPictureMode(
                        PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            // User really closed the app: stop audio and free the WebView.
            MediaPlaybackService.stop(this)
            PlayerStateManager.hideCustomView()
            YouTubeWebHolder.release()
        }
        super.onDestroy()
    }
}

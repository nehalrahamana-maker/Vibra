package com.kazinehal.vibra

import android.Manifest
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.google.common.util.concurrent.ListenableFuture
import com.kazinehal.vibra.data.MusicRepository
import com.kazinehal.vibra.data.UserLibraryManager
import com.kazinehal.vibra.player.VibraMediaService
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.components.BottomNavBar
import com.kazinehal.vibra.ui.components.BottomPlayerBar
import com.kazinehal.vibra.ui.components.RivaTab
import com.kazinehal.vibra.ui.components.SettingsDialog
import com.kazinehal.vibra.ui.screens.*
import com.kazinehal.vibra.ui.theme.VibraBackground
import com.kazinehal.vibra.ui.theme.VibraTheme

class MainActivity : ComponentActivity() {

    private lateinit var player: VibraPlayer
    private val seedTracks = MusicRepository.trendingTracks

    // Keeps VibraMediaService bound while the UI is alive, so the MediaSession (and its
    // notification) exists before the first track is even played.
    private var controllerFuture: ListenableFuture<MediaController>? = null

    // Android 13+: media notification (lockscreen/quick-settings controls) needs this
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) android.util.Log.w("MainActivity", "POST_NOTIFICATIONS denied — media controls won't show on the lockscreen")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize persistent user library
        UserLibraryManager.init(applicationContext)

        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        player = VibraPlayer.get(this)
        if (player.queue.isEmpty()) {
            player.queue = seedTracks.toMutableList()
        }

        // Bind to the media service (auto-creates it and its MediaSession)
        val sessionToken = SessionToken(applicationContext, ComponentName(this, VibraMediaService::class.java))
        controllerFuture = MediaController.Builder(applicationContext, sessionToken).buildAsync()

        setContent {
            VibraTheme {
                var selectedTab by remember { mutableStateOf(RivaTab.HOME) }
                var isExpandedPlayerVisible by remember { mutableStateOf(false) }
                var isSettingsOpen by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VibraBackground),
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VibraBackground)
                        ) {
                            // Floating Mini Player Capsule
                            if (player.currentTrack != null) {
                                BottomPlayerBar(
                                    player = player,
                                    onBarClick = { isExpandedPlayerVisible = true }
                                )
                            }
                            // Riva 4-Tab Bottom Navigation Bar
                            BottomNavBar(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        // Dynamic song artwork background glow when song is playing
                        if (player.currentTrack != null) {
                            AsyncImage(
                                model = player.currentTrack!!.artUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.84f),
                                                Color.Black.copy(alpha = 0.94f),
                                                Color.Black
                                            )
                                        )
                                    )
                            )
                        }

                        // 4 Riva Navigation Tabs
                        when (selectedTab) {
                            RivaTab.HOME -> {
                                HomeScreen(
                                    player = player,
                                    tracks = seedTracks,
                                    onTrackSelect = { track ->
                                        if (!player.queue.contains(track)) {
                                            player.queue.add(0, track)
                                        }
                                        player.playTrack(track)
                                    },
                                    onOpenSearch = { selectedTab = RivaTab.SEARCH },
                                    onOpenSettings = { isSettingsOpen = true }
                                )
                            }
                            RivaTab.SEARCH -> {
                                SearchScreen(
                                    player = player,
                                    tracks = seedTracks,
                                    onTrackSelect = { track ->
                                        if (!player.queue.contains(track)) {
                                            player.queue.add(0, track)
                                        }
                                        player.playTrack(track)
                                    }
                                )
                            }
                            RivaTab.LIBRARY -> {
                                LibraryScreen(
                                    player = player,
                                    tracks = seedTracks,
                                    onTrackSelect = { track ->
                                        if (!player.queue.contains(track)) {
                                            player.queue.add(0, track)
                                        }
                                        player.playTrack(track)
                                    }
                                )
                            }
                            RivaTab.MIX -> {
                                MixScreen(
                                    player = player,
                                    tracks = seedTracks,
                                    onTrackSelect = { track ->
                                        if (!player.queue.contains(track)) {
                                            player.queue.add(0, track)
                                        }
                                        player.playTrack(track)
                                    }
                                )
                            }
                        }

                        // Fullscreen Expanded Now Playing Screen
                        AnimatedVisibility(
                            visible = isExpandedPlayerVisible,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            NowPlayingScreen(
                                player = player,
                                onClose = { isExpandedPlayerVisible = false }
                            )
                        }

                        // Settings Dialog
                        if (isSettingsOpen) {
                            SettingsDialog(onDismiss = { isSettingsOpen = false })
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unbind from the media service but NEVER release the singleton player here —
        // playback (and the notification) must survive Activity recreation. media3
        // releases the service itself once nothing is bound and playback has stopped.
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}

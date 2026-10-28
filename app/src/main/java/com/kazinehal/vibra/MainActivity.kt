package com.kazinehal.vibra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import coil.compose.AsyncImage
import com.kazinehal.vibra.data.MusicRepository
import com.kazinehal.vibra.data.UserLibraryManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize persistent user library
        UserLibraryManager.init(applicationContext)

        player = VibraPlayer(applicationContext)
        player.queue = seedTracks.toMutableList()

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
        if (::player.isInitialized) {
            player.release()
        }
    }
}

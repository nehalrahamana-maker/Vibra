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
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.components.BottomNavBar
import com.kazinehal.vibra.ui.components.BottomPlayerBar
import com.kazinehal.vibra.ui.components.SettingsDialog
import com.kazinehal.vibra.ui.components.SimpTab
import com.kazinehal.vibra.ui.screens.*
import com.kazinehal.vibra.ui.theme.VibraBackground
import com.kazinehal.vibra.ui.theme.VibraTheme

class MainActivity : ComponentActivity() {

    private lateinit var player: VibraPlayer

    // Real Worldwide Hit Songs Catalog (Bollywood, Global Pop, Punjabi, Hip-Hop)
    private val seedTracks = MusicRepository.trendingTracks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = VibraPlayer(applicationContext)
        player.queue = seedTracks.toMutableList()

        setContent {
            VibraTheme {
                var selectedTab by remember { mutableStateOf(SimpTab.HOME) }
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
                            // SimpMusic 4-Tab Bottom Navigation Bar
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

                        // 4 SimpMusic Tabs
                        when (selectedTab) {
                            SimpTab.HOME -> {
                                HomeScreen(
                                    player = player,
                                    tracks = seedTracks,
                                    onTrackSelect = { track ->
                                        if (!player.queue.contains(track)) {
                                            player.queue.add(0, track)
                                        }
                                        player.playTrack(track)
                                    },
                                    onOpenSettings = { isSettingsOpen = true }
                                )
                            }
                            SimpTab.MIX -> {
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
                            SimpTab.LIBRARY -> {
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
                            SimpTab.SEARCH -> {
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

                        // Settings & Import Dialog
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
        player.release()
    }
}

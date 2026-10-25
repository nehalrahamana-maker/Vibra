package com.kazinehal.vibra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kazinehal.vibra.model.Track
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.components.BottomPlayerBar
import com.kazinehal.vibra.ui.screens.HomeScreen
import com.kazinehal.vibra.ui.screens.NowPlayingScreen
import com.kazinehal.vibra.ui.theme.VibraTheme

class MainActivity : ComponentActivity() {

    private lateinit var player: VibraPlayer

    // Curated High-Fidelity Audiophile Seed Tracks
    private val seedTracks = listOf(
        Track(
            id = "track-1",
            title = "Neon Eclipse",
            artist = "Vector Drive",
            album = "Synthetic Horizons",
            genre = "Synthwave",
            duration = 218,
            artUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3?filename=synthwave-80s-110045.mp3"
        ),
        Track(
            id = "track-2",
            title = "Midnight Rain & Study Tea",
            artist = "Lofi Future",
            album = "Bedroom Dreams",
            genre = "Lofi",
            duration = 174,
            artUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3?filename=lofi-study-112191.mp3"
        ),
        Track(
            id = "track-3",
            title = "Astra Nebula",
            artist = "Cosmic Pulse",
            album = "Stardust Odyssey",
            genre = "EDM",
            duration = 245,
            artUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3?filename=electronic-future-beats-117997.mp3"
        ),
        Track(
            id = "track-4",
            title = "Flow State Algorithm",
            artist = "Binary Bloom",
            album = "Code & Harmony",
            genre = "Coding Flow",
            duration = 198,
            artUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://cdn.pixabay.com/download/audio/2022/10/14/audio_9939f77c30.mp3?filename=lofi-chill-medium-version-122394.mp3"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = VibraPlayer(applicationContext)
        player.queue = seedTracks.toMutableList()

        setContent {
            VibraTheme {
                var isExpandedPlayerVisible by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (player.currentTrack != null) {
                            BottomPlayerBar(
                                player = player,
                                onBarClick = { isExpandedPlayerVisible = true }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomeScreen(
                            player = player,
                            tracks = seedTracks,
                            onTrackSelect = { track ->
                                player.playTrack(track)
                            }
                        )

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

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

    // Curated High-Fidelity Audiophile Seed Tracks (Verified 200 OK Streams)
    private val seedTracks = listOf(
        Track(
            id = "track-1",
            title = "Neon Eclipse",
            artist = "Vector Drive",
            album = "Synthetic Horizons",
            genre = "Synthwave",
            duration = 372,
            artUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        ),
        Track(
            id = "track-2",
            title = "Midnight Rain & Study Tea",
            artist = "Lofi Future",
            album = "Bedroom Dreams",
            genre = "Lofi",
            duration = 423,
            artUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
        ),
        Track(
            id = "track-3",
            title = "Astra Nebula",
            artist = "Cosmic Pulse",
            album = "Stardust Odyssey",
            genre = "EDM",
            duration = 345,
            artUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
        ),
        Track(
            id = "track-4",
            title = "Flow State Algorithm",
            artist = "Binary Bloom",
            album = "Code & Harmony",
            genre = "Coding Flow",
            duration = 302,
            artUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
        ),
        Track(
            id = "track-5",
            title = "Ethereal Echoes",
            artist = "Solaris Wave",
            album = "Zero Horizon",
            genre = "Ambient",
            duration = 310,
            artUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
        ),
        Track(
            id = "track-6",
            title = "Cyberpunk Horizon",
            artist = "Neon Nexus",
            album = "Future Tokyo",
            genre = "Synthwave",
            duration = 360,
            artUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3"
        ),
        Track(
            id = "track-7",
            title = "Retro Groove Odyssey",
            artist = "Kazi Nehal",
            album = "Vibra Master Studio",
            genre = "EDM",
            duration = 340,
            artUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3"
        ),
        Track(
            id = "track-8",
            title = "Cosmic Lofi Reverie",
            artist = "Astral Beats",
            album = "Dark City Nights",
            genre = "Lofi",
            duration = 380,
            artUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?auto=format&fit=crop&w=600&q=80",
            audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
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

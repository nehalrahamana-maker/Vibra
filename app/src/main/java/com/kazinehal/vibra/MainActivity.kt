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
import com.kazinehal.vibra.data.MusicRepository
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

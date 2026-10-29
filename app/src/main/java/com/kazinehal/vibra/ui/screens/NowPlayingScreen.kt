package com.kazinehal.vibra.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kazinehal.vibra.data.LyricsSource
import com.kazinehal.vibra.data.UserLibraryManager
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.theme.RivaRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NowPlayingScreen(
    player: VibraPlayer,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = player.currentTrack ?: return
    var isLiked by remember(track.id) { mutableStateOf(UserLibraryManager.isLiked(track.id)) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showLyrics by remember(track.id) { mutableStateOf(false) }
    var lyricsText by remember(track.id) { mutableStateOf(track.lyrics) }
    var lyricsLoading by remember(track.id) { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }

    // Lazy LRCLIB fetch the first time the lyrics panel is opened for this track
    LaunchedEffect(track.id, showLyrics) {
        if (showLyrics && lyricsText.isBlank()) {
            lyricsLoading = true
            lyricsText = withContext(Dispatchers.IO) {
                LyricsSource.fetch(track.title, track.artist, player.durationMs / 1000) ?: ""
            }
            lyricsLoading = false
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Dynamic Blurred Glow Backdrop
        AsyncImage(
            model = track.artUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Dark Scrim Overlay for pure AMOLED contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.94f),
                            Color.Black
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM YOUTUBE MUSIC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = RivaRed
                    )
                    Text(
                        text = track.album,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showLyrics = !showLyrics }) {
                        Icon(
                            imageVector = Icons.Filled.Lyrics,
                            contentDescription = "Lyrics",
                            tint = if (showLyrics) RivaRed else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = { showQueueSheet = !showQueueSheet }) {
                        Icon(
                            imageVector = Icons.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = if (showQueueSheet) RivaRed else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Album Artwork
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
            ) {
                AsyncImage(
                    model = track.artUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title, Artist, and Real Working Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        fontSize = 15.sp,
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = {
                    isLiked = UserLibraryManager.toggleLike(track)
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isLiked) RivaRed else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Slider
            val progress = if (player.durationMs > 0) {
                (player.currentPositionMs.toFloat() / player.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Slider(
                value = progress,
                onValueChange = { newPct ->
                    player.seekTo((newPct * player.durationMs).toLong())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = RivaRed,
                    activeTrackColor = RivaRed,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            // Timestamps: mm:ss / mm:ss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(player.currentPositionMs),
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
                Text(
                    text = formatMs(player.durationMs),
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Main Playback Controls: Shuffle, Previous, Play/Pause, Next, Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { player.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (player.isShuffleOn) RivaRed else Color(0xFF9E9E9E),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { player.previous() }) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Large Circle Play/Pause Button with Loading state
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { player.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    if (player.isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (player.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                IconButton(onClick = { player.next() }) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                IconButton(onClick = { player.toggleRepeat() }) {
                    Icon(
                        imageVector = Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = if (player.isRepeatOn) RivaRed else Color(0xFF9E9E9E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep timer chip
            OutlinedButton(
                onClick = { showSleepDialog = true },
                border = BorderStroke(
                    1.dp,
                    if (player.sleepTimerMinutes > 0) RivaRed else Color.White.copy(alpha = 0.25f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = if (player.sleepTimerMinutes > 0) RivaRed else Color(0xFFB3B3B3),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (player.sleepTimerMinutes > 0) "Sleep ${player.sleepTimerMinutes}m" else "Sleep timer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (player.sleepTimerMinutes > 0) RivaRed else Color(0xFFB3B3B3)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Lyrics card (LRCLIB — fetched on first open of the lyrics panel)
            if (showLyrics) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141416))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "LYRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = RivaRed
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    when {
                        lyricsLoading -> CircularProgressIndicator(
                            color = RivaRed,
                            strokeWidth = 3.dp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        lyricsText.isBlank() -> Text(
                            text = "Lyrics not found for this track.",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E)
                        )

                        else -> Text(
                            text = lyricsText,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Queue Preview or Track Info Card
            if (showQueueSheet) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141416))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Upcoming in Queue (${player.queue.size} tracks)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    player.queue.take(5).forEach { qTrack ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { player.playTrack(qTrack) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = qTrack.artUrl,
                                contentDescription = qTrack.title,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = qTrack.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (qTrack.id == track.id) RivaRed else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = qTrack.artist,
                                    fontSize = 11.sp,
                                    color = Color(0xFF9E9E9E),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Sleep timer picker
        if (showSleepDialog) {
            AlertDialog(
                onDismissRequest = { showSleepDialog = false },
                containerColor = Color(0xFF141416),
                title = { Text("Sleep Timer", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        listOf(0, 15, 30, 45, 60).forEach { mins ->
                            val label = if (mins == 0) "Off" else "$mins minutes"
                            val selected = mins == player.sleepTimerMinutes
                            Text(
                                text = label,
                                fontSize = 16.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) RivaRed else Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        player.setSleepTimer(mins)
                                        showSleepDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSleepDialog = false }) {
                        Text("Close", color = RivaRed)
                    }
                }
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

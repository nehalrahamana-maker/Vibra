package com.kazinehal.vibra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kazinehal.vibra.data.UserLibraryManager
import com.kazinehal.vibra.model.Track
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.theme.RivaRed
import com.kazinehal.vibra.ui.theme.TextMuted
import com.kazinehal.vibra.ui.theme.TextPrimary
import com.kazinehal.vibra.ui.theme.VibraBackground

@Composable
fun LibraryScreen(
    player: VibraPlayer,
    tracks: List<Track>,
    onTrackSelect: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubtab by remember { mutableStateOf("Favorites") }
    val subtabs = listOf("Favorites", "History", "Top Hits")

    val likedTracks = remember(player.currentTrack, selectedSubtab) {
        UserLibraryManager.getLikedTracks()
    }
    val historyTracks = remember(player.currentTrack, selectedSubtab) {
        UserLibraryManager.getHistoryTracks()
    }

    val displayTracks = when (selectedSubtab) {
        "Favorites" -> likedTracks
        "History" -> historyTracks
        else -> tracks
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VibraBackground)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 120.dp)
    ) {
        // Title & Header
        item {
            Text(
                text = "Your Library",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Subtabs Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subtabs) { tab ->
                    val isSelected = selectedSubtab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RivaRed else Color(0xFF1E1E1E))
                            .clickable { selectedSubtab = tab }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Header info & Play All button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${displayTracks.size} tracks",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                if (displayTracks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(RivaRed)
                            .clickable {
                                player.queue = displayTracks.toMutableList()
                                player.playTrack(displayTracks.first())
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play All",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Play All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Empty state
        if (displayTracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedSubtab == "Favorites") Icons.Filled.FavoriteBorder else Icons.Filled.History,
                            contentDescription = null,
                            tint = Color(0xFF555555),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedSubtab == "Favorites") "No liked tracks yet" else "No listening history yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF888888)
                        )
                        Text(
                            text = "Tap the heart icon on any song to add it here",
                            fontSize = 12.sp,
                            color = Color(0xFF555555)
                        )
                    }
                }
            }
        } else {
            // Track List
            items(displayTracks) { track ->
                val isCurrentPlaying = player.currentTrack?.id == track.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrentPlaying) Color(0xFF1A1A1A) else Color.Transparent)
                        .clickable { onTrackSelect(track) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = track.artUrl,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = if (isCurrentPlaying) RivaRed else TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist} • ${track.album}",
                            color = TextMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isCurrentPlaying && player.isPlaying) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = "Playing",
                            tint = RivaRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

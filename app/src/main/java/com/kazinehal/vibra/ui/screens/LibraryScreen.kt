package com.kazinehal.vibra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kazinehal.vibra.model.Track
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.theme.*

data class LibTileData(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun LibraryScreen(
    player: VibraPlayer,
    tracks: List<Track>,
    onTrackSelect: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubtab by remember { mutableStateOf("Your library") }
    val subtabs = listOf("Your library", "Riva Charts", "Your Playlists", "Downloaded")

    val tiles = listOf(
        LibTileData("Favorite", Icons.Filled.Favorite, TileFavorite),
        LibTileData("Followed", Icons.Filled.Person, TileFollowed),
        LibTileData("Most Played", Icons.Filled.TrendingUp, TileMostPlayed),
        LibTileData("Downloaded", Icons.Filled.Download, TileDownloaded)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VibraBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp)
    ) {
        // Title & Header
        item {
            Text(
                text = "Library",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Subtabs Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subtabs) { tab ->
                    val isSelected = selectedSubtab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) VibraAccentPrimary else VibraSurface)
                            .clickable { selectedSubtab = tab }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4 Big Colorful Action Tiles (Exact 2x2 Grid from Video)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TileCard(tile = tiles[0], modifier = Modifier.weight(1f)) {
                        if (tracks.isNotEmpty()) onTrackSelect(tracks.first())
                    }
                    TileCard(tile = tiles[1], modifier = Modifier.weight(1f)) {
                        if (tracks.size > 1) onTrackSelect(tracks[1])
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TileCard(tile = tiles[2], modifier = Modifier.weight(1f)) {
                        if (tracks.size > 2) onTrackSelect(tracks[2])
                    }
                    TileCard(tile = tiles[3], modifier = Modifier.weight(1f)) {
                        if (tracks.size > 3) onTrackSelect(tracks[3])
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Recently Added",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Track List
        items(tracks) { track ->
            val isCurrentPlaying = player.currentTrack?.id == track.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCurrentPlaying) VibraSurfaceVariant else Color.Transparent)
                    .clickable { onTrackSelect(track) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = track.artUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = if (isCurrentPlaying) VibraAccentPrimary else TextPrimary,
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

                IconButton(onClick = { onTrackSelect(track) }) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isCurrentPlaying) VibraAccentPrimary else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun TileCard(
    tile: LibTileData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(tile.color)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tile.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Icon(
                imageVector = tile.icon,
                contentDescription = tile.title,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

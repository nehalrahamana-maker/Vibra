package com.kazinehal.vibra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kazinehal.vibra.model.Track
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    player: VibraPlayer,
    tracks: List<Track>,
    onTrackSelect: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }
    val genres = listOf("All", "Synthwave", "Lofi", "EDM", "Coding Flow", "Ambient")

    val filteredTracks = remember(searchQuery, selectedGenre, tracks) {
        tracks.filter { track ->
            val matchesQuery = track.title.contains(searchQuery, ignoreCase = true) ||
                    track.artist.contains(searchQuery, ignoreCase = true)
            val matchesGenre = selectedGenre == "All" || track.genre.equals(selectedGenre, ignoreCase = true)
            matchesQuery && matchesGenre
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VibraBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // App Header Brand
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vibra 🎵",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Native Jetpack Compose Audiophile",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = VibraAccentPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search songs, artists, or genres...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibraAccentPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedContainerColor = VibraSurface,
                    unfocusedContainerColor = VibraSurface
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres) { genre ->
                    val isSelected = selectedGenre == genre
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGenre = genre },
                        label = { Text(genre, color = if (isSelected) Color.White else TextSecondary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VibraAccentPrimary,
                            containerColor = VibraSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f),
                            selectedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Hero Featured Card
        if (tracks.isNotEmpty()) {
            item {
                val hero = tracks.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onTrackSelect(hero) }
                ) {
                    AsyncImage(
                        model = hero.artUrl,
                        contentDescription = hero.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "FEATURED MASTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibraAccentSecondary
                        )
                        Text(
                            text = hero.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = hero.artist,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Quick Picks Section Title
        item {
            Text(
                text = "Quick Picks & Tracks",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Track List Items
        items(filteredTracks) { track ->
            val isCurrentPlaying = player.currentTrack?.id == track.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
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
                        .size(54.dp)
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
                        text = "${track.artist} • ${track.genre}",
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

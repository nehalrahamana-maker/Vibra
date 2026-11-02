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

@Composable
fun HomeScreen(
    player: VibraPlayer,
    tracks: List<Track>,
    onTrackSelect: (Track) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Bollywood", "Punjabi", "Global Pop", "Rock/Pop", "Chill")

    val historyTracks = remember(player.currentTrack) {
        UserLibraryManager.getHistoryTracks()
    }

    val filteredTracks = remember(selectedCategory, tracks) {
        when (selectedCategory) {
            "Bollywood" -> tracks.filter { it.genre.equals("Bollywood", true) }
            "Punjabi" -> tracks.filter { it.genre.equals("Punjabi", true) }
            "Global Pop" -> tracks.filter { it.genre.equals("Global Pop", true) }
            "Rock/Pop" -> tracks.filter { it.genre.equals("Rock/Pop", true) }
            "Chill" -> tracks.filter { it.genre.equals("Chill", true) }
            else -> tracks
        }.ifEmpty { tracks }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 120.dp)
    ) {
        // TOP HEADER: Riva Red Branding & Real Functional Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Riva",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = RivaRed,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "YouTube Music Engine",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF888888)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Search Shortcut
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF161616))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Real Settings Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF161616))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // CATEGORY PILLS ROW
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RivaRed else Color(0xFF1E1E1E))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // RECENTLY PLAYED SECTION (Real working user history)
        if (historyTracks.isNotEmpty()) {
            item {
                Text(
                    text = "Listen again",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(historyTracks.take(8)) { track ->
                        Column(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { onTrackSelect(track) }
                        ) {
                            AsyncImage(
                                model = track.artUrl,
                                contentDescription = track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = track.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                fontSize = 11.sp,
                                color = Color(0xFF9E9E9E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // SECTION: Trending on YouTube Music
        item {
            Text(
                text = "Trending on YouTube Music",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(filteredTracks) { track ->
            val isCurrent = player.currentTrack?.id == track.id
            var isLiked by remember(track.id) { mutableStateOf(UserLibraryManager.isLiked(track.id)) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCurrent) Color(0xFF1A1A1A) else Color.Transparent)
                    .clickable { onTrackSelect(track) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High-res YouTube thumbnail
                AsyncImage(
                    model = track.artUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Title & Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCurrent) RivaRed else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${track.artist} • ${track.album}",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Working Heart (Favorite) button
                IconButton(
                    onClick = {
                        isLiked = UserLibraryManager.toggleLike(track)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isLiked) RivaRed else Color(0xFF777777),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play icon indicator
                if (isCurrent && player.isPlaying) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = "Playing",
                        tint = RivaRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

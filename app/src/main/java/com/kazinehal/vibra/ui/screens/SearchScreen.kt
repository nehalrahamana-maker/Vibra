package com.kazinehal.vibra.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kazinehal.vibra.data.MusicRepository
import com.kazinehal.vibra.model.Track
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.theme.*
import kotlinx.coroutines.delay

data class GenreCardData(val title: String, val gradient: Brush)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    player: VibraPlayer,
    tracks: List<Track>,
    onTrackSelect: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Genre Cards from Video Frame 00:53
    val genreCards = listOf(
        GenreCardData("African", Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFFB923C)))),
        GenreCardData("Party", Brush.linearGradient(listOf(Color(0xFF84CC16), Color(0xFFFDE047)))),
        GenreCardData("Classical", Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF38BDF8)))),
        GenreCardData("Hindi", Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E)))),
        GenreCardData("Desi hip-hop", Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF6366F1)))),
        GenreCardData("Haryanvi", Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFA855F7))))
    )

    // Moods & Moments from Video Frame 00:53
    val moodCards = listOf(
        GenreCardData("Chill", Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
        GenreCardData("Commute", Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFF97316)))),
        GenreCardData("Energize", Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFD946EF)))),
        GenreCardData("Feel good", Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF10B981))))
    )

    // Live debounced online search
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            isSearching = true
            delay(300)
            try {
                val results = MusicRepository.searchOnline(q)
                searchResults = results
            } catch (e: Exception) {
                searchResults = tracks.filter {
                    it.title.contains(q, ignoreCase = true) || it.artist.contains(q, ignoreCase = true)
                }
            } finally {
                isSearching = false
            }
        } else {
            searchResults = emptyList()
            isSearching = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // Search Bar (Rounded pill shape from Video Frame 00:53)
        item {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = {
                    Text(
                        text = "Search for song...",
                        color = Color(0xFF9E9E9E),
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9E9E9E)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF9E9E9E)
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // If searching: Show results or spinner
        if (searchQuery.isNotBlank()) {
            if (isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(32.dp))
                    }
                }
            } else if (searchResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tracks found for \"$searchQuery\"",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            } else {
                items(searchResults) { track ->
                    val isCurrent = player.currentTrack?.id == track.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) Color(0xFF1E1E1E) else Color.Transparent)
                            .clickable { onTrackSelect(track) }
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = track.artUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCurrent) Color(0xFF38BDF8) else Color.White,
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
                        IconButton(onClick = { Toast.makeText(context, track.title, Toast.LENGTH_SHORT).show() }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // DEFAULT VIEW (Video Frame 00:53)
            item {
                Text(
                    text = "Everything you need",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Search for songs, artists, albums, playlists, and more",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Yellow Chart Pill Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2B2516))
                        .clickable { Toast.makeText(context, "Opening Riva Chart", Toast.LENGTH_SHORT).show() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦  Introducing Riva Chart",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFDE047)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2-Column Genre Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0 until genreCards.size step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GenreTile(
                                card = genreCards[i],
                                modifier = Modifier.weight(1f)
                            ) {
                                searchQuery = genreCards[i].title
                            }
                            if (i + 1 < genreCards.size) {
                                GenreTile(
                                    card = genreCards[i + 1],
                                    modifier = Modifier.weight(1f)
                                ) {
                                    searchQuery = genreCards[i + 1].title
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Moods & Moments Section
            item {
                Text(
                    text = "Moods & moments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0 until moodCards.size step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GenreTile(
                                card = moodCards[i],
                                modifier = Modifier.weight(1f)
                            ) {
                                searchQuery = moodCards[i].title
                            }
                            if (i + 1 < moodCards.size) {
                                GenreTile(
                                    card = moodCards[i + 1],
                                    modifier = Modifier.weight(1f)
                                ) {
                                    searchQuery = moodCards[i + 1].title
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreTile(
    card: GenreCardData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(card.gradient)
            .clickable { onClick() }
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = card.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Filled.NorthEast,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

package com.kazinehal.vibra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val genreCards = listOf(
        GenreCardData("Family", Brush.linearGradient(listOf(Color(0xFF84CC16), Color(0xFFA3E635)))),
        GenreCardData("1990s", Brush.linearGradient(listOf(Color(0xFFEAB308), Color(0xFFFDE047)))),
        GenreCardData("Chill", Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFFB923C)))),
        GenreCardData("Gaming", Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFF97316)))),
        GenreCardData("R&B & soul", Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFFC084FC)))),
        GenreCardData("Jazz", Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF14B8A6))))
    )

    val moods = listOf("Chill", "Commute", "Energize", "Feel good", "Workout", "Party")
    val recentQueries = listOf("Arijit Singh", "Diljit Dosanjh", "Starboy", "Kesariya", "The Weeknd")

    // Debounced Live Search
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            isSearching = true
            delay(350)
            try {
                searchResults = MusicRepository.searchOnline(q)
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
            .background(VibraBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // Search Pill Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for song, artist, album...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VibraAccentPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedContainerColor = VibraSurface,
                    unfocusedContainerColor = VibraSurface
                )
            )

            if (isSearching) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = VibraAccentPrimary,
                    trackColor = VibraSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // When User is Searching: Display Live Search Results
        if (searchQuery.isNotEmpty()) {
            item {
                Text(
                    text = "Search Results (${searchResults.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(searchResults) { track ->
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
        } else {
            // When Not Searching: SimpMusic Explore Landing Page
            item {
                // Everything you need Promo Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2E0854), Color(0xFF1E1035))
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Text(
                            text = "Everything you need",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search for songs, artists, albums, playlists, and more",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✨ Introducing Vibra Studio Chart",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2-Column Colorful Genre Cards
                Text(
                    text = "Explore Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2-column genre grid items
            items(genreCards.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { card ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(card.gradient)
                                .clickable { searchQuery = card.title }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = card.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Moods & Moments
                Text(
                    text = "Moods & moments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(moods) { mood ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(VibraSurface)
                                .clickable { searchQuery = mood }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = mood,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Recent Searches
                Text(
                    text = "Recent Searches",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentQueries) { query ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(VibraSurfaceVariant)
                                .clickable { searchQuery = query }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = query,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

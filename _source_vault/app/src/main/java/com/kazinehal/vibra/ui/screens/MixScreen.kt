package com.kazinehal.vibra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazinehal.vibra.model.Track
import com.kazinehal.vibra.player.VibraPlayer
import com.kazinehal.vibra.ui.theme.TextMuted
import com.kazinehal.vibra.ui.theme.TextPrimary
import com.kazinehal.vibra.ui.theme.VibraBackground
import com.kazinehal.vibra.ui.theme.VibraSurface

data class MixItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: Brush,
    val genreFilter: String
)

@Composable
fun MixScreen(
    player: VibraPlayer,
    tracks: List<Track>,
    onTrackSelect: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val mixItems = listOf(
        MixItem(
            title = "My Supermix",
            subtitle = "Daily curated blend",
            icon = Icons.Filled.AutoAwesome,
            gradient = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))),
            genreFilter = "All"
        ),
        MixItem(
            title = "My Mix 1",
            subtitle = "Bollywood & Romantic",
            icon = Icons.Filled.Favorite,
            gradient = Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFFB7185))),
            genreFilter = "Bollywood"
        ),
        MixItem(
            title = "My Mix 2",
            subtitle = "Pop & Global Hits",
            icon = Icons.Filled.Headphones,
            gradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))),
            genreFilter = "Pop"
        ),
        MixItem(
            title = "Discover Mix",
            subtitle = "Hip-Hop & Punjabi",
            icon = Icons.Filled.Explore,
            gradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF34D399))),
            genreFilter = "Hip-Hop"
        ),
        MixItem(
            title = "Feel Good Mix",
            subtitle = "Upbeat energy boost",
            icon = Icons.Filled.SentimentVerySatisfied,
            gradient = Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF38BDF8))),
            genreFilter = "R&B"
        ),
        MixItem(
            title = "Worldwide Beats",
            subtitle = "Top charts & virals",
            icon = Icons.Filled.Public,
            gradient = Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E))),
            genreFilter = "Worldwide"
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(VibraBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(
                    text = "Mix for you",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Endless radio streams and auto-mixes from your music taste",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Liked Music Big Special Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                            )
                        )
                        .clickable {
                            val liked = com.kazinehal.vibra.data.UserLibraryManager.getLikedTracks()
                            if (liked.isNotEmpty()) {
                                onTrackSelect(liked.first())
                            } else if (tracks.isNotEmpty()) {
                                onTrackSelect(tracks.first())
                            }
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = "Liked Music",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Liked Music",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Auto playlist • Your favorites",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Personalized Stations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        items(mixItems) { mix ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VibraSurface)
                    .clickable {
                        val matching = tracks.filter {
                            mix.genreFilter == "All" || it.genre.contains(mix.genreFilter, ignoreCase = true)
                        }
                        if (matching.isNotEmpty()) {
                            onTrackSelect(matching.first())
                        } else if (tracks.isNotEmpty()) {
                            onTrackSelect(tracks.first())
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(mix.gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = mix.icon,
                        contentDescription = mix.title,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(42.dp)
                    )
                }
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = mix.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mix.subtitle,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

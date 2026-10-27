package com.kazinehal.vibra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kazinehal.vibra.ui.theme.*

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    var ytPlaylistUrl by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf("") }
    var isSpotifyConnected by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141416))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings & Sync",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Music Import & Account Sync (Spotify / YouTube Music)
                Text(
                    text = "PLAYLIST IMPORT & ACCOUNT SYNC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VibraAccentPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // YouTube Music Playlist Import
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VibraSurface)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Import YouTube Music Playlist",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = ytPlaylistUrl,
                        onValueChange = { ytPlaylistUrl = it },
                        placeholder = { Text("Paste YouTube Music playlist URL...", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibraAccentPrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF1A1A1C),
                            unfocusedContainerColor = Color(0xFF1A1A1C)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (ytPlaylistUrl.isNotBlank()) {
                                importStatusMessage = "Syncing playlist tracks into your library..."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VibraAccentPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Import & Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Spotify Account Connect / Sync
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VibraSurface)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spotify Account Sync",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isSpotifyConnected) "Connected as Kazi Nehal • Playlists synced" else "Sync Spotify Liked Songs & Playlists",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Button(
                        onClick = { isSpotifyConnected = !isSpotifyConnected },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpotifyConnected) Color(0xFF1DB954) else Color(0xFF262628)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isSpotifyConnected) "Synced ✓" else "Login",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                if (importStatusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ $importStatusMessage",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Audio & Playback Settings
                Text(
                    text = "AUDIO QUALITY & DSP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VibraAccentPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VibraSurface)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Streaming Quality", fontSize = 14.sp, color = TextPrimary)
                        Text("Audiophile 320kbps", fontSize = 12.sp, color = VibraAccentPrimary, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No-Ads Mode", fontSize = 14.sp, color = TextPrimary)
                        Text("Active (100% Free)", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gapless Playback", fontSize = 14.sp, color = TextPrimary)
                        Text("Enabled", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 3: About Author (Kazi Nehal, Oct 26 2026, v2.0.0)
                Text(
                    text = "ABOUT VIBRA & AUTHOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VibraAccentPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1A1A24), Color(0xFF121218))
                            )
                        )
                        .border(1.dp, VibraAccentPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VibraAccentPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("KN", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Kazi Nehal",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Lead Developer & Creator",
                                fontSize = 12.sp,
                                color = VibraAccentPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("App Name", fontSize = 12.sp, color = TextMuted)
                        Text("Vibra (SimpMusic Core)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Release Version", fontSize = 12.sp, color = TextMuted)
                        Text("v2.0.0 (Release)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Creation Date", fontSize = 12.sp, color = TextMuted)
                        Text("October 26, 2026", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("License", fontSize = 12.sp, color = TextMuted)
                        Text("Free & Open Source • No Ads", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

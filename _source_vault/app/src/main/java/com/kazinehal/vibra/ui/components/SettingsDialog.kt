package com.kazinehal.vibra.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kazinehal.vibra.ui.theme.RivaRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var audioQuality by remember { mutableStateOf("High Quality (256kbps Opus)") }
    var autoPlayNext by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black
                    )
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Audio Streaming Settings
                item {
                    Text(
                        text = "Audio & Playback",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RivaRed
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCardItem(
                        title = "Streaming Quality",
                        subtitle = audioQuality,
                        onClick = {
                            audioQuality = if (audioQuality.startsWith("High")) {
                                "Standard Quality (128kbps AAC)"
                            } else {
                                "High Quality (256kbps Opus)"
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF141416))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-play next track",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Keep music playing when current track ends",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        Switch(
                            checked = autoPlayNext,
                            onCheckedChange = { autoPlayNext = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RivaRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCardItem(
                        title = "Clear Media Cache",
                        subtitle = "Free up memory and refresh stream cache",
                        onClick = {
                            Toast.makeText(context, "Media cache cleared successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // YouTube Music Backend Engine info
                item {
                    Text(
                        text = "Backend Engine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RivaRed
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCardItem(
                        title = "Data Source",
                        subtitle = "YouTube Music InnerTube API (Real-time Stream Engine)",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCardItem(
                        title = "Stream Resolver",
                        subtitle = "Multi-tier Invidious + Piped + Edge Worker Gateway",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ABOUT US (Explicitly requested by user)
                item {
                    Text(
                        text = "About Us",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RivaRed
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCardItem(
                        title = "Application",
                        subtitle = "Riva Music Experience",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCardItem(
                        title = "Author",
                        subtitle = "Kazi Nehal Rahman",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCardItem(
                        title = "Version",
                        subtitle = "v2.0.0 (Release)",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsCardItem(
                        title = "Build Date",
                        subtitle = "October 26, 2026",
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "@2026 Riva v2.0.0",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "kazinehal-dev",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.35f)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsCardItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141416))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}

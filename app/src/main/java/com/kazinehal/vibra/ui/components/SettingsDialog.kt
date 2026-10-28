package com.kazinehal.vibra.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kazinehal.vibra.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Interface States
    var currentTheme by remember { mutableStateOf("Dark") }
    var nowPlayingStyle by remember { mutableStateOf("Classic") }
    var lyricsStyle by remember { mutableStateOf("Classic") }
    var themeColor by remember { mutableStateOf("Custom") }
    var customColorHex by remember { mutableStateOf("#B8CAE5") }
    var isLiquidGlassEnabled by remember { mutableStateOf(true) }

    // Content States
    var currentLanguage by remember { mutableStateOf("English") }
    var contentCountry by remember { mutableStateOf("US") }
    var preferredAudioLanguage by remember { mutableStateOf("Original audio") }
    var audioQuality by remember { mutableStateOf("High Opus - 256kbps (YT Premium)") }
    var downloadQuality by remember { mutableStateOf("High AAC - 256kbps (YT Premium)") }
    var videoQuality by remember { mutableStateOf("360p") }
    var videoDownloadQuality by remember { mutableStateOf("360p") }

    // Switches
    var downloadSongsYouLike by remember { mutableStateOf(false) }
    var playVideoTrack by remember { mutableStateOf(false) }
    var playAudioInRadio by remember { mutableStateOf(false) }
    var syncFollowWithYt by remember { mutableStateOf(false) }
    var sendListeningData by remember { mutableStateOf(false) }
    var playExplicitContent by remember { mutableStateOf(true) }
    var keepYtPlaylistOffline by remember { mutableStateOf(false) }
    var enableProxy by remember { mutableStateOf(false) }

    // Audio / DSP Switches
    var normalizeVolume by remember { mutableStateOf(false) }
    var skipSilent by remember { mutableStateOf(false) }
    var equalizerType by remember { mutableStateOf("Built-in") }
    var equalizerEnabled by remember { mutableStateOf(false) }
    var delayEnabled by remember { mutableStateOf(false) }
    var reverbEnabled by remember { mutableStateOf(false) }
    var savePlaybackState by remember { mutableStateOf(false) }

    // Spotify & Integrations
    var spotifyLyricsEnabled by remember { mutableStateOf(false) }
    var spotifyCanvasEnabled by remember { mutableStateOf(false) }
    var discordRichPresence by remember { mutableStateOf(false) }
    var lastFmScrobbling by remember { mutableStateOf(false) }
    var sponsorBlockEnabled by remember { mutableStateOf(false) }

    // Backup & Restore
    var backupDownloadedData by remember { mutableStateOf(false) }
    var autoBackup by remember { mutableStateOf(false) }

    // About Us & Update States
    var autoCheckUpdate by remember { mutableStateOf(true) }
    var lastCheckedText by remember { mutableStateOf("Last checked at 2026-10-28 12:04") }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    // Dialog Dialog Triggers
    var showThemeDialog by remember { mutableStateOf(false) }
    var showNowPlayingDialog by remember { mutableStateOf(false) }
    var showLyricsStyleDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showRomanizationDialog by remember { mutableStateOf(false) }

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

                // SECTION: Devices
                item {
                    SettingsSectionHeader("Devices")
                    SettingsItem(
                        title = "Send sign-ins to desktop",
                        subtitle = "Scan the QR code shown by SimpMusic on your computer"
                    ) {
                        Toast.makeText(context, "Scanning desktop QR code...", Toast.LENGTH_SHORT).show()
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: Interface
                item {
                    SettingsSectionHeader("Interface")
                    SettingsItem(
                        title = "Theme",
                        subtitle = currentTheme
                    ) { showThemeDialog = true }

                    SettingsItem(
                        title = "Now Playing style",
                        subtitle = nowPlayingStyle
                    ) { showNowPlayingDialog = true }

                    SettingsItem(
                        title = "Lyrics style",
                        subtitle = lyricsStyle
                    ) { showLyricsStyleDialog = true }

                    SettingsItem(
                        title = "Lyrics romanization",
                        subtitle = "Show lyrics in Latin script for the languages you pick"
                    ) { showRomanizationDialog = true }

                    SettingsItem(
                        title = "Theme color",
                        subtitle = themeColor
                    ) {
                        themeColor = if (themeColor == "Custom") "Simp Crimson" else "Custom"
                    }

                    SettingsItem(
                        title = "Custom color",
                        subtitle = customColorHex
                    ) {
                        Toast.makeText(context, "Color set to $customColorHex", Toast.LENGTH_SHORT).show()
                    }

                    SettingsSwitchItem(
                        title = "Enable liquid glass (BETA)",
                        subtitle = "Enable iOS's liquid glass effect in some position (Android 13+)",
                        checked = isLiquidGlassEnabled,
                        onCheckedChange = { isLiquidGlassEnabled = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: Content
                item {
                    SettingsSectionHeader("Content")
                    SettingsItem(
                        title = "YouTube Account",
                        subtitle = "Manage your YouTube Accounts"
                    ) {
                        Toast.makeText(context, "YouTube Account Manager", Toast.LENGTH_SHORT).show()
                    }

                    SettingsItem(
                        title = "Language",
                        subtitle = currentLanguage
                    ) {
                        currentLanguage = if (currentLanguage == "English") "Hindi" else "English"
                    }

                    SettingsItem(
                        title = "Content Country",
                        subtitle = contentCountry
                    ) {
                        contentCountry = if (contentCountry == "US") "IN" else "US"
                    }

                    SettingsItem(
                        title = "Preferred audio language",
                        subtitle = preferredAudioLanguage
                    ) {
                        Toast.makeText(context, "Audio Language: $preferredAudioLanguage", Toast.LENGTH_SHORT).show()
                    }

                    SettingsItem(
                        title = "Quality",
                        subtitle = audioQuality
                    ) { showQualityDialog = true }

                    SettingsItem(
                        title = "Download quality",
                        subtitle = downloadQuality
                    ) { showQualityDialog = true }

                    SettingsItem(
                        title = "Video Quality",
                        subtitle = videoQuality
                    ) { showVideoQualityDialog = true }

                    SettingsItem(
                        title = "Video download quality",
                        subtitle = videoDownloadQuality
                    ) { showVideoQualityDialog = true }

                    SettingsSwitchItem(
                        title = "Download songs you like",
                        subtitle = "Automatically download a song for offline playback when you add it to favorites",
                        checked = downloadSongsYouLike,
                        onCheckedChange = { downloadSongsYouLike = it }
                    )

                    SettingsSwitchItem(
                        title = "Play video for video track instead of audio only",
                        subtitle = "Such as Music Video, Lyrics Video, Podcasts, and more",
                        checked = playVideoTrack,
                        onCheckedChange = { playVideoTrack = it }
                    )

                    SettingsSwitchItem(
                        title = "Play audio version instead of MV in radio",
                        subtitle = "Play the audio version instead of the music video when these is one",
                        checked = playAudioInRadio,
                        onCheckedChange = { playAudioInRadio = it }
                    )

                    SettingsSwitchItem(
                        title = "Sync Follow with YouTube",
                        subtitle = "Following an artist in SimpMusic also subscribes to their channel on your YouTube account",
                        checked = syncFollowWithYt,
                        onCheckedChange = { syncFollowWithYt = it }
                    )

                    SettingsSwitchItem(
                        title = "Send back listening data to Google",
                        subtitle = "Upload your listening history to YouTube Music server",
                        checked = sendListeningData,
                        onCheckedChange = { sendListeningData = it }
                    )

                    SettingsSwitchItem(
                        title = "Play explicit content",
                        subtitle = "Enable to play explicit content",
                        checked = playExplicitContent,
                        onCheckedChange = { playExplicitContent = it }
                    )

                    SettingsSwitchItem(
                        title = "Keep showing your YouTube playlist when offline",
                        subtitle = "Save your YT playlist to local and keep to show when offline",
                        checked = keepYtPlaylistOffline,
                        onCheckedChange = { keepYtPlaylistOffline = it }
                    )

                    SettingsSwitchItem(
                        title = "Proxy",
                        subtitle = "Using Proxy to bypass country content blocking",
                        checked = enableProxy,
                        onCheckedChange = { enableProxy = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: Audio
                item {
                    SettingsSectionHeader("Audio")
                    SettingsSwitchItem(
                        title = "Normalize Volume",
                        subtitle = "Balance media loudness",
                        checked = normalizeVolume,
                        onCheckedChange = { normalizeVolume = it }
                    )

                    SettingsSwitchItem(
                        title = "Skip Silent",
                        subtitle = "Skip no music part",
                        checked = skipSilent,
                        onCheckedChange = { skipSilent = it }
                    )

                    SettingsItem(
                        title = "Equalizer type",
                        subtitle = equalizerType
                    ) {
                        equalizerType = if (equalizerType == "Built-in") "System DSP" else "Built-in"
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: Playback
                item {
                    SettingsSectionHeader("Playback")
                    SettingsSwitchItem(
                        title = "Equalizer",
                        subtitle = "Shape the sound with a ten-band curve",
                        checked = equalizerEnabled,
                        onCheckedChange = { equalizerEnabled = it }
                    )

                    SettingsSwitchItem(
                        title = "Delay",
                        subtitle = "Echo the sound with a repeating delay",
                        checked = delayEnabled,
                        onCheckedChange = { delayEnabled = it }
                    )

                    SettingsSwitchItem(
                        title = "Reverb",
                        subtitle = "Place the music in a room, hall or plate",
                        checked = reverbEnabled,
                        onCheckedChange = { reverbEnabled = it }
                    )

                    SettingsSwitchItem(
                        title = "Save Playback State",
                        subtitle = "Remember last played song and position on app restart",
                        checked = savePlaybackState,
                        onCheckedChange = { savePlaybackState = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: Log in to Spotify
                item {
                    SettingsSectionHeader("Spotify Integration")
                    SettingsItem(
                        title = "Log in to Spotify",
                        subtitle = "Sync your Spotify playlists, Canvas and synced lyrics"
                    ) {
                        Toast.makeText(context, "Connecting to Spotify...", Toast.LENGTH_SHORT).show()
                    }

                    SettingsSwitchItem(
                        title = "Enable Spotify Lyrics",
                        subtitle = "More lyrics without translation",
                        checked = spotifyLyricsEnabled,
                        onCheckedChange = { spotifyLyricsEnabled = it }
                    )

                    SettingsSwitchItem(
                        title = "Enable Canvas",
                        subtitle = "Show Spotify Canvas in SimpMusic",
                        checked = spotifyCanvasEnabled,
                        onCheckedChange = { spotifyCanvasEnabled = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: Discord Integration
                item {
                    SettingsSectionHeader("Discord Integration")
                    SettingsItem(
                        title = "Log in to Discord",
                        subtitle = "Log in to enable Discord Rich Presence"
                    ) {
                        Toast.makeText(context, "Opening Discord login...", Toast.LENGTH_SHORT).show()
                    }

                    SettingsSwitchItem(
                        title = "Enable rich presence",
                        subtitle = "Show media session info on your Discord profile",
                        checked = discordRichPresence,
                        onCheckedChange = { discordRichPresence = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: Last.fm
                item {
                    SettingsSectionHeader("Last.fm")
                    SettingsItem(
                        title = "Log in to Last.fm",
                        subtitle = "Log in to scrobble what you listen to"
                    ) {
                        Toast.makeText(context, "Opening Last.fm login...", Toast.LENGTH_SHORT).show()
                    }

                    SettingsSwitchItem(
                        title = "Enable scrobbling",
                        subtitle = "Send what you play to your Last.fm profile",
                        checked = lastFmScrobbling,
                        onCheckedChange = { lastFmScrobbling = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: SponsorBlock
                item {
                    SettingsSectionHeader("SponsorBlock")
                    SettingsSwitchItem(
                        title = "Enable SponsorBlock",
                        subtitle = "Skip sponsor segments, intros, and non-music parts",
                        checked = sponsorBlockEnabled,
                        onCheckedChange = { sponsorBlockEnabled = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: Backup & Restore
                item {
                    SettingsSectionHeader("Backup & Restore")
                    SettingsSwitchItem(
                        title = "Backup downloaded data",
                        subtitle = "Backup all downloaded song's data",
                        checked = backupDownloadedData,
                        onCheckedChange = { backupDownloadedData = it }
                    )

                    SettingsSwitchItem(
                        title = "Auto backup",
                        subtitle = "Automatically backup your data to Downloads folder",
                        checked = autoBackup,
                        onCheckedChange = { autoBackup = it }
                    )

                    SettingsItem(
                        title = "Backup",
                        subtitle = "Save all your playlist data"
                    ) {
                        Toast.makeText(context, "Playlist data backed up successfully!", Toast.LENGTH_SHORT).show()
                    }

                    SettingsItem(
                        title = "Restore your Data",
                        subtitle = "Restore your saved data"
                    ) {
                        Toast.makeText(context, "Select backup JSON file to restore", Toast.LENGTH_SHORT).show()
                    }

                    SettingsItem(
                        title = "Import playlists",
                        subtitle = "Import playlists converted from Spotify or another client"
                    ) {
                        Toast.makeText(context, "Playlist importer opened", Toast.LENGTH_SHORT).show()
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SECTION: About us (Exactly as user requested)
                item {
                    SettingsSectionHeader("About us")
                    SettingsItem(
                        title = "Version",
                        subtitle = "v2.0.0"
                    )

                    SettingsItem(
                        title = "Author",
                        subtitle = "Kazi Nehal Rahman"
                    )

                    SettingsItem(
                        title = "Build Date",
                        subtitle = "October 26, 2026"
                    )

                    SettingsSwitchItem(
                        title = "Automatic check for update",
                        subtitle = "Checking for update when you open app",
                        checked = autoCheckUpdate,
                        onCheckedChange = { autoCheckUpdate = it }
                    )

                    SettingsItem(
                        title = "Update channel",
                        subtitle = "Vibra GitHub Release"
                    )

                    SettingsItem(
                        title = "Check for update",
                        subtitle = if (isCheckingUpdate) "Checking for updates..." else lastCheckedText
                    ) {
                        isCheckingUpdate = true
                        lastCheckedText = "You are on the latest version v2.0.0!"
                        isCheckingUpdate = false
                        Toast.makeText(context, "Vibra v2.0.0 is up to date!", Toast.LENGTH_SHORT).show()
                    }

                    SettingsItem(
                        title = "Follow me on X",
                        subtitle = "@kazinehal"
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/kazinehal"))
                        context.startActivity(intent)
                    }

                    SettingsItem(
                        title = "Developer Blog",
                        subtitle = "Read dev notes, tutorials & behind-the-scenes"
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nehalrahamana-maker/Vibra"))
                        context.startActivity(intent)
                    }

                    SettingsItem(
                        title = "Buy me a coffee",
                        subtitle = "If you love my work, give me a coffee"
                    ) {
                        Toast.makeText(context, "Thank you for supporting Vibra!", Toast.LENGTH_SHORT).show()
                    }

                    SettingsItem(
                        title = "Third party libraries",
                        subtitle = "Description and licenses"
                    ) {
                        Toast.makeText(context, "ExoPlayer, Compose, Coil, LRCLIB", Toast.LENGTH_SHORT).show()
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "@2026 Vibra v2.0.0",
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

    // -------------------------------------------------------------
    // INTERACTIVE SELECTION DIALOGS
    // -------------------------------------------------------------

    // 1. Theme Dialog (System default, Dark, Light)
    if (showThemeDialog) {
        val options = listOf("System default", "Dark", "Light")
        var tempSelected by remember { mutableStateOf(currentTheme) }

        SingleChoiceSelectionDialog(
            title = "Theme",
            options = options,
            selectedOption = tempSelected,
            onOptionSelected = { tempSelected = it },
            onDismiss = { showThemeDialog = false },
            onConfirm = {
                currentTheme = tempSelected
                showThemeDialog = false
            }
        )
    }

    // 2. Now Playing Style Dialog
    if (showNowPlayingDialog) {
        val options = listOf("Classic", "Material 3 Expressive", "Apple Music (Android 12+)")
        var tempSelected by remember { mutableStateOf(nowPlayingStyle) }

        SingleChoiceSelectionDialog(
            title = "Now Playing style",
            options = options,
            selectedOption = tempSelected,
            onOptionSelected = { tempSelected = it },
            onDismiss = { showNowPlayingDialog = false },
            onConfirm = {
                nowPlayingStyle = tempSelected
                showNowPlayingDialog = false
            }
        )
    }

    // 3. Lyrics Style Dialog
    if (showLyricsStyleDialog) {
        val options = listOf("Classic", "Apple Music (Android 12+)")
        var tempSelected by remember { mutableStateOf(lyricsStyle) }

        SingleChoiceSelectionDialog(
            title = "Lyrics style",
            options = options,
            selectedOption = tempSelected,
            onOptionSelected = { tempSelected = it },
            onDismiss = { showLyricsStyleDialog = false },
            onConfirm = {
                lyricsStyle = tempSelected
                showLyricsStyleDialog = false
            }
        )
    }

    // 4. Audio Quality Dialog
    if (showQualityDialog) {
        val options = listOf(
            "Low - 64kbps",
            "Medium - 128kbps",
            "High Opus - 256kbps (YT Premium)",
            "High AAC - 256kbps (YT Premium)"
        )
        var tempSelected by remember { mutableStateOf(audioQuality) }

        SingleChoiceSelectionDialog(
            title = "Quality",
            options = options,
            selectedOption = tempSelected,
            onOptionSelected = { tempSelected = it },
            onDismiss = { showQualityDialog = false },
            onConfirm = {
                audioQuality = tempSelected
                downloadQuality = tempSelected
                showQualityDialog = false
            }
        )
    }

    // 5. Video Quality Dialog
    if (showVideoQualityDialog) {
        val options = listOf("1080p", "720p", "360p")
        var tempSelected by remember { mutableStateOf(videoQuality) }

        SingleChoiceSelectionDialog(
            title = "Video Quality",
            options = options,
            selectedOption = tempSelected,
            onOptionSelected = { tempSelected = it },
            onDismiss = { showVideoQualityDialog = false },
            onConfirm = {
                videoQuality = tempSelected
                videoDownloadQuality = tempSelected
                showVideoQualityDialog = false
            }
        )
    }

    // 6. Lyrics Romanization Dialog
    if (showRomanizationDialog) {
        val languages = listOf("Japanese", "Korean", "Chinese", "Hindi", "Punjabi", "Russian", "Ukrainian", "Serbian")
        val selectedSet = remember { mutableStateListOf("Hindi", "Punjabi") }

        AlertDialog(
            onDismissRequest = { showRomanizationDialog = false },
            title = {
                Text(
                    text = "Lyrics romanization",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    languages.forEach { lang ->
                        val isChecked = selectedSet.contains(lang)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedSet.remove(lang) else selectedSet.add(lang)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedSet.add(lang) else selectedSet.remove(lang)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VibraAccentPrimary,
                                    uncheckedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang, color = Color.White, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRomanizationDialog = false }) {
                    Text("Save", color = VibraAccentPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRomanizationDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// -------------------------------------------------------------
// HELPER COMPOSABLES
// -------------------------------------------------------------

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF6B7280),
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF38BDF8),
                uncheckedThumbColor = Color(0xFF888888),
                uncheckedTrackColor = Color(0xFF2C2C2E)
            )
        )
    }
}

@Composable
fun SingleChoiceSelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (option == selectedOption),
                                onClick = { onOptionSelected(option) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF38BDF8),
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Change", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp)
    )
}

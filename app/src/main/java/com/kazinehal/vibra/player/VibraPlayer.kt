package com.kazinehal.vibra.player

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.kazinehal.vibra.data.UserLibraryManager
import com.kazinehal.vibra.data.YtMusicSource
import com.kazinehal.vibra.data.YtStreamResolver
import com.kazinehal.vibra.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VibraPlayer private constructor(context: Context) {

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(15000)
        .setAllowCrossProtocolRedirects(true)

    private val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    // handleAudioFocus=true → pause/duck on focus loss (calls, other apps, other players).
    // setHandleAudioBecomingNoisy(true) → pause when headphones unplug / Bluetooth drops.
    internal val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(mediaSourceFactory)
        .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val scope = CoroutineScope(Dispatchers.Main)

    var currentTrack by mutableStateOf<Track?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var currentPositionMs by mutableLongStateOf(0L)
        private set

    var durationMs by mutableLongStateOf(1L)
        private set

    var isShuffleOn by mutableStateOf(false)
    var isRepeatOn by mutableStateOf(false)

    var queue = mutableListOf<Track>()
    private var currentIndex = 0

    /** Remaining minutes of the sleep timer; 0 = off. */
    var sleepTimerMinutes by mutableStateOf(0)
        private set

    private var sleepJob: Job? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) {
                    isLoading = false
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isLoading = true
                    }
                    Player.STATE_READY -> {
                        isLoading = false
                        val dur = exoPlayer.duration
                        if (dur > 1000L) {
                            durationMs = dur
                        }
                    }
                    Player.STATE_ENDED -> {
                        isLoading = false
                        if (isRepeatOn) {
                            seekTo(0)
                            exoPlayer.play()
                        } else {
                            next()
                        }
                    }
                    Player.STATE_IDLE -> {
                        // idle
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("VibraPlayer", "ExoPlayer playback error: ${error.message}", error)
                isLoading = false
                val track = currentTrack ?: return

                // On error, try to re-resolve fresh stream URL from YouTube
                scope.launch {
                    val stream = withContext(Dispatchers.IO) {
                        if (track.videoId.isNotBlank()) {
                            YtStreamResolver.resolve(track.videoId)
                        } else {
                            val vid = YtMusicSource.findVideoId(track.title, track.artist)
                            if (!vid.isNullOrBlank()) YtStreamResolver.resolve(vid) else null
                        }
                    }
                    if (currentTrack?.id == track.id && stream != null && stream.audioUrl.isNotBlank() && stream.audioUrl != track.audioUrl) {
                        if (stream.durationSeconds > 0) {
                            durationMs = stream.durationSeconds * 1000L
                        }
                        startPlayback(stream.audioUrl)
                    }
                }
            }
        })

        // Position ticker
        scope.launch {
            while (true) {
                if (isPlaying) {
                    currentPositionMs = exoPlayer.currentPosition
                }
                delay(250)
            }
        }
    }

    /**
     * Play track using YouTube Music Backend
     */
    fun playTrack(track: Track) {
        currentTrack = track
        durationMs = track.duration * 1000L
        isLoading = true

        // Add to persistent user history
        UserLibraryManager.addToHistory(track)

        scope.launch {
            try {
                // If track already has a valid direct audio URL, play it
                if (track.audioUrl.isNotBlank() && !track.audioUrl.contains("itunes") && !track.audioUrl.contains("mzstatic")) {
                    startPlayback(track.audioUrl)
                    return@launch
                }

                // Resolve videoId if empty
                var videoId = track.videoId
                if (videoId.isBlank()) {
                    val foundVid = withContext(Dispatchers.IO) {
                        YtMusicSource.findVideoId(track.title, track.artist)
                    }
                    videoId = foundVid ?: ""
                }

                // Resolve YouTube stream
                if (videoId.isNotBlank()) {
                    val stream = withContext(Dispatchers.IO) {
                        YtStreamResolver.resolve(videoId)
                    }
                    if (currentTrack?.id == track.id && stream != null && stream.audioUrl.isNotBlank()) {
                        if (stream.durationSeconds > 0) {
                            durationMs = stream.durationSeconds * 1000L
                        }
                        startPlayback(stream.audioUrl)
                        return@launch
                    }
                }

                // Fallback: try cached audioUrl
                if (track.audioUrl.isNotBlank()) {
                    startPlayback(track.audioUrl)
                }
            } catch (e: Exception) {
                android.util.Log.e("VibraPlayer", "playTrack failed: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun startPlayback(url: String) {
        if (url.isBlank()) return
        try {
            val t = currentTrack
            // Metadata rides with the MediaItem → the MediaSession notification shows
            // title/artist/artwork and lockscreen/Android Auto controls get it too.
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t?.title ?: "Riva")
                        .setArtist(t?.artist ?: "")
                        .setAlbumTitle(t?.album?.takeIf { it.isNotBlank() })
                        .setArtworkUri(t?.artUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })
                        .build()
                )
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            android.util.Log.e("VibraPlayer", "startPlayback failed: ${e.message}", e)
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        currentPositionMs = positionMs
    }

    fun toggleShuffle() {
        isShuffleOn = !isShuffleOn
    }

    fun toggleRepeat() {
        isRepeatOn = !isRepeatOn
    }

    /** Arm (minutes > 0) or cancel (0) the sleep timer — pauses playback when it fires. */
    fun setSleepTimer(minutes: Int) {
        sleepJob?.cancel()
        sleepJob = null
        sleepTimerMinutes = minutes.coerceAtLeast(0)
        if (sleepTimerMinutes == 0) return
        sleepJob = scope.launch {
            delay(sleepTimerMinutes * 60_000L)
            exoPlayer.pause()
            sleepTimerMinutes = 0
        }
    }

    fun next() {
        if (queue.isEmpty()) return
        currentIndex = if (isShuffleOn && queue.size > 1) {
            (queue.indices - currentIndex).random()
        } else {
            (currentIndex + 1) % queue.size
        }
        playTrack(queue[currentIndex])
    }

    fun previous() {
        if (currentPositionMs > 3000) {
            seekTo(0)
            return
        }
        if (queue.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) queue.size - 1 else currentIndex - 1
        playTrack(queue[currentIndex])
    }

    fun release() {
        exoPlayer.release()
    }

    companion object {
        @Volatile
        private var instance: VibraPlayer? = null

        /**
         * Process-wide singleton. The MediaSessionService and the UI must share the same
         * ExoPlayer instance — a second player would silently fight the first for audio focus
         * and the notification would control a player nobody is listening to.
         */
        fun get(context: Context): VibraPlayer {
            return instance ?: synchronized(this) {
                instance ?: VibraPlayer(context.applicationContext).also { instance = it }
            }
        }
    }
}

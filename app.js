/**
 * VIBRA AUDIO ENGINE & APPLICATION CORE
 * Authored by: Kazi Nehal (CSE)
 * Next-Gen Web Music Player with Real-Time Web Audio Equalizer,
 * Synced Lyrics (LRCLIB), Invidious YouTube Music Search, and Canvas Visualizer.
 */

// =============================================================================
// 1. CURATED SOUNDSCAPES & SEED TRACKS
// =============================================================================

const CURATED_TRACKS = [
  {
    id: 'track-1',
    title: 'Neon Eclipse',
    artist: 'Vector Drive',
    album: 'Synthetic Horizons',
    genre: 'Synthwave',
    duration: 218,
    art: 'assets/covers/cyberpunk.jpg',
    audioUrl: 'https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3?filename=synthwave-80s-110045.mp3',
    lyrics: `[00:00.00] (Neon synthesizer intro initiates)
[00:08.50] Gliding through the obsidian rain
[00:15.20] Purple lasers pulsing through my veins
[00:23.00] Zero latency in the midnight air
[00:29.80] Neon shadows dancing everywhere
[00:37.40] We run the circuit board
[00:44.20] Unbound by wires, electric Lord
[00:51.90] Feel the sub-bass rumble deep
[00:58.70] Promises the digital heavens keep
[01:06.50] Cybernetic skyline burning bright
[01:13.80] Chasing the pulse of electric night
[01:21.00] (Analog filter sweep & arpeggiator solo)
[01:36.00] Signals reverberate across the sea
[01:43.50] Living inside this frequency
[01:50.00] Neon Eclipse, set us free`
  },
  {
    id: 'track-2',
    title: 'Midnight Rain & Study Tea',
    artist: 'Lofi Future',
    album: 'Bedroom Dreams',
    genre: 'Lofi',
    duration: 174,
    art: 'assets/covers/lofi.jpg',
    audioUrl: 'https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3?filename=lofi-study-112191.mp3',
    lyrics: `[00:00.00] (Vinyl crackle and gentle raindrops on glass)
[00:12.00] Steam rises slow from the porcelain cup
[00:24.00] Late night assignments, never giving up
[00:36.50] Warm acoustic guitar in the gentle breeze
[00:48.00] Mind drifts away with absolute ease
[01:00.00] Soft rhodes piano fills the quiet space
[01:12.50] Raindrops cascading in a gentle race
[01:24.00] Focus unlocked, tension starts to fade
[01:36.00] In the peaceful sanctuary we have made`
  },
  {
    id: 'track-3',
    title: 'Astra Nebula',
    artist: 'Cosmic Pulse',
    album: 'Stardust Odyssey',
    genre: 'EDM',
    duration: 245,
    art: 'assets/covers/cosmic.jpg',
    audioUrl: 'https://cdn.pixabay.com/download/audio/2022/03/15/audio_c8c8a73467.mp3?filename=electronic-future-beats-117997.mp3',
    lyrics: `[00:00.00] (Deep ambient space atmosphere)
[00:14.00] Launching thrusters past the atmosphere
[00:21.50] Leaving gravity and earthly fear
[00:29.00] Stars align across the galactic dome
[00:36.20] Infinite cosmos is our only home
[00:44.00] 3, 2, 1 — Drop into warp speed!
[00:52.00] (Heavy energetic synth bass drops)
[01:08.00] Supernova glowing in our eyes
[01:16.00] Flying through holographic skies
[01:24.00] Astra Nebula, carry us far
[01:32.00] Riding the frequency of a burning star`
  },
  {
    id: 'track-4',
    title: 'Flow State Algorithm',
    artist: 'Binary Bloom',
    album: 'Code & Harmony',
    genre: 'Coding Flow',
    duration: 198,
    art: 'assets/covers/cyberpunk.jpg',
    audioUrl: 'https://cdn.pixabay.com/download/audio/2022/10/14/audio_9939f77c30.mp3?filename=lofi-chill-medium-version-122394.mp3',
    lyrics: `[00:00.00] (Subtle rhythmic click and deep 808 sub)
[00:10.00] Binary thoughts in a lucid stream
[00:20.00] Building the logic of an open dream
[00:30.00] Clean architecture, elegant lines
[00:40.00] Where mathematical beauty shines
[00:50.00] Uninterrupted focus hour by hour
[01:00.00] Coding with effortless mental power`
  },
  {
    id: 'track-5',
    title: 'Ethereal Echoes',
    artist: 'Solaris Wave',
    album: 'Zero Horizon',
    genre: 'Ambient',
    duration: 210,
    art: 'assets/covers/cosmic.jpg',
    audioUrl: 'https://cdn.pixabay.com/download/audio/2021/08/04/audio_bb630cc098.mp3?filename=ambient-piano-amp-strings-10711.mp3',
    lyrics: `[00:00.00] (Ethereal reverberated piano chords)
[00:15.00] Echoes travelling through time and space
[00:30.00] Soft reflections in an empty place
[00:45.00] Breathing in the serenity of silence
[01:00.00] Peace restored in harmonious balance`
  }
];

// =============================================================================
// 2. APPLICATION STATE
// =============================================================================

class VibraState {
  constructor() {
    this.playlist = [...CURATED_TRACKS];
    this.queue = [...CURATED_TRACKS];
    this.currentIndex = 0;
    this.isPlaying = false;
    this.isMuted = false;
    this.volume = parseFloat(localStorage.getItem('vibra_volume') || '0.8');
    this.playbackRate = 1.0;
    this.isShuffle = false;
    this.repeatMode = 'all'; // 'off', 'all', 'one'
    this.favorites = JSON.parse(localStorage.getItem('vibra_favorites') || '["track-1"]');
    this.currentTheme = localStorage.getItem('vibra_theme') || 'apple-glass';
    this.sleepTimerMinutes = 0;
    this.sleepTimerRemaining = 0;
    this.sleepInterval = null;
    this.activeGenre = 'all';
    this.stats = JSON.parse(localStorage.getItem('vibra_stats') || JSON.stringify({
      playCount: 0,
      totalSecondsListened: 0,
      topGenre: 'Synthwave'
    }));
  }

  getCurrentTrack() {
    return this.queue[this.currentIndex] || this.playlist[0];
  }

  isCurrentFavorite() {
    const track = this.getCurrentTrack();
    return this.favorites.includes(track.id);
  }

  toggleFavorite(trackId) {
    const idx = this.favorites.indexOf(trackId);
    if (idx > -1) {
      this.favorites.splice(idx, 1);
    } else {
      this.favorites.push(trackId);
    }
    localStorage.setItem('vibra_favorites', JSON.stringify(this.favorites));
  }

  saveStats() {
    localStorage.setItem('vibra_stats', JSON.stringify(this.stats));
  }
}

const state = new VibraState();

// =============================================================================
// 3. WEB AUDIO API & EQUALIZER ENGINE
// =============================================================================

class AudioEngine {
  constructor() {
    this.audioElement = document.getElementById('nativeAudio');
    this.audioCtx = null;
    this.sourceNode = null;
    this.analyser = null;
    this.gainNode = null;
    this.eqBands = [];
    this.isInitialized = false;

    // Frequencies: 60Hz, 150Hz, 400Hz, 1kHz, 2.4kHz, 15kHz
    this.frequencies = [60, 150, 400, 1000, 2400, 15000];
    this.eqGains = [0, 0, 0, 0, 0, 0];
    this.eqEnabled = true;

    this.initAudioContext = this.initAudioContext.bind(this);
  }

  initAudioContext() {
    if (this.isInitialized) return;

    try {
      const AudioCtxClass = window.AudioContext || window.webkitAudioContext;
      this.audioCtx = new AudioCtxClass();

      this.sourceNode = this.audioCtx.createMediaElementSource(this.audioElement);
      this.analyser = this.audioCtx.createAnalyser();
      this.analyser.fftSize = 256;
      this.analyser.smoothingTimeConstant = 0.85;

      this.gainNode = this.audioCtx.createGain();
      this.gainNode.gain.value = state.volume;

      // Build Biquad Filter EQ Chain
      let lastNode = this.sourceNode;
      this.eqBands = this.frequencies.map((freq, index) => {
        const filter = this.audioCtx.createBiquadFilter();
        if (index === 0) {
          filter.type = 'lowshelf';
        } else if (index === this.frequencies.length - 1) {
          filter.type = 'highshelf';
        } else {
          filter.type = 'peaking';
          filter.Q.value = 1.4;
        }
        filter.frequency.value = freq;
        filter.gain.value = this.eqGains[index];

        lastNode.connect(filter);
        lastNode = filter;
        return filter;
      });

      lastNode.connect(this.gainNode);
      this.gainNode.connect(this.analyser);
      this.analyser.connect(this.audioCtx.destination);

      this.isInitialized = true;
    } catch (err) {
      console.warn('Web Audio API initialized in fallback mode:', err);
    }
  }

  resume() {
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }
  }

  setGain(bandIndex, dbValue) {
    this.eqGains[bandIndex] = dbValue;
    if (this.eqBands[bandIndex] && this.eqEnabled) {
      this.eqBands[bandIndex].gain.setTargetAtTime(dbValue, this.audioCtx.currentTime, 0.05);
    }
  }

  setEqEnabled(enabled) {
    this.eqEnabled = enabled;
    this.eqBands.forEach((band, idx) => {
      band.gain.setTargetAtTime(enabled ? this.eqGains[idx] : 0, this.audioCtx.currentTime, 0.05);
    });
  }

  applyPreset(presetName) {
    const presets = {
      'flat': [0, 0, 0, 0, 0, 0],
      'bass-boost': [8, 6, 2, 0, 1, 2],
      'edm': [6, 4, -1, 2, 4, 6],
      'vocal': [-2, 0, 3, 5, 4, 1],
      'rock': [5, 3, -1, 2, 4, 5],
      'acoustic': [3, 2, 1, 2, 3, 4]
    };

    const gains = presets[presetName] || presets.flat;
    gains.forEach((g, idx) => {
      this.setGain(idx, g);
      const slider = document.getElementById(['eq60', 'eq150', 'eq400', 'eq1k', 'eq2k4', 'eq15k'][idx]);
      const label = document.getElementById(['gainVal60', 'gainVal150', 'gainVal400', 'gainVal1k', 'gainVal2k4', 'gainVal15k'][idx]);
      if (slider) slider.value = g;
      if (label) label.textContent = `${g > 0 ? '+' : ''}${g} dB`;
    });
  }
}

const audio = new AudioEngine();

// =============================================================================
// 4. CANVAS AUDIO SPECTRUM VISUALIZER
// =============================================================================

class Visualizer {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    if (!this.canvas) return;
    this.ctx = this.canvas.getContext('2d');
    this.mode = 'bars'; // 'bars', 'wave', 'circular'
    this.animationId = null;
    this.resizeCanvas();
    window.addEventListener('resize', () => this.resizeCanvas());
  }

  resizeCanvas() {
    if (!this.canvas) return;
    const rect = this.canvas.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    this.canvas.width = rect.width * dpr;
    this.canvas.height = rect.height * dpr;
    this.ctx.scale(dpr, dpr);
  }

  start() {
    if (this.animationId) cancelAnimationFrame(this.animationId);
    const render = () => {
      this.draw();
      this.animationId = requestAnimationFrame(render);
    };
    render();
  }

  draw() {
    if (!this.canvas) return;
    const width = this.canvas.offsetWidth;
    const height = this.canvas.offsetHeight;
    this.ctx.clearRect(0, 0, width, height);

    let dataArray = null;
    if (audio.analyser && state.isPlaying) {
      const bufferLength = audio.analyser.frequencyBinCount;
      dataArray = new Uint8Array(bufferLength);
      if (this.mode === 'wave') {
        audio.analyser.getByteTimeDomainData(dataArray);
      } else {
        audio.analyser.getByteFrequencyData(dataArray);
      }
    } else {
      // Simulated subtle ambient floating rhythm when paused
      dataArray = new Uint8Array(64);
      const time = Date.now() * 0.003;
      for (let i = 0; i < 64; i++) {
        dataArray[i] = Math.sin(time + i * 0.2) * 20 + 25;
      }
    }

    if (this.mode === 'bars') {
      this.drawBars(dataArray, width, height);
    } else if (this.mode === 'wave') {
      this.drawWave(dataArray, width, height);
    } else if (this.mode === 'circular') {
      this.drawCircular(dataArray, width, height);
    }
  }

  drawBars(dataArray, width, height) {
    const bars = 48;
    const barWidth = (width / bars) * 0.75;
    const gap = (width / bars) * 0.25;

    for (let i = 0; i < bars; i++) {
      const val = dataArray[i * 2] || 0;
      const barHeight = Math.max(4, (val / 255) * height * 0.85);

      const x = i * (barWidth + gap) + gap;
      const y = height - barHeight;

      const grad = this.ctx.createLinearGradient(0, height, 0, y);
      grad.addColorStop(0, 'rgba(250, 45, 72, 0.4)');
      grad.addColorStop(0.5, '#fa2d48');
      grad.addColorStop(1, '#00f0ff');

      this.ctx.fillStyle = grad;
      this.ctx.beginPath();
      this.ctx.roundRect(x, y, barWidth, barHeight, [4, 4, 0, 0]);
      this.ctx.fill();

      // Glowing peak cap
      this.ctx.fillStyle = '#ffffff';
      this.ctx.shadowColor = '#00f0ff';
      this.ctx.shadowBlur = 8;
      this.ctx.fillRect(x, y - 3, barWidth, 2);
      this.ctx.shadowBlur = 0;
    }
  }

  drawWave(dataArray, width, height) {
    this.ctx.lineWidth = 3;
    this.ctx.strokeStyle = '#00f0ff';
    this.ctx.shadowColor = '#00f0ff';
    this.ctx.shadowBlur = 12;
    this.ctx.beginPath();

    const sliceWidth = width / dataArray.length;
    let x = 0;

    for (let i = 0; i < dataArray.length; i++) {
      const v = dataArray[i] / 128.0;
      const y = (v * height) / 2;

      if (i === 0) {
        this.ctx.moveTo(x, y);
      } else {
        this.ctx.lineTo(x, y);
      }
      x += sliceWidth;
    }

    this.ctx.lineTo(width, height / 2);
    this.ctx.stroke();
    this.ctx.shadowBlur = 0;
  }

  drawCircular(dataArray, width, height) {
    const centerX = width / 2;
    const centerY = height / 2;
    const radius = Math.min(centerX, centerY) * 0.45;
    const numPoints = 64;

    this.ctx.beginPath();
    this.ctx.arc(centerX, centerY, radius * 0.8, 0, Math.PI * 2);
    this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.15)';
    this.ctx.lineWidth = 2;
    this.ctx.stroke();

    for (let i = 0; i < numPoints; i++) {
      const angle = (i / numPoints) * Math.PI * 2;
      const val = (dataArray[i] || 0) / 255;
      const barLen = val * (radius * 0.9);

      const x1 = centerX + Math.cos(angle) * radius;
      const y1 = centerY + Math.sin(angle) * radius;
      const x2 = centerX + Math.cos(angle) * (radius + barLen);
      const y2 = centerY + Math.sin(angle) * (radius + barLen);

      const grad = this.ctx.createLinearGradient(x1, y1, x2, y2);
      grad.addColorStop(0, '#fa2d48');
      grad.addColorStop(1, '#ff007a');

      this.ctx.strokeStyle = grad;
      this.ctx.lineWidth = 3;
      this.ctx.beginPath();
      this.ctx.moveTo(x1, y1);
      this.ctx.lineTo(x2, y2);
      this.ctx.stroke();
    }
  }
}

let visualizer = null;

// =============================================================================
// 5. REAL-TIME SYNCED LYRICS PARSER (LRCLIB FORMAT)
// =============================================================================

class LyricsEngine {
  constructor() {
    this.parsedLines = [];
    this.currentIndex = -1;
  }

  parse(lrcString) {
    this.parsedLines = [];
    if (!lrcString) return;

    const lines = lrcString.split('\n');
    const timeRegex = /\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\]/g;

    for (const rawLine of lines) {
      let match;
      const timeStamps = [];
      let text = rawLine;

      while ((match = timeRegex.exec(rawLine)) !== null) {
        const mins = parseInt(match[1], 10);
        const secs = parseInt(match[2], 10);
        const ms = match[3] ? parseInt(match[3].padEnd(3, '0'), 10) : 0;
        const totalSeconds = mins * 60 + secs + ms / 1000;
        timeStamps.push(totalSeconds);
        text = text.replace(match[0], '');
      }

      text = text.trim();
      if (text) {
        timeStamps.forEach(time => {
          this.parsedLines.push({ time, text });
        });
      }
    }

    this.parsedLines.sort((a, b) => a.time - b.time);
  }

  async fetchOnlineLyrics(artist, track) {
    try {
      const res = await fetch(`https://lrclib.net/api/get?artist_name=${encodeURIComponent(artist)}&track_name=${encodeURIComponent(track)}`);
      if (res.ok) {
        const data = await res.json();
        if (data.syncedLyrics) {
          this.parse(data.syncedLyrics);
          return true;
        } else if (data.plainLyrics) {
          this.parsePlain(data.plainLyrics);
          return true;
        }
      }
    } catch (e) {
      console.warn('LRCLIB lyrics fetch fallback:', e);
    }
    return false;
  }

  parsePlain(plainText) {
    this.parsedLines = plainText.split('\n').filter(l => l.trim()).map((text, i) => ({
      time: i * 5,
      text
    }));
  }

  renderTo(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (this.parsedLines.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; color: var(--text-dim); padding: 40px 0;">
          <p style="font-size: 16px;">Instrumental / No synced lyrics available for this track.</p>
          <span style="font-size: 12px; margin-top: 8px; display: block;">Enjoy the frequencies & visualizer 🎶</span>
        </div>`;
      return;
    }

    container.innerHTML = this.parsedLines.map((line, idx) => `
      <div class="lyric-line" data-index="${idx}" data-time="${line.time}">
        ${line.text}
      </div>
    `).join('');

    // Click to seek
    container.querySelectorAll('.lyric-line').forEach(el => {
      el.addEventListener('click', () => {
        const time = parseFloat(el.getAttribute('data-time'));
        audio.audioElement.currentTime = time;
      });
    });
  }

  update(currentTime, containerId) {
    if (this.parsedLines.length === 0) return;

    let activeIdx = -1;
    for (let i = 0; i < this.parsedLines.length; i++) {
      if (currentTime >= this.parsedLines[i].time) {
        activeIdx = i;
      } else {
        break;
      }
    }

    if (activeIdx !== this.currentIndex) {
      this.currentIndex = activeIdx;
      const container = document.getElementById(containerId);
      if (!container) return;

      const lines = container.querySelectorAll('.lyric-line');
      lines.forEach((el, idx) => {
        if (idx === activeIdx) {
          el.classList.add('active');
          el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        } else {
          el.classList.remove('active');
        }
      });
    }
  }
}

const lyricsEngine = new LyricsEngine();

// =============================================================================
// 6. PLAYER CONTROLLER & MEDIA SESSION
// =============================================================================

function loadTrack(index, autoPlay = true) {
  if (index < 0 || index >= state.queue.length) return;
  state.currentIndex = index;
  const track = state.getCurrentTrack();

  audio.audioElement.src = track.audioUrl;
  audio.audioElement.load();

  // Update UI Elements
  document.getElementById('playerTrackTitle').textContent = track.title;
  document.getElementById('playerArtistName').textContent = track.artist;
  document.getElementById('playerCoverArt').src = track.art;
  document.getElementById('sidebarTrackTitle').textContent = track.title;
  document.getElementById('sidebarTrackArtist').textContent = track.artist;
  document.getElementById('sidebarTrackArt').src = track.art;
  document.getElementById('heroSongTitle').textContent = `${track.artist} — ${track.title}`;
  document.getElementById('heroCoverArt').src = track.art;
  document.getElementById('lyricsTrackName').textContent = track.title;
  document.getElementById('lyricsArtistName').textContent = track.artist;
  document.getElementById('lyricsArtwork').src = track.art;
  document.getElementById('expandedTitle').textContent = track.title;
  document.getElementById('expandedArtist').textContent = track.artist;
  document.getElementById('expandedAlbumArt').src = track.art;
  document.getElementById('visTrackLabel').textContent = `${track.artist} — ${track.title}`;

  // Update Favorite Heart
  updateFavoriteIcons();

  // Load Lyrics (Curated or LRCLIB Online)
  if (track.lyrics) {
    lyricsEngine.parse(track.lyrics);
    lyricsEngine.renderTo('lyricsScrollBox');
    lyricsEngine.renderTo('expandedLyricsScroll');
  } else {
    lyricsEngine.fetchOnlineLyrics(track.artist, track.title).then(() => {
      lyricsEngine.renderTo('lyricsScrollBox');
      lyricsEngine.renderTo('expandedLyricsScroll');
    });
  }

  // Windows Media Session API Integration
  if ('mediaSession' in navigator) {
    navigator.mediaSession.metadata = new MediaMetadata({
      title: track.title,
      artist: track.artist,
      album: track.album || 'Vibra Master',
      artwork: [
        { src: track.art, sizes: '512x512', type: 'image/jpeg' }
      ]
    });
  }

  // Update Queue & Grid active cards
  renderQueue();
  updateActiveCards();

  if (autoPlay) {
    playTrack();
  }
}

function playTrack() {
  audio.initAudioContext();
  audio.resume();

  audio.audioElement.play().then(() => {
    state.isPlaying = true;
    updatePlaybackUI();
    state.stats.playCount++;
    state.saveStats();
    updateStatsUI();
  }).catch(e => {
    console.warn('Playback gesture required or source error:', e);
  });
}

function pauseTrack() {
  audio.audioElement.pause();
  state.isPlaying = false;
  updatePlaybackUI();
}

function togglePlay() {
  if (state.isPlaying) {
    pauseTrack();
  } else {
    playTrack();
  }
}

function nextTrack() {
  if (state.repeatMode === 'one') {
    audio.audioElement.currentTime = 0;
    playTrack();
    return;
  }

  let nextIdx = state.currentIndex + 1;
  if (state.isShuffle) {
    nextIdx = Math.floor(Math.random() * state.queue.length);
  } else if (nextIdx >= state.queue.length) {
    if (state.repeatMode === 'all') {
      nextIdx = 0;
    } else {
      pauseTrack();
      return;
    }
  }
  loadTrack(nextIdx, true);
}

function prevTrack() {
  if (audio.audioElement.currentTime > 3) {
    audio.audioElement.currentTime = 0;
    return;
  }
  let prevIdx = state.currentIndex - 1;
  if (prevIdx < 0) {
    prevIdx = state.queue.length - 1;
  }
  loadTrack(prevIdx, true);
}

function updatePlaybackUI() {
  const masterBtn = document.getElementById('masterPlayBtn');
  const playIcon = masterBtn.querySelector('.play-icon');
  const pauseIcon = masterBtn.querySelector('.pause-icon');
  const sidebarWaves = document.getElementById('sidebarWaves');

  if (state.isPlaying) {
    playIcon.style.display = 'none';
    pauseIcon.style.display = 'block';
    sidebarWaves.classList.add('playing');
    if ('mediaSession' in navigator) navigator.mediaSession.playbackState = 'playing';
  } else {
    playIcon.style.display = 'block';
    pauseIcon.style.display = 'none';
    sidebarWaves.classList.remove('playing');
    if ('mediaSession' in navigator) navigator.mediaSession.playbackState = 'paused';
  }
}

function formatTime(seconds) {
  if (isNaN(seconds)) return '0:00';
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${s < 10 ? '0' : ''}${s}`;
}

// =============================================================================
// 7. ONLINE YOUTUBE MUSIC SEARCH (INVIDIOUS API)
// =============================================================================

async function searchOnlineYouTubeMusic(query) {
  if (!query.trim()) return;

  const trackGrid = document.getElementById('trackGrid');
  trackGrid.innerHTML = `
    <div style="grid-column: 1 / -1; text-align: center; padding: 40px;">
      <div style="font-size: 16px; color: var(--accent-primary);">Searching YouTube Music cloud for "${query}"...</div>
    </div>`;

  const instances = [
    'https://inv.tux.pizza',
    'https://invidious.nerdvpn.de',
    'https://yewtu.be'
  ];

  let results = null;

  for (const inst of instances) {
    try {
      const res = await fetch(`${inst}/api/v1/search?q=${encodeURIComponent(query)}&type=video`, {
        signal: AbortSignal.timeout(4000)
      });
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) {
          results = data.slice(0, 8).map(v => ({
            id: `yt-${v.videoId}`,
            title: v.title,
            artist: v.author,
            album: 'YouTube Music Stream',
            genre: 'Online',
            duration: v.lengthSeconds || 210,
            art: v.videoThumbnails?.[0]?.url || 'assets/covers/cyberpunk.jpg',
            audioUrl: `${inst}/latest_version?id=${v.videoId}&itag=251`
          }));
          break;
        }
      }
    } catch (e) {
      console.warn(`Instance ${inst} failed:`, e);
    }
  }

  if (results && results.length > 0) {
    state.queue = [...results, ...state.playlist];
    renderTrackGrid(results);
    renderQueue();
  } else {
    // Fallback: local search
    const localFiltered = state.playlist.filter(t => 
      t.title.toLowerCase().includes(query.toLowerCase()) ||
      t.artist.toLowerCase().includes(query.toLowerCase()) ||
      t.genre.toLowerCase().includes(query.toLowerCase())
    );
    renderTrackGrid(localFiltered);
  }
}

// =============================================================================
// 8. LOCAL AUDIO FILE IMPORT (DRAG & DROP)
// =============================================================================

function setupLocalFileHandlers() {
  const fileInput = document.getElementById('localFileInput');
  const dropzone = document.getElementById('dropzoneCard');

  const processFiles = (files) => {
    Array.from(files).forEach(file => {
      if (!file.type.startsWith('audio/')) return;

      const blobUrl = URL.createObjectURL(file);
      const cleanName = file.name.replace(/\.[^/.]+$/, "");
      const newTrack = {
        id: `local-${Date.now()}-${Math.random()}`,
        title: cleanName,
        artist: 'Local Music File',
        album: 'My Computer',
        genre: 'Local',
        duration: 180,
        art: 'assets/covers/lofi.jpg',
        audioUrl: blobUrl
      };

      state.playlist.unshift(newTrack);
      state.queue.unshift(newTrack);
    });

    renderTrackGrid(state.playlist);
    renderQueue();
    loadTrack(0, true);
  };

  fileInput.addEventListener('change', (e) => processFiles(e.target.files));

  ['dragenter', 'dragover'].forEach(name => {
    dropzone.addEventListener(name, (e) => {
      e.preventDefault();
      dropzone.classList.add('drag-over');
    });
  });

  ['dragleave', 'drop'].forEach(name => {
    dropzone.addEventListener(name, (e) => {
      e.preventDefault();
      dropzone.classList.remove('drag-over');
    });
  });

  dropzone.addEventListener('drop', (e) => {
    if (e.dataTransfer.files) {
      processFiles(e.dataTransfer.files);
    }
  });
}

// =============================================================================
// 9. UI RENDERING & EVENT LISTENERS
// =============================================================================

function renderTrackGrid(tracksToRender = state.playlist) {
  const grid = document.getElementById('trackGrid');
  document.getElementById('trackCount').textContent = tracksToRender.length;

  grid.innerHTML = tracksToRender.map((t, idx) => {
    const isPlayingThis = state.isPlaying && state.getCurrentTrack().id === t.id;
    const isLiked = state.favorites.includes(t.id);

    return `
      <div class="track-card ${isPlayingThis ? 'active-playing' : ''}" data-id="${t.id}" data-index="${idx}">
        <div class="track-art-box">
          <img src="${t.art}" alt="${t.title}">
          <div class="track-play-badge">
            <svg viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
          </div>
        </div>
        <div class="track-meta-title" title="${t.title}">${t.title}</div>
        <div class="track-meta-artist">${t.artist}</div>
        <div class="track-bottom-row">
          <span class="track-genre-tag">${t.genre}</span>
          <button class="card-like-btn ${isLiked ? 'liked' : ''}" data-id="${t.id}">
            <svg viewBox="0 0 24 24" fill="${isLiked ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
          </button>
        </div>
      </div>
    `;
  }).join('');

  // Card click triggers
  grid.querySelectorAll('.track-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.card-like-btn')) return;
      const trackId = card.getAttribute('data-id');
      const queueIdx = state.queue.findIndex(t => t.id === trackId);
      if (queueIdx > -1) {
        loadTrack(queueIdx, true);
      }
    });
  });

  // Like click triggers
  grid.querySelectorAll('.card-like-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const trackId = btn.getAttribute('data-id');
      state.toggleFavorite(trackId);
      renderTrackGrid(tracksToRender);
      updateFavoriteIcons();
      updateStatsUI();
    });
  });
}

function renderChartsTable() {
  const tableBody = document.getElementById('chartsTableBody');
  if (!tableBody) return;

  tableBody.innerHTML = state.playlist.map((t, idx) => `
    <div class="charts-table-row" data-index="${idx}">
      <span class="col-rank">${idx + 1}</span>
      <div class="col-title">
        <img src="${t.art}" alt="${t.title}">
        <div class="table-track-info">
          <div class="table-track-name">${t.title}</div>
          <div class="table-track-artist">${t.artist}</div>
        </div>
      </div>
      <div class="col-album">${t.album || 'Single'}</div>
      <div class="col-genre">${t.genre}</div>
      <div class="col-time">${formatTime(t.duration)}</div>
      <div class="col-action">
        <button class="table-play-btn" data-index="${idx}">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
        </button>
      </div>
    </div>
  `).join('');

  tableBody.querySelectorAll('.charts-table-row').forEach(row => {
    row.addEventListener('click', () => {
      const idx = parseInt(row.getAttribute('data-index'), 10);
      loadTrack(idx, true);
    });
  });
}

function renderFavoritesGrid() {
  const favGrid = document.getElementById('favoritesGrid');
  if (!favGrid) return;
  const favTracks = state.playlist.filter(t => state.favorites.includes(t.id));
  document.getElementById('favCount').textContent = favTracks.length;

  if (favTracks.length === 0) {
    favGrid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-dim);">
        <p style="font-size: 16px;">No favorite tracks yet.</p>
        <span style="font-size: 13px; margin-top: 6px; display: block;">Click the heart icon on any track to add it here.</span>
      </div>`;
    return;
  }

  favGrid.innerHTML = favTracks.map((t, idx) => `
    <div class="track-card" data-id="${t.id}">
      <div class="track-art-box">
        <img src="${t.art}" alt="${t.title}">
        <div class="track-play-badge">
          <svg viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
        </div>
      </div>
      <div class="track-meta-title">${t.title}</div>
      <div class="track-meta-artist">${t.artist}</div>
      <div class="track-bottom-row">
        <span class="track-genre-tag">${t.genre}</span>
        <button class="card-like-btn liked" data-id="${t.id}">
          <svg viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
        </button>
      </div>
    </div>
  `).join('');

  favGrid.querySelectorAll('.track-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.card-like-btn')) return;
      const trackId = card.getAttribute('data-id');
      const idx = state.queue.findIndex(t => t.id === trackId);
      if (idx > -1) loadTrack(idx, true);
    });
  });

  favGrid.querySelectorAll('.card-like-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const trackId = btn.getAttribute('data-id');
      state.toggleFavorite(trackId);
      renderFavoritesGrid();
      updateFavoriteIcons();
      updateStatsUI();
    });
  });
}

function renderQueue() {
  const queueList = document.getElementById('queueList');
  document.getElementById('queueCount').textContent = `${state.queue.length} tracks`;

  queueList.innerHTML = state.queue.map((t, idx) => `
    <div class="queue-item ${idx === state.currentIndex ? 'current-playing' : ''}" data-index="${idx}">
      <img src="${t.art}" alt="${t.title}">
      <div class="queue-item-meta">
        <div class="queue-item-title">${t.title}</div>
        <div class="queue-item-artist">${t.artist}</div>
      </div>
      <span style="font-size: 11px; color: var(--text-dim);">${formatTime(t.duration)}</span>
    </div>
  `).join('');

  queueList.querySelectorAll('.queue-item').forEach(item => {
    item.addEventListener('click', () => {
      const idx = parseInt(item.getAttribute('data-index'), 10);
      loadTrack(idx, true);
    });
  });
}

function updateActiveCards() {
  const current = state.getCurrentTrack();
  document.querySelectorAll('.track-card').forEach(card => {
    if (card.getAttribute('data-id') === current.id) {
      card.classList.add('active-playing');
    } else {
      card.classList.remove('active-playing');
    }
  });
}

function updateFavoriteIcons() {
  const isFav = state.isCurrentFavorite();
  const playerLikeBtn = document.getElementById('playerLikeBtn');
  const svg = playerLikeBtn.querySelector('svg');
  if (isFav) {
    playerLikeBtn.classList.add('liked');
    svg.setAttribute('fill', 'currentColor');
  } else {
    playerLikeBtn.classList.remove('liked');
    svg.setAttribute('fill', 'none');
  }
  document.getElementById('favCount').textContent = state.favorites.length;
}

function updateStatsUI() {
  document.getElementById('statPlayCount').textContent = state.stats.playCount;
  document.getElementById('statListenMinutes').textContent = Math.round(state.stats.totalSecondsListened / 60);
  document.getElementById('statFavoritesCount').textContent = state.favorites.length;
  document.getElementById('statTopGenre').textContent = state.stats.topGenre;
}

// =============================================================================
// 10. NAVIGATION & EVENT BINDINGS
// =============================================================================

function setupNavigation() {
  const navBtns = document.querySelectorAll('.nav-item[data-view]');
  const viewPanels = document.querySelectorAll('.view-panel');

  navBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetView = btn.getAttribute('data-view');
      navBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      viewPanels.forEach(p => {
        if (p.id === `view-${targetView}`) {
          p.classList.add('active');
        } else {
          p.classList.remove('active');
        }
      });

      if (targetView === 'explore') renderChartsTable();
      if (targetView === 'library') renderFavoritesGrid();
      if (targetView === 'visualizer' && visualizer) visualizer.resizeCanvas();
    });
  });
}

function setupPlayerEventListeners() {
  const nativeAudio = audio.audioElement;

  document.getElementById('masterPlayBtn').addEventListener('click', togglePlay);
  document.getElementById('heroPlayBtn').addEventListener('click', togglePlay);
  document.getElementById('nextTrackBtn').addEventListener('click', nextTrack);
  document.getElementById('prevTrackBtn').addEventListener('click', prevTrack);

  // Time & Progress
  nativeAudio.addEventListener('timeupdate', () => {
    const cur = nativeAudio.currentTime;
    const dur = nativeAudio.duration || state.getCurrentTrack().duration;

    document.getElementById('currentTimeLabel').textContent = formatTime(cur);
    document.getElementById('durationTimeLabel').textContent = formatTime(dur);

    const pct = (cur / dur) * 100;
    document.getElementById('seekProgress').style.width = `${pct}%`;
    document.getElementById('seekThumb').style.left = `${pct}%`;

    // Synced lyrics update
    lyricsEngine.update(cur, 'lyricsScrollBox');
    lyricsEngine.update(cur, 'expandedLyricsScroll');

    // Stats
    if (state.isPlaying) {
      state.stats.totalSecondsListened += 0.25;
      if (Math.round(state.stats.totalSecondsListened) % 15 === 0) {
        state.saveStats();
        updateStatsUI();
      }
    }
  });

  // Track Ended
  nativeAudio.addEventListener('ended', () => {
    if (state.sleepTimerMinutes === -1) {
      pauseTrack();
      state.sleepTimerMinutes = 0;
      document.getElementById('sleepTimerBadge').textContent = 'Sleep';
      return;
    }
    nextTrack();
  });

  // Seek Scrubber
  const seekTrack = document.getElementById('seekTrack');
  const seekTooltip = document.getElementById('seekTooltip');

  seekTrack.addEventListener('mousemove', (e) => {
    const rect = seekTrack.getBoundingClientRect();
    const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    const dur = nativeAudio.duration || state.getCurrentTrack().duration;
    seekTooltip.style.display = 'block';
    seekTooltip.style.left = `${pct * 100}%`;
    seekTooltip.textContent = formatTime(pct * dur);
  });

  seekTrack.addEventListener('mouseleave', () => {
    seekTooltip.style.display = 'none';
  });

  seekTrack.addEventListener('click', (e) => {
    const rect = seekTrack.getBoundingClientRect();
    const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    const dur = nativeAudio.duration || state.getCurrentTrack().duration;
    nativeAudio.currentTime = pct * dur;
  });

  // Volume
  const volumeTrack = document.getElementById('volumeTrack');
  const volumeFill = document.getElementById('volumeFill');
  const volumeMuteBtn = document.getElementById('volumeMuteBtn');

  const updateVol = (vol) => {
    state.volume = Math.max(0, Math.min(1, vol));
    nativeAudio.volume = state.volume;
    if (audio.gainNode) audio.gainNode.gain.value = state.volume;
    volumeFill.style.width = `${state.volume * 100}%`;
    localStorage.setItem('vibra_volume', state.volume.toString());

    const volHigh = volumeMuteBtn.querySelector('.vol-high');
    const volMuted = volumeMuteBtn.querySelector('.vol-muted');
    if (state.volume === 0 || state.isMuted) {
      volHigh.style.display = 'none';
      volMuted.style.display = 'block';
    } else {
      volHigh.style.display = 'block';
      volMuted.style.display = 'none';
    }
  };

  volumeTrack.addEventListener('click', (e) => {
    const rect = volumeTrack.getBoundingClientRect();
    const vol = (e.clientX - rect.left) / rect.width;
    state.isMuted = false;
    updateVol(vol);
  });

  volumeMuteBtn.addEventListener('click', () => {
    state.isMuted = !state.isMuted;
    nativeAudio.muted = state.isMuted;
    updateVol(state.isMuted ? 0 : state.volume);
  });

  // Shuffle & Repeat
  const shuffleBtn = document.getElementById('shuffleBtn');
  shuffleBtn.addEventListener('click', () => {
    state.isShuffle = !state.isShuffle;
    shuffleBtn.classList.toggle('active', state.isShuffle);
  });

  const repeatBtn = document.getElementById('repeatBtn');
  repeatBtn.addEventListener('click', () => {
    const modes = ['all', 'one', 'off'];
    const nextMode = modes[(modes.indexOf(state.repeatMode) + 1) % modes.length];
    state.repeatMode = nextMode;
    repeatBtn.classList.toggle('active', state.repeatMode !== 'off');
    repeatBtn.title = `Repeat: ${state.repeatMode.toUpperCase()}`;
  });

  // Playback speed
  const speedBtn = document.getElementById('speedToggleBtn');
  const speeds = [1.0, 1.25, 1.5, 2.0, 0.75];
  speedBtn.addEventListener('click', () => {
    const nextSpeed = speeds[(speeds.indexOf(state.playbackRate) + 1) % speeds.length];
    state.playbackRate = nextSpeed;
    nativeAudio.playbackRate = nextSpeed;
    speedBtn.textContent = `${nextSpeed}x`;
  });

  // Favorite button
  document.getElementById('playerLikeBtn').addEventListener('click', () => {
    state.toggleFavorite(state.getCurrentTrack().id);
    updateFavoriteIcons();
    renderTrackGrid();
    renderFavoritesGrid();
    updateStatsUI();
  });

  // Up Next Queue Toggle
  const queueDrawer = document.getElementById('queueDrawer');
  document.getElementById('queueToggleBtn').addEventListener('click', () => {
    queueDrawer.classList.toggle('open');
  });
  document.getElementById('closeQueueBtn').addEventListener('click', () => {
    queueDrawer.classList.remove('open');
  });

  // Expanded Now Playing Modal
  const expandedModal = document.getElementById('expandedPlayerModal');
  const expandBtn = document.getElementById('expandPlayerBtn');
  const playerArtTrigger = document.getElementById('playerArtTrigger');
  const closeExpandedBtn = document.getElementById('closeExpandedModalBtn');

  [expandBtn, playerArtTrigger].forEach(el => {
    el.addEventListener('click', () => expandedModal.classList.add('open'));
  });
  closeExpandedBtn.addEventListener('click', () => expandedModal.classList.remove('open'));

  // Hero Queue button
  document.getElementById('heroQueueBtn').addEventListener('click', () => {
    state.queue.push(state.getCurrentTrack());
    renderQueue();
  });
}

function setupEqualizerControls() {
  const sliders = [
    { id: 'eq60', valId: 'gainVal60', idx: 0 },
    { id: 'eq150', valId: 'gainVal150', idx: 1 },
    { id: 'eq400', valId: 'gainVal400', idx: 2 },
    { id: 'eq1k', valId: 'gainVal1k', idx: 3 },
    { id: 'eq2k4', valId: 'gainVal2k4', idx: 4 },
    { id: 'eq15k', valId: 'gainVal15k', idx: 5 }
  ];

  sliders.forEach(s => {
    const el = document.getElementById(s.id);
    const label = document.getElementById(s.valId);
    el.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      label.textContent = `${val > 0 ? '+' : ''}${val} dB`;
      audio.setGain(s.idx, val);
    });
  });

  // EQ Power Toggle
  const eqToggle = document.getElementById('eqPowerToggle');
  eqToggle.addEventListener('change', (e) => {
    audio.setEqEnabled(e.target.checked);
  });

  // Preset Buttons
  document.querySelectorAll('.eq-preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.eq-preset-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const preset = btn.getAttribute('data-preset');
      audio.applyPreset(preset);
    });
  });
}

function setupVisualizerControls() {
  document.querySelectorAll('.vis-mode-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.vis-mode-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      if (visualizer) visualizer.mode = btn.getAttribute('data-vismode');
    });
  });
}

function setupSearchAndFilters() {
  const searchInput = document.getElementById('trackSearchInput');
  const clearBtn = document.getElementById('clearSearchBtn');

  let debounceTimer = null;
  searchInput.addEventListener('input', (e) => {
    const q = e.target.value.trim();
    clearBtn.style.display = q ? 'block' : 'none';

    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      if (q) {
        searchOnlineYouTubeMusic(q);
      } else {
        renderTrackGrid(state.playlist);
      }
    }, 400);
  });

  clearBtn.addEventListener('click', () => {
    searchInput.value = '';
    clearBtn.style.display = 'none';
    renderTrackGrid(state.playlist);
  });

  // Genre chips
  document.querySelectorAll('.chip[data-genre]').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const genre = chip.getAttribute('data-genre');
      state.activeGenre = genre;

      if (genre === 'all') {
        renderTrackGrid(state.playlist);
      } else {
        const filtered = state.playlist.filter(t => t.genre.toLowerCase() === genre.toLowerCase());
        renderTrackGrid(filtered);
      }
    });
  });
}

function setupThemeSwitcher() {
  const menuBtn = document.getElementById('themeMenuBtn');
  const menu = document.getElementById('themeDropdownMenu');
  const label = document.getElementById('currentThemeLabel');

  menuBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    menu.classList.toggle('show');
  });

  document.addEventListener('click', () => menu.classList.remove('show'));

  document.querySelectorAll('.theme-opt').forEach(opt => {
    opt.addEventListener('click', () => {
      const theme = opt.getAttribute('data-theme');
      document.documentElement.setAttribute('data-theme', theme);
      state.currentTheme = theme;
      localStorage.setItem('vibra_theme', theme);

      document.querySelectorAll('.theme-opt').forEach(o => o.classList.remove('active'));
      opt.classList.add('active');
      label.textContent = opt.textContent.split(' ')[1] || opt.textContent;
    });
  });

  // Set initial theme
  document.documentElement.setAttribute('data-theme', state.currentTheme);
}

function setupSleepTimer() {
  const sleepBtn = document.getElementById('sleepTimerBtn');
  const modal = document.getElementById('sleepModalBackdrop');
  const closeBtn = document.getElementById('closeSleepModalBtn');

  sleepBtn.addEventListener('click', () => modal.style.display = 'flex');
  closeBtn.addEventListener('click', () => modal.style.display = 'none');
  modal.addEventListener('click', (e) => {
    if (e.target === modal) modal.style.display = 'none';
  });

  document.querySelectorAll('.sleep-opt').forEach(opt => {
    opt.addEventListener('click', () => {
      const mins = parseInt(opt.getAttribute('data-minutes'), 10);
      modal.style.display = 'none';
      clearInterval(state.sleepInterval);

      if (mins === 0) {
        state.sleepTimerMinutes = 0;
        document.getElementById('sleepTimerBadge').textContent = 'Sleep';
        return;
      }

      if (mins === -1) {
        state.sleepTimerMinutes = -1;
        document.getElementById('sleepTimerBadge').textContent = 'Track End';
        return;
      }

      state.sleepTimerMinutes = mins;
      state.sleepTimerRemaining = mins * 60;
      document.getElementById('sleepTimerBadge').textContent = `${mins}m`;

      state.sleepInterval = setInterval(() => {
        state.sleepTimerRemaining--;
        if (state.sleepTimerRemaining <= 0) {
          clearInterval(state.sleepInterval);
          pauseTrack();
          document.getElementById('sleepTimerBadge').textContent = 'Sleep';
        } else {
          const remMins = Math.ceil(state.sleepTimerRemaining / 60);
          document.getElementById('sleepTimerBadge').textContent = `${remMins}m`;
        }
      }, 1000);
    });
  });
}

function setupKeyboardShortcuts() {
  document.addEventListener('keydown', (e) => {
    if (['input', 'textarea'].includes(document.activeElement.tagName.toLowerCase())) return;

    if (e.code === 'Space') {
      e.preventDefault();
      togglePlay();
    } else if (e.code === 'ArrowRight') {
      e.preventDefault();
      audio.audioElement.currentTime = Math.min(audio.audioElement.duration || 0, audio.audioElement.currentTime + 5);
    } else if (e.code === 'ArrowLeft') {
      e.preventDefault();
      audio.audioElement.currentTime = Math.max(0, audio.audioElement.currentTime - 5);
    } else if (e.code === 'ArrowUp') {
      e.preventDefault();
      state.volume = Math.min(1, state.volume + 0.05);
      audio.audioElement.volume = state.volume;
      document.getElementById('volumeFill').style.width = `${state.volume * 100}%`;
    } else if (e.code === 'ArrowDown') {
      e.preventDefault();
      state.volume = Math.max(0, state.volume - 0.05);
      audio.audioElement.volume = state.volume;
      document.getElementById('volumeFill').style.width = `${state.volume * 100}%`;
    } else if (e.key === 'm' || e.key === 'M') {
      document.getElementById('volumeMuteBtn').click();
    } else if (e.key === 'l' || e.key === 'L') {
      document.getElementById('navLyricsBtn').click();
    }
  });
}

// Windows Media Session Action Handlers
function setupMediaSessionHandlers() {
  if (!('mediaSession' in navigator)) return;

  navigator.mediaSession.setActionHandler('play', playTrack);
  navigator.mediaSession.setActionHandler('pause', pauseTrack);
  navigator.mediaSession.setActionHandler('previoustrack', prevTrack);
  navigator.mediaSession.setActionHandler('nexttrack', nextTrack);
  navigator.mediaSession.setActionHandler('seekto', (details) => {
    if (details.seekTime && audio.audioElement) {
      audio.audioElement.currentTime = details.seekTime;
    }
  });
}

// Android Back Button Bridge
window.handleAndroidBack = function() {
  const expanded = document.getElementById('expandedPlayerModal');
  const queue = document.getElementById('queueDrawer');
  const sleep = document.getElementById('sleepModalBackdrop');
  if (expanded && expanded.classList.contains('open')) {
    expanded.classList.remove('open');
    return true;
  }
  if (queue && queue.classList.contains('open')) {
    queue.classList.remove('open');
    return true;
  }
  if (sleep && sleep.style.display === 'flex') {
    sleep.style.display = 'none';
    return true;
  }
  return false;
};

// Mobile Touch Gestures (Swipe Player Bar to change tracks)
function setupMobileGestures() {
  const playerBar = document.getElementById('playerBar');
  let startX = 0;
  let startY = 0;

  playerBar.addEventListener('touchstart', (e) => {
    startX = e.touches[0].clientX;
    startY = e.touches[0].clientY;
  }, { passive: true });

  playerBar.addEventListener('touchend', (e) => {
    const deltaX = e.changedTouches[0].clientX - startX;
    const deltaY = e.changedTouches[0].clientY - startY;

    if (Math.abs(deltaX) > 60 && Math.abs(deltaY) < 40) {
      if (deltaX < 0) {
        nextTrack(); // Swipe Left -> Next
      } else {
        prevTrack(); // Swipe Right -> Prev
      }
    }
  }, { passive: true });
}

// =============================================================================
// 11. INITIALIZATION ON DOM READY
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
  visualizer = new Visualizer('audioVisualizerCanvas');
  visualizer.start();

  setupNavigation();
  setupPlayerEventListeners();
  setupEqualizerControls();
  setupVisualizerControls();
  setupSearchAndFilters();
  setupLocalFileHandlers();
  setupThemeSwitcher();
  setupSleepTimer();
  setupKeyboardShortcuts();
  setupMediaSessionHandlers();
  setupMobileGestures();

  // Initial load
  loadTrack(0, false);
  renderTrackGrid();
  renderFavoritesGrid();
  updateStatsUI();

  // Register service worker if available
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('sw.js').catch(() => {});
  }
});

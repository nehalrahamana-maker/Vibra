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
// 2. RESILIENT HELPERS & APPLICATION STATE
// =============================================================================

function loadJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return fallback;
    const parsed = JSON.parse(raw);
    return (parsed !== null && parsed !== undefined) ? parsed : fallback;
  } catch (e) {
    console.warn(`Corrupted localStorage key "${key}", safely using fallback:`, e);
    return fallback;
  }
}

function esc(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

const SETTINGS_KEY = 'vibra_settings_v1';
const DEFAULT_SETTINGS = {
  activeSource: 'itunes',
  fallbackSource: 'audius',
  apiBase: 'https://vibra-api.kazimdnehal.workers.dev',
  timeoutMs: 8000,
  lyricsProvider: 'lrclib',
  lyricsPlainFallback: true,
  crossfadeSec: 0,
  gapless: true,
  normalize: false,
  quality: 'bestaudio',
  storageCapMB: 500,
  autoCache: false,
  persistStorage: true,
  resultLimit: 25,
  theme: 'apple-glass',
  visMode: 'bars',
  reduceMotion: false,
  spatialWidth: 1.4,
  spatialEnabled: false,
  exciterMix: 0.15,
  exciterEnabled: false,
  eq: {
    gains: [0, 0, 0, 0, 0, 0],
    enabled: true,
    preset: 'flat'
  }
};

const rawSettings = loadJson(SETTINGS_KEY, {});
let settings = {
  ...DEFAULT_SETTINGS,
  ...rawSettings,
  apiBase: rawSettings.apiBase || DEFAULT_SETTINGS.apiBase,
  eq: {
    ...DEFAULT_SETTINGS.eq,
    ...(rawSettings.eq || {})
  }
};
const saveSettings = () => localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings));

class VibraState {
  constructor() {
    this.playlist = [...CURATED_TRACKS];
    this.searchResults = [];

    // Track Registry for cross-session ID resolution (search results added to playlists/favorites)
    this.trackRegistry = loadJson('vibra_tracks_v1', {});
    CURATED_TRACKS.forEach(t => { this.trackRegistry[t.id] = t; });

    // Persisted Queue Architecture
    const savedQueueData = loadJson('vibra_queue_v1', null);
    if (savedQueueData && Array.isArray(savedQueueData.queue) && savedQueueData.queue.length > 0) {
      this.queue = savedQueueData.queue;
      this.currentIndex = Math.min(Math.max(0, savedQueueData.currentIndex || 0), this.queue.length - 1);
    } else {
      this.queue = [...CURATED_TRACKS];
      this.currentIndex = 0;
    }

    this.isPlaying = false;
    this.isMuted = false;
    this.volume = parseFloat(localStorage.getItem('vibra_volume') || '0.8');
    this.playbackRate = 1.0;
    this.isShuffle = false;
    this.repeatMode = 'all'; // 'off', 'all', 'one'
    this.favorites = loadJson('vibra_favorites', ['track-1']);
    this.currentTheme = localStorage.getItem('vibra_theme') || settings.theme || 'apple-glass';
    this.sleepTimerMinutes = 0;
    this.sleepTimerRemaining = 0;
    this.sleepInterval = null;
    this.activeGenre = 'all';
    this.stats = loadJson('vibra_stats', {
      playCount: 0,
      totalSecondsListened: 0,
      topGenre: 'Synthwave'
    });
  }

  registerTrack(track) {
    if (!track || !track.id) return;
    this.trackRegistry[track.id] = track;
    try {
      localStorage.setItem('vibra_tracks_v1', JSON.stringify(this.trackRegistry));
    } catch (e) {}
  }

  findTrack(trackId) {
    if (!trackId) return null;
    return this.trackRegistry[trackId] ||
           this.playlist.find(t => t.id === trackId) ||
           this.queue.find(t => t.id === trackId) ||
           (this.searchResults && this.searchResults.find(t => t.id === trackId)) ||
           null;
  }

  getCurrentTrack() {
    return this.queue[this.currentIndex] || this.playlist[0] || CURATED_TRACKS[0];
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

  saveQueue() {
    localStorage.setItem('vibra_queue_v1', JSON.stringify({
      queue: this.queue,
      currentIndex: this.currentIndex
    }));
  }

  addToQueue(track) {
    if (!track) return;
    this.queue.push(track);
    this.saveQueue();
    renderQueue();
  }

  playNext(track) {
    if (!track) return;
    this.queue.splice(this.currentIndex + 1, 0, track);
    this.saveQueue();
    renderQueue();
  }

  playNow(track) {
    if (!track) return;
    const existingIdx = this.queue.findIndex(t => t.id === track.id);
    if (existingIdx > -1) {
      loadTrack(existingIdx, true);
    } else {
      this.queue.splice(this.currentIndex + 1, 0, track);
      this.saveQueue();
      renderQueue();
      loadTrack(this.currentIndex + 1, true);
    }
  }

  updateTopGenre(genre) {
    if (!genre || genre === 'Online' || genre === 'Local') return;
    if (!this.stats.genreCounts) this.stats.genreCounts = {};
    this.stats.genreCounts[genre] = (this.stats.genreCounts[genre] || 0) + 1;
    let top = 'Synthwave';
    let max = 0;
    for (const [g, count] of Object.entries(this.stats.genreCounts)) {
      if (count > max) {
        max = count;
        top = g;
      }
    }
    this.stats.topGenre = top;
    this.saveStats();
  }
}

const state = new VibraState();

// =============================================================================
// TOAST NOTIFICATIONS SYSTEM
// =============================================================================

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  const icon = type === 'success' ? '✓' : type === 'error' ? '⚠️' : 'ℹ️';
  toast.innerHTML = `<span style="font-weight:700;">${icon}</span> <span>${esc(message)}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 250);
  }, 3200);
}

// =============================================================================
// INDEXEDDB OFFLINE VAULT ($0 Local Storage Engine)
// =============================================================================

const DB_NAME = 'vibra-offline';
const DB_VERSION = 1;
const STORE_NAME = 'tracks';

function openOfflineDB() {
  return new Promise((resolve, reject) => {
    if (!('indexedDB' in window)) {
      return reject(new Error('IndexedDB not supported'));
    }
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = (e) => {
      const db = e.target.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'id' });
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

const OfflineVault = {
  async get(id) {
    try {
      const db = await openOfflineDB();
      return new Promise((resolve) => {
        const tx = db.transaction(STORE_NAME, 'readonly');
        const req = tx.objectStore(STORE_NAME).get(id);
        req.onsuccess = () => resolve(req.result || null);
        req.onerror = () => resolve(null);
      });
    } catch (e) {
      return null;
    }
  },

  async getAll() {
    try {
      const db = await openOfflineDB();
      return new Promise((resolve) => {
        const tx = db.transaction(STORE_NAME, 'readonly');
        const req = tx.objectStore(STORE_NAME).getAll();
        req.onsuccess = () => resolve(req.result || []);
        req.onerror = () => resolve([]);
      });
    } catch (e) {
      return [];
    }
  },

  async save(track, blob) {
    const db = await openOfflineDB();
    const capBytes = (settings.storageCapMB || 500) * 1024 * 1024;

    // Check quota / capacity and evict oldest if needed
    if (capBytes > 0) {
      const all = await this.getAll();
      let currentTotal = all.reduce((sum, item) => sum + (item.size || 0), 0);
      if (currentTotal + blob.size > capBytes) {
        all.sort((a, b) => (a.addedAt || 0) - (b.addedAt || 0));
        while (all.length > 0 && currentTotal + blob.size > capBytes) {
          const evicted = all.shift();
          await this.delete(evicted.id);
          currentTotal -= evicted.size || 0;
        }
      }
    }

    // Persist storage anti-eviction
    if (navigator.storage && navigator.storage.persist) {
      navigator.storage.persist().catch(() => {});
    }

    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readwrite');
      const store = tx.objectStore(STORE_NAME);
      const record = {
        id: track.id,
        meta: {
          title: track.title,
          artist: track.artist,
          album: track.album || 'Single',
          genre: track.genre || 'Music',
          duration: track.duration || 180,
          art: track.art,
          source: track.source || 'local',
          videoId: track.videoId || null,
          isPreview: Boolean(track.isPreview)
        },
        blob,
        size: blob.size,
        addedAt: Date.now()
      };
      const req = store.put(record);
      req.onsuccess = () => resolve(record);
      req.onerror = () => reject(req.error);
    });
  },

  async delete(id) {
    try {
      const db = await openOfflineDB();
      return new Promise((resolve) => {
        const tx = db.transaction(STORE_NAME, 'readwrite');
        tx.objectStore(STORE_NAME).delete(id);
        tx.oncomplete = () => resolve(true);
        tx.onerror = () => resolve(false);
      });
    } catch (e) {
      return false;
    }
  },

  async clearAll() {
    try {
      const db = await openOfflineDB();
      return new Promise((resolve) => {
        const tx = db.transaction(STORE_NAME, 'readwrite');
        tx.objectStore(STORE_NAME).clear();
        tx.oncomplete = () => resolve(true);
        tx.onerror = () => resolve(false);
      });
    } catch (e) {
      return false;
    }
  }
};

// =============================================================================
// PLAYLISTS ENGINE (vibra_playlists_v1 via localStorage)
// =============================================================================

class PlaylistManager {
  constructor() {
    this.playlists = loadJson('vibra_playlists_v1', [
      {
        id: 'pl-seed-focus',
        name: 'Deep Focus & Study Beats',
        desc: 'Curated sounds for programming, flow state, and nocturnal concentration',
        trackIds: ['track-1', 'track-2'],
        createdAt: Date.now() - 86400000
      }
    ]);
    this.activePlaylistId = null;
  }

  save() {
    localStorage.setItem('vibra_playlists_v1', JSON.stringify(this.playlists));
    this.updateBadge();
  }

  updateBadge() {
    const badge = document.getElementById('playlistCountBadge');
    if (badge) badge.textContent = this.playlists.length;
  }

  create(name, desc = '') {
    if (!name || !name.trim()) return null;
    const pl = {
      id: `pl-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      name: name.trim(),
      desc: desc.trim(),
      trackIds: [],
      createdAt: Date.now()
    };
    this.playlists.unshift(pl);
    this.save();
    return pl;
  }

  rename(id, newName) {
    const pl = this.playlists.find(p => p.id === id);
    if (pl && newName && newName.trim()) {
      pl.name = newName.trim();
      this.save();
      return true;
    }
    return false;
  }

  delete(id) {
    const idx = this.playlists.findIndex(p => p.id === id);
    if (idx > -1) {
      this.playlists.splice(idx, 1);
      this.save();
      return true;
    }
    return false;
  }

  addTrack(playlistId, trackId) {
    const pl = this.playlists.find(p => p.id === playlistId);
    if (!pl) return false;
    if (!pl.trackIds.includes(trackId)) {
      pl.trackIds.push(trackId);
      this.save();
      return true;
    }
    return false;
  }

  removeTrack(playlistId, trackId) {
    const pl = this.playlists.find(p => p.id === playlistId);
    if (!pl) return false;
    const idx = pl.trackIds.indexOf(trackId);
    if (idx > -1) {
      pl.trackIds.splice(idx, 1);
      this.save();
      return true;
    }
    return false;
  }
}

const playlistManager = new PlaylistManager();

// =============================================================================
// =============================================================================
// 3. WEB AUDIO API & ADVANCED DSP ENGINE (Phase 2B)
// =============================================================================

let saveSettingsTimeout = null;
function debouncedSaveSettings() {
  clearTimeout(saveSettingsTimeout);
  saveSettingsTimeout = setTimeout(() => {
    saveSettings();
  }, 300);
}

class AudioEngine {
  constructor() {
    this.audioA = document.getElementById('nativeAudio');
    this.audioB = document.getElementById('nativeAudioB');
    this.activeSlot = 'A'; // 'A' or 'B'
    this.isCrossfading = false;

    this.audioCtx = null;
    this.sourceNodeA = null;
    this.sourceNodeB = null;
    this.crossfadeGainA = null;
    this.crossfadeGainB = null;

    // EQ Biquads (B4)
    this.frequencies = [60, 150, 400, 1000, 2400, 15000];
    this.eqBands = [];
    this.eqGains = settings.eq?.gains ? [...settings.eq.gains] : [0, 0, 0, 0, 0, 0];
    this.eqEnabled = settings.eq ? settings.eq.enabled : true;

    // Bass Harmonic Exciter Nodes (B3)
    this.exciterFilter = null;
    this.exciterDrive = null;
    this.exciterShaper = null;
    this.exciterDry = null;
    this.exciterWet = null;
    this.exciterOut = null;

    // M/S Stereo Widener Nodes (B2)
    this.channelSplitter = null;
    this.midBus = null;
    this.sideBus = null;
    this.channelMerger = null;

    // Limiter / Compressor Nodes (B6)
    this.compressorNode = null;
    this.makeupGainNode = null;

    // Analyser & Master Output
    this.analyser = null;
    this.masterGain = null;
    this.isInitialized = false;

    this.initAudioContext = this.initAudioContext.bind(this);
  }

  get audioElement() {
    return this.activeSlot === 'A' ? this.audioA : this.audioB;
  }

  getActiveElement() {
    return this.activeSlot === 'A' ? this.audioA : this.audioB;
  }

  getInactiveElement() {
    return this.activeSlot === 'A' ? this.audioB : this.audioA;
  }

  getActiveGain() {
    return this.activeSlot === 'A' ? this.crossfadeGainA : this.crossfadeGainB;
  }

  getInactiveGain() {
    return this.activeSlot === 'A' ? this.crossfadeGainB : this.crossfadeGainA;
  }

  initAudioContext() {
    if (this.isInitialized) return;

    try {
      const AudioCtxClass = window.AudioContext || window.webkitAudioContext;
      this.audioCtx = new AudioCtxClass();

      // 1. Two-Element Crossfade Source Nodes (B1)
      this.sourceNodeA = this.audioCtx.createMediaElementSource(this.audioA);
      this.sourceNodeB = this.audioCtx.createMediaElementSource(this.audioB);

      this.crossfadeGainA = this.audioCtx.createGain();
      this.crossfadeGainB = this.audioCtx.createGain();
      this.crossfadeGainA.gain.value = 1.0;
      this.crossfadeGainB.gain.value = 0.0;

      this.sourceNodeA.connect(this.crossfadeGainA);
      this.sourceNodeB.connect(this.crossfadeGainB);

      const preEQNode = this.audioCtx.createGain();
      this.crossfadeGainA.connect(preEQNode);
      this.crossfadeGainB.connect(preEQNode);

      // 2. 6-Band Biquad Filter EQ Chain (B4)
      let lastNode = preEQNode;
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
        filter.gain.value = this.eqEnabled ? (this.eqGains[index] || 0) : 0;

        lastNode.connect(filter);
        lastNode = filter;
        return filter;
      });

      const postEQNode = this.audioCtx.createGain();
      lastNode.connect(postEQNode);

      // 3. Bass Harmonic Exciter Tap (B3)
      // postEQ ──┬── dry (gain 1.0) ──────────────────────┐
      //          └── lowpass @180Hz ─► gain 0.6 ─► WaveShaper ─► wet ─┴─► exciterOut
      this.exciterDry = this.audioCtx.createGain();
      this.exciterDry.gain.value = 1.0;

      this.exciterFilter = this.audioCtx.createBiquadFilter();
      this.exciterFilter.type = 'lowpass';
      this.exciterFilter.frequency.value = 180;

      this.exciterDrive = this.audioCtx.createGain();
      this.exciterDrive.gain.value = 0.6;

      this.exciterShaper = this.audioCtx.createWaveShaper();
      const n_samples = 2048;
      const curve = new Float32Array(n_samples);
      const norm = Math.tanh(3);
      for (let i = 0; i < n_samples; ++i) {
        const x = (i * 2) / n_samples - 1;
        curve[i] = Math.tanh(x * 3) / norm;
      }
      this.exciterShaper.curve = curve;
      this.exciterShaper.oversample = '4x';

      this.exciterWet = this.audioCtx.createGain();
      const initExciterMix = settings.exciterEnabled ? (settings.exciterMix || 0.15) : 0.0;
      this.exciterWet.gain.value = initExciterMix;

      this.exciterOut = this.audioCtx.createGain();

      postEQNode.connect(this.exciterDry);
      this.exciterDry.connect(this.exciterOut);

      postEQNode.connect(this.exciterFilter);
      this.exciterFilter.connect(this.exciterDrive);
      this.exciterDrive.connect(this.exciterShaper);
      this.exciterShaper.connect(this.exciterWet);
      this.exciterWet.connect(this.exciterOut);

      // 4. M/S Stereo Widener Matrix (B2)
      // ChannelSplitter(2) ─► [mid/side matrix] ─► ChannelMerger(2)
      this.channelSplitter = this.audioCtx.createChannelSplitter(2);
      this.exciterOut.connect(this.channelSplitter);

      // Mid bus: 0.5 * L + 0.5 * R
      const midGainL = this.audioCtx.createGain();
      const midGainR = this.audioCtx.createGain();
      midGainL.gain.value = 0.5;
      midGainR.gain.value = 0.5;
      this.midBus = this.audioCtx.createGain();
      this.midBus.gain.value = 1.0;
      this.channelSplitter.connect(midGainL, 0);
      this.channelSplitter.connect(midGainR, 1);
      midGainL.connect(this.midBus);
      midGainR.connect(this.midBus);

      // Side bus: 0.5 * L - 0.5 * R multiplied by width
      const sideGainL = this.audioCtx.createGain();
      const sideGainR = this.audioCtx.createGain();
      sideGainL.gain.value = 0.5;
      sideGainR.gain.value = -0.5;
      this.sideBus = this.audioCtx.createGain();
      const initWidth = settings.spatialEnabled ? (settings.spatialWidth || 1.4) : 1.0;
      this.sideBus.gain.value = initWidth;
      this.channelSplitter.connect(sideGainL, 0);
      this.channelSplitter.connect(sideGainR, 1);
      sideGainL.connect(this.sideBus);
      sideGainR.connect(this.sideBus);

      // Reconstruction: L' = Mid + Side, R' = Mid - Side
      const outL_Mid = this.audioCtx.createGain();
      const outL_Side = this.audioCtx.createGain();
      const outR_Mid = this.audioCtx.createGain();
      const outR_Side = this.audioCtx.createGain();
      outL_Mid.gain.value = 1.0;
      outL_Side.gain.value = 1.0;
      outR_Mid.gain.value = 1.0;
      outR_Side.gain.value = -1.0;

      this.midBus.connect(outL_Mid);
      this.sideBus.connect(outL_Side);
      this.midBus.connect(outR_Mid);
      this.sideBus.connect(outR_Side);

      this.channelMerger = this.audioCtx.createChannelMerger(2);
      outL_Mid.connect(this.channelMerger, 0, 0);
      outL_Side.connect(this.channelMerger, 0, 0);
      outR_Mid.connect(this.channelMerger, 0, 1);
      outR_Side.connect(this.channelMerger, 0, 1);

      // 5. Dynamics Compressor / Limiter (B6)
      this.compressorNode = this.audioCtx.createDynamicsCompressor();
      this.compressorNode.threshold.value = settings.normalize ? -6 : 0;
      this.compressorNode.knee.value = 6;
      this.compressorNode.ratio.value = settings.normalize ? 12 : 1;
      this.compressorNode.attack.value = 0.003;
      this.compressorNode.release.value = 0.25;

      this.makeupGainNode = this.audioCtx.createGain();
      this.makeupGainNode.gain.value = settings.normalize ? 1.35 : 1.0;

      this.channelMerger.connect(this.compressorNode);
      this.compressorNode.connect(this.makeupGainNode);

      // 6. Analyser & Master Gain Node
      this.analyser = this.audioCtx.createAnalyser();
      this.analyser.fftSize = 256;
      this.analyser.smoothingTimeConstant = 0.85;

      this.masterGain = this.audioCtx.createGain();
      this.masterGain.gain.value = state.volume;

      this.makeupGainNode.connect(this.analyser);
      this.analyser.connect(this.masterGain);
      this.masterGain.connect(this.audioCtx.destination);

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

  setVolume(vol) {
    if (this.masterGain && this.audioCtx) {
      this.masterGain.gain.setTargetAtTime(vol, this.audioCtx.currentTime, 0.02);
    }
    this.audioA.volume = vol;
    this.audioB.volume = vol;
  }

  setGain(bandIndex, dbValue) {
    this.eqGains[bandIndex] = dbValue;
    if (settings.eq) {
      settings.eq.gains[bandIndex] = dbValue;
      settings.eq.preset = 'custom';
      debouncedSaveSettings();
    }
    if (this.eqBands[bandIndex] && this.audioCtx) {
      this.eqBands[bandIndex].gain.setTargetAtTime(this.eqEnabled ? dbValue : 0, this.audioCtx.currentTime, 0.05);
    }
  }

  setEqEnabled(enabled) {
    this.eqEnabled = enabled;
    if (settings.eq) {
      settings.eq.enabled = enabled;
      debouncedSaveSettings();
    }
    if (this.audioCtx) {
      this.eqBands.forEach((band, idx) => {
        band.gain.setTargetAtTime(enabled ? (this.eqGains[idx] || 0) : 0, this.audioCtx.currentTime, 0.05);
      });
    }
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
    this.eqGains = [...gains];
    if (settings.eq) {
      settings.eq.gains = [...gains];
      settings.eq.preset = presetName;
      debouncedSaveSettings();
    }

    gains.forEach((g, idx) => {
      if (this.eqBands[idx] && this.audioCtx) {
        this.eqBands[idx].gain.setTargetAtTime(this.eqEnabled ? g : 0, this.audioCtx.currentTime, 0.05);
      }
      const slider = document.getElementById(['eq60', 'eq150', 'eq400', 'eq1k', 'eq2k4', 'eq15k'][idx]);
      const label = document.getElementById(['gainVal60', 'gainVal150', 'gainVal400', 'gainVal1k', 'gainVal2k4', 'gainVal15k'][idx]);
      if (slider) slider.value = g;
      if (label) label.textContent = `${g > 0 ? '+' : ''}${g} dB`;
    });
  }

  // B2: M/S Stereo Widener controls
  setSpatialEnabled(enabled) {
    settings.spatialEnabled = enabled;
    saveSettings();
    if (this.sideBus && this.audioCtx) {
      const targetWidth = enabled ? (settings.spatialWidth || 1.4) : 1.0;
      this.sideBus.gain.setTargetAtTime(targetWidth, this.audioCtx.currentTime, 0.05);
    }
  }

  setSpatialWidth(width) {
    settings.spatialWidth = width;
    saveSettings();
    if (this.sideBus && this.audioCtx && settings.spatialEnabled) {
      this.sideBus.gain.setTargetAtTime(width, this.audioCtx.currentTime, 0.05);
    }
  }

  // B3: Bass Harmonic Exciter controls
  setExciterEnabled(enabled) {
    settings.exciterEnabled = enabled;
    saveSettings();
    if (this.exciterWet && this.audioCtx) {
      const targetMix = enabled ? (settings.exciterMix || 0.15) : 0.0;
      this.exciterWet.gain.setTargetAtTime(targetMix, this.audioCtx.currentTime, 0.05);
    }
  }

  setExciterMix(mix) {
    settings.exciterMix = mix;
    saveSettings();
    if (this.exciterWet && this.audioCtx && settings.exciterEnabled) {
      this.exciterWet.gain.setTargetAtTime(mix, this.audioCtx.currentTime, 0.05);
    }
  }

  // B6: Limiter / Normalize controls
  setNormalize(enabled) {
    settings.normalize = enabled;
    saveSettings();
    if (this.compressorNode && this.makeupGainNode && this.audioCtx) {
      const now = this.audioCtx.currentTime;
      this.compressorNode.threshold.setTargetAtTime(enabled ? -6 : 0, now, 0.05);
      this.compressorNode.ratio.setTargetAtTime(enabled ? 12 : 1, now, 0.05);
      this.makeupGainNode.gain.setTargetAtTime(enabled ? 1.35 : 1.0, now, 0.05);
    }
  }

  // B5: Two-Element Crossfade
  async triggerCrossfade(nextIndex) {
    if (this.isCrossfading || settings.crossfadeSec <= 0) return false;
    if (state.repeatMode === 'one') return false;
    if (nextIndex < 0 || nextIndex >= state.queue.length) return false;

    this.initAudioContext();
    this.resume();

    const nextTrack = state.queue[nextIndex];
    if (!nextTrack) return false;

    this.isCrossfading = true;
    const fadeSec = Math.max(1, settings.crossfadeSec);
    const nextSlot = this.activeSlot === 'A' ? 'B' : 'A';
    const incomingAudio = nextSlot === 'A' ? this.audioA : this.audioB;
    const outgoingAudio = this.activeSlot === 'A' ? this.audioA : this.audioB;
    const incomingGain = nextSlot === 'A' ? this.crossfadeGainA : this.crossfadeGainB;
    const outgoingGain = this.activeSlot === 'A' ? this.crossfadeGainA : this.crossfadeGainB;

    const streamUrl = await resolveStream(nextTrack);
    if (!streamUrl) {
      this.isCrossfading = false;
      return false;
    }

    incomingAudio.src = streamUrl;
    incomingAudio.load();

    const now = this.audioCtx.currentTime;
    incomingGain.gain.cancelScheduledValues(now);
    outgoingGain.gain.cancelScheduledValues(now);

    incomingGain.gain.setValueAtTime(0, now);
    outgoingGain.gain.setValueAtTime(1, now);

    try {
      await incomingAudio.play();
      incomingGain.gain.linearRampToValueAtTime(1.0, now + fadeSec);
      outgoingGain.gain.linearRampToValueAtTime(0.0, now + fadeSec);

      this.activeSlot = nextSlot;
      state.currentIndex = nextIndex;
      state.saveQueue();
      renderQueue();
      updatePlaybackUI();
      updateActiveCards();
      updateActiveTrackUI(nextTrack);

      setTimeout(() => {
        outgoingAudio.pause();
        outgoingAudio.currentTime = 0;
        if (this.audioCtx) {
          incomingGain.gain.setValueAtTime(1.0, this.audioCtx.currentTime);
          outgoingGain.gain.setValueAtTime(0.0, this.audioCtx.currentTime);
        }
        this.isCrossfading = false;
      }, fadeSec * 1000);

      return true;
    } catch (e) {
      console.warn('Crossfade play error:', e);
      this.isCrossfading = false;
      return false;
    }
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
    this.offscreenCap = null;
    this.cachedCapWidth = 0;
    this.capPad = 12;

    this.resizeCanvas();
    window.addEventListener('resize', () => this.resizeCanvas());
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        if (this.animationId) cancelAnimationFrame(this.animationId);
      } else {
        this.start();
      }
    });
  }

  resizeCanvas() {
    if (!this.canvas) return;
    const rect = this.canvas.getBoundingClientRect();
    if (!rect.width || !rect.height) return;
    const dpr = window.devicePixelRatio || 1;
    this.canvas.width = rect.width * dpr;
    this.canvas.height = rect.height * dpr;
    this.ctx.scale(dpr, dpr);
    this.cachedCapWidth = 0; // invalidate cached offscreen cap
  }

  initOffscreenCap(barWidth) {
    if (this.offscreenCap && this.cachedCapWidth === barWidth) return;
    this.offscreenCap = document.createElement('canvas');
    const pad = 12;
    this.offscreenCap.width = Math.ceil(barWidth + pad * 2);
    this.offscreenCap.height = Math.ceil(4 + pad * 2);
    const octx = this.offscreenCap.getContext('2d');

    // Single pre-rendered shadow glow
    octx.shadowColor = '#00f0ff';
    octx.shadowBlur = 8;
    octx.fillStyle = '#ffffff';
    octx.fillRect(pad, pad, barWidth, 2);

    this.cachedCapWidth = barWidth;
    this.capPad = pad;
  }

  start() {
    if (this.animationId) cancelAnimationFrame(this.animationId);
    const render = () => {
      const visView = document.getElementById('view-visualizer');
      const expandedModal = document.getElementById('expandedPlayerModal');
      const isVisible = (visView && visView.classList.contains('active')) ||
                        (expandedModal && expandedModal.classList.contains('open'));

      if (!document.hidden && isVisible) {
        this.draw();
      }
      this.animationId = requestAnimationFrame(render);
    };
    this.animationId = requestAnimationFrame(render);
  }

  draw() {
    if (!this.canvas) return;
    const width = this.canvas.offsetWidth;
    const height = this.canvas.offsetHeight;
    if (!width || !height) return;
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
      // Ambient floating rhythm when paused
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

    // Ensure offscreen glowing bar cap is initialized once
    this.initOffscreenCap(barWidth);

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

      // Blit offscreen glowing cap: ~10x cheaper than shadowBlur per bar
      if (this.offscreenCap) {
        this.ctx.drawImage(this.offscreenCap, x - this.capPad, y - 3 - this.capPad);
      }
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
        ${esc(line.text)}
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

async function loadTrack(index, autoPlay = true) {
  if (index < 0 || index >= state.queue.length) return;

  if (audio.isCrossfading) {
    audio.isCrossfading = false;
    const inactiveAudio = audio.getInactiveElement();
    inactiveAudio.pause();
    inactiveAudio.currentTime = 0;
    if (audio.audioCtx) {
      const now = audio.audioCtx.currentTime;
      audio.getActiveGain()?.gain.cancelScheduledValues(now);
      audio.getActiveGain()?.gain.setValueAtTime(1.0, now);
      audio.getInactiveGain()?.gain.cancelScheduledValues(now);
      audio.getInactiveGain()?.gain.setValueAtTime(0.0, now);
    }
  }

  state.currentIndex = index;
  state.saveQueue();
  const track = state.getCurrentTrack();

  const streamUrl = await resolveStream(track);
  if (streamUrl) {
    audio.audioElement.src = streamUrl;
    audio.audioElement.load();
  }

  // Restore mid-song position if resuming from refresh
  if (!autoPlay) {
    const savedPos = parseFloat(localStorage.getItem('vibra_position') || '0');
    if (savedPos > 0) {
      const applyPos = () => {
        try {
          if (audio.audioElement.duration && savedPos < audio.audioElement.duration) {
            audio.audioElement.currentTime = savedPos;
          }
        } catch (e) {}
      };
      audio.audioElement.addEventListener('loadedmetadata', applyPos, { once: true });
      const dur = track.duration || 180;
      const curLabel = document.getElementById('currentTimeLabel');
      const durLabel = document.getElementById('durationTimeLabel');
      const seekProg = document.getElementById('seekProgress');
      const seekTh = document.getElementById('seekThumb');
      if (curLabel) curLabel.textContent = formatTime(savedPos);
      if (durLabel) durLabel.textContent = formatTime(dur);
      if (seekProg && seekTh) {
        const pct = Math.min(100, (savedPos / dur) * 100);
        seekProg.style.width = `${pct}%`;
        seekTh.style.left = `${pct}%`;
      }
    }
  } else {
    localStorage.removeItem('vibra_position');
  }

  // Update UI Elements
  updateActiveTrackUI(track);

  // Update Queue & Grid active cards
  renderQueue();
  updateActiveCards();

  if (autoPlay) {
    playTrack();
  }
}

function updateActiveTrackUI(track) {
  if (!track) return;
  const trackTitleEl = document.getElementById('playerTrackTitle');
  if (trackTitleEl) {
    if (track.isPreview) {
      trackTitleEl.innerHTML = `${esc(track.title)} <span class="preview-badge">Preview 30s</span>`;
    } else {
      trackTitleEl.textContent = track.title;
    }
  }
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
  if (settings.lyricsProvider !== 'off') {
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
  } else {
    lyricsEngine.parsedLines = [];
    lyricsEngine.renderTo('lyricsScrollBox');
    lyricsEngine.renderTo('expandedLyricsScroll');
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
}

function playTrack() {
  audio.initAudioContext();
  audio.resume();

  audio.getActiveElement().play().then(() => {
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
  audio.getActiveElement().pause();
  if (audio.isCrossfading) {
    audio.getInactiveElement().pause();
  }
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
// 7. MULTI-SOURCE SEARCH ENGINE & STREAM RESOLVER
// =============================================================================

const resolvedStreamCache = new Map();

async function resolveStream(track) {
  if (!track) return null;

  // 1. Check Offline Vault first (Airplane mode & local offline playback)
  try {
    const offlineRecord = await OfflineVault.get(track.id);
    if (offlineRecord && offlineRecord.blob) {
      return URL.createObjectURL(offlineRecord.blob);
    }
  } catch (e) {
    console.warn('Offline vault check error:', e);
  }

  if (track.audioUrl) return track.audioUrl;

  if (track.source === 'ytmusic' && track.videoId) {
    if (resolvedStreamCache.has(track.videoId)) {
      const cached = resolvedStreamCache.get(track.videoId);
      if (Date.now() < cached.expiresAt) return cached.url;
    }

    if (settings.apiBase) {
      try {
        const res = await fetch(`${settings.apiBase}/api/stream?id=${encodeURIComponent(track.videoId)}&q=${settings.quality}`, {
          signal: AbortSignal.timeout(settings.timeoutMs)
        });
        if (res.ok) {
          const data = await res.json();
          if (data.url) {
            resolvedStreamCache.set(track.videoId, {
              url: data.url,
              expiresAt: data.expiresAt || (Date.now() + 3.8 * 3600 * 1000)
            });
            return data.url;
          }
        }
      } catch (e) {
        console.warn('Worker stream resolve failed:', e);
      }
    }
    // Fallback: Piped audio stream
    return `https://pipedapi.kavin.rocks/streams/${track.videoId}`;
  }

  if (track.source === 'audius') {
    const rawId = track.id.replace('audius-', '');
    return `https://discoveryprovider.audius.co/v1/tracks/${rawId}/stream?app_name=Vibra`;
  }

  return track.audioUrl || null;
}

const ADAPTERS = {
  // 1. iTunes Search API (Free, CORS-open, official 30s AAC previews, no key required)
  itunes: async (q, cfg) => {
    const limit = cfg.resultLimit || 25;
    const res = await fetch(`https://itunes.apple.com/search?term=${encodeURIComponent(q)}&media=music&entity=song&limit=${limit}`, {
      signal: AbortSignal.timeout(cfg.timeoutMs)
    });
    if (!res.ok) throw new Error(`iTunes HTTP ${res.status}`);
    const data = await res.json();
    if (!data.results) return [];

    return data.results.map(item => {
      const art = (item.artworkUrl100 || '').replace('100x100bb', '600x600bb') || 'assets/covers/cyberpunk.jpg';
      return {
        id: `itunes-${item.trackId}`,
        source: 'itunes',
        title: item.trackName || 'Unknown Title',
        artist: item.artistName || 'Unknown Artist',
        album: item.collectionName || 'Single',
        genre: item.primaryGenreName || 'Music',
        duration: Math.round((item.trackTimeMillis || 30000) / 1000),
        art,
        audioUrl: item.previewUrl,
        isPreview: true,
        lyricsOk: true
      };
    });
  },

  // 2. Audius API (Free, CORS-open, full streaming tracks, no key required)
  audius: async (q, cfg) => {
    const res = await fetch(`https://discoveryprovider.audius.co/v1/tracks/search?query=${encodeURIComponent(q)}&app_name=Vibra`, {
      signal: AbortSignal.timeout(cfg.timeoutMs)
    });
    if (!res.ok) throw new Error(`Audius HTTP ${res.status}`);
    const data = await res.json();
    const tracks = data.data || [];

    return tracks.slice(0, cfg.resultLimit || 25).map(item => {
      const art = item.artwork?.['480x480'] || item.artwork?.['150x150'] || 'assets/covers/cosmic.jpg';
      return {
        id: `audius-${item.id}`,
        source: 'audius',
        title: item.title || 'Unknown Title',
        artist: item.user?.name || 'Audius Creator',
        album: item.mood ? `Vibe: ${item.mood}` : 'Audius Release',
        genre: item.genre || 'Electronic',
        duration: item.duration || 180,
        art,
        audioUrl: `https://discoveryprovider.audius.co/v1/tracks/${item.id}/stream?app_name=Vibra`,
        isPreview: false,
        lyricsOk: true
      };
    });
  },

  // 3. Jamendo API (Free public client ID, CC full audio streams)
  jamendo: async (q, cfg) => {
    const limit = cfg.resultLimit || 25;
    const res = await fetch(`https://api.jamendo.com/v3.0/tracks/?client_id=56d30c95&format=jsonpretty&limit=${limit}&search=${encodeURIComponent(q)}&audioformat=mp32`, {
      signal: AbortSignal.timeout(cfg.timeoutMs)
    });
    if (!res.ok) throw new Error(`Jamendo HTTP ${res.status}`);
    const data = await res.json();
    if (data.headers && data.headers.status === 'failed') {
      throw new Error(`Jamendo API: ${data.headers.error_message || 'Application suspended'}`);
    }
    const tracks = data.results || [];

    return tracks.map(item => ({
      id: `jamendo-${item.id}`,
      source: 'jamendo',
      title: item.name || 'Unknown Track',
      artist: item.artist_name || 'Jamendo Artist',
      album: item.album_name || 'Jamendo Album',
      genre: item.musicinfo?.tags?.genres?.[0] || 'Indie',
      duration: item.duration || 180,
      art: item.album_image || item.image || 'assets/covers/lofi.jpg',
      audioUrl: item.audio,
      isPreview: false,
      lyricsOk: true
    }));
  },

  // 4. YouTube Music (InnerTube via Cloudflare Worker)
  ytmusic: async (q, cfg) => {
    if (!cfg.apiBase) {
      throw new Error('Worker API Base URL not configured. Switch source or enter URL in Settings.');
    }
    const limit = cfg.resultLimit || 25;
    const res = await fetch(`${cfg.apiBase}/api/search?q=${encodeURIComponent(q)}&limit=${limit}`, {
      signal: AbortSignal.timeout(cfg.timeoutMs)
    });
    if (!res.ok) throw new Error(`Worker HTTP ${res.status}`);
    const tracks = await res.json();
    return tracks;
  },

  // 5. Deezer (via Worker proxy)
  deezer: async (q, cfg) => {
    if (!cfg.apiBase) {
      throw new Error('Worker API Base URL required for Deezer CORS proxy.');
    }
    const res = await fetch(`${cfg.apiBase}/api/deezer?q=${encodeURIComponent(q)}&limit=${cfg.resultLimit || 25}`, {
      signal: AbortSignal.timeout(cfg.timeoutMs)
    });
    if (!res.ok) throw new Error(`Deezer Proxy HTTP ${res.status}`);
    const data = await res.json();
    return (data.data || []).map(item => ({
      id: `deezer-${item.id}`,
      source: 'deezer',
      title: item.title,
      artist: item.artist?.name || 'Unknown Artist',
      album: item.album?.title || 'Deezer Track',
      genre: 'Music',
      duration: item.duration || 30,
      art: (item.album?.cover_medium || '').replace('250x250', '500x500') || 'assets/covers/cyberpunk.jpg',
      audioUrl: item.preview,
      isPreview: true,
      lyricsOk: true
    }));
  },

  // 6. Local Library Filter
  local: async (q) => {
    const term = q.toLowerCase();
    return state.playlist.filter(t =>
      (t.title && t.title.toLowerCase().includes(term)) ||
      (t.artist && t.artist.toLowerCase().includes(term)) ||
      (t.genre && t.genre.toLowerCase().includes(term))
    );
  }
};

const Sources = {
  async search(query) {
    const s = settings;
    const active = s.activeSource || 'itunes';
    const fallback = s.fallbackSource || 'audius';

    if (ADAPTERS[active]) {
      try {
        const results = await ADAPTERS[active](query, s);
        if (Array.isArray(results) && results.length > 0) return results;
      } catch (err) {
        console.warn(`Source "${active}" failed, seamlessly failing over to "${fallback}":`, err);
      }
    }

    if (ADAPTERS[fallback] && fallback !== active) {
      try {
        const fallbackResults = await ADAPTERS[fallback](query, s);
        if (Array.isArray(fallbackResults) && fallbackResults.length > 0) return fallbackResults;
      } catch (fErr) {
        console.warn(`Fallback source "${fallback}" also failed:`, fErr);
      }
    }

    return ADAPTERS.local(query, s);
  }
};

let currentSearchToken = 0;

async function searchOnlineMusic(query) {
  if (!query || !query.trim()) {
    state.searchResults = [];
    renderTrackGrid(state.playlist);
    return;
  }

  const token = ++currentSearchToken;
  const trackGrid = document.getElementById('trackGrid');
  const sourceLabel = (settings.activeSource || 'iTunes').toUpperCase();

  trackGrid.innerHTML = `
    <div style="grid-column: 1 / -1; text-align: center; padding: 48px;">
      <div class="search-shimmer-loader">
        <div class="spinner-ring"></div>
        <div style="font-size: 15px; color: var(--accent-primary); margin-top: 14px;">Searching ${esc(sourceLabel)} for "${esc(query)}"...</div>
      </div>
    </div>`;

  try {
    const results = await Sources.search(query);
    if (token !== currentSearchToken) return; // Discard stale responses

    state.searchResults = results || [];
    state.searchResults.forEach(t => state.registerTrack(t));
    if (state.searchResults.length > 0) {
      renderTrackGrid(state.searchResults);
    } else {
      trackGrid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 48px; color: var(--text-dim);">
          <p style="font-size: 16px;">No tracks found on ${esc(sourceLabel)} for "${esc(query)}".</p>
          <span style="font-size: 13px; margin-top: 6px; display: block;">Try another search term or switch source in Settings.</span>
        </div>`;
    }
  } catch (e) {
    if (token !== currentSearchToken) return;
    trackGrid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 48px; color: var(--accent-primary);">
        <p style="font-size: 16px;">Search error on ${esc(sourceLabel)}</p>
        <span style="font-size: 13px; margin-top: 6px; display: block; color: var(--text-dim);">${esc(e.message || 'Please check your connection')}</span>
      </div>`;
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

      state.registerTrack(newTrack);
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

function renderTrackGrid(tracksToRender = (state.searchResults && state.searchResults.length > 0 ? state.searchResults : state.playlist)) {
  const grid = document.getElementById('trackGrid');
  document.getElementById('trackCount').textContent = tracksToRender.length;

  grid.innerHTML = tracksToRender.map((t, idx) => {
    const isPlayingThis = state.isPlaying && state.getCurrentTrack().id === t.id;
    const isLiked = state.favorites.includes(t.id);

    return `
      <div class="track-card ${isPlayingThis ? 'active-playing' : ''}" data-id="${esc(t.id)}" data-index="${idx}">
        <div class="track-art-box">
          <img src="${esc(t.art)}" alt="${esc(t.title)}" loading="lazy" decoding="async">
          <div class="track-play-badge">
            <svg viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
          </div>
        </div>
        <div class="track-meta-title" title="${esc(t.title)}">${esc(t.title)}</div>
        <div class="track-meta-artist">${esc(t.artist)}</div>
        <div class="track-bottom-row">
          <div style="display:flex; align-items:center; gap:6px;">
            <span class="track-genre-tag">${esc(t.genre)}</span>
            ${t.isPreview ? '<span class="preview-badge">30s</span>' : ''}
          </div>
          <div class="track-actions-group">
            <button class="card-action-btn btn-play-next" data-id="${esc(t.id)}" title="Play Next">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="5 4 15 12 5 20 5 4"></polygon><line x1="19" y1="5" x2="19" y2="19"></line></svg>
            </button>
            <button class="card-action-btn btn-add-queue" data-id="${esc(t.id)}" title="Add to Queue">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
            </button>
            <button class="card-action-btn btn-add-playlist" data-id="${esc(t.id)}" title="Add to Playlist">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 11h-6V5h-2v6H5v2h6v6h2v-6h6z"></path></svg>
            </button>
            <button class="card-action-btn btn-download-track" data-id="${esc(t.id)}" title="Download Offline">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
            </button>
            <button class="card-like-btn ${isLiked ? 'liked' : ''}" data-id="${esc(t.id)}" title="Favorite">
              <svg viewBox="0 0 24 24" fill="${isLiked ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');

  // Card click triggers
  grid.querySelectorAll('.track-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.card-like-btn') || e.target.closest('.card-action-btn')) return;
      const trackId = card.getAttribute('data-id');
      const track = tracksToRender.find(t => t.id === trackId);
      if (track) {
        state.playNow(track);
      }
    });
  });

  // Play Next triggers
  grid.querySelectorAll('.btn-play-next').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const trackId = btn.getAttribute('data-id');
      const track = tracksToRender.find(t => t.id === trackId);
      if (track) {
        state.playNext(track);
      }
    });
  });

  // Add to Queue triggers
  grid.querySelectorAll('.btn-add-queue').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const trackId = btn.getAttribute('data-id');
      const track = tracksToRender.find(t => t.id === trackId);
      if (track) {
        state.addToQueue(track);
      }
    });
  });

  // Add to Playlist triggers
  grid.querySelectorAll('.btn-add-playlist').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const trackId = btn.getAttribute('data-id');
      const track = tracksToRender.find(t => t.id === trackId);
      if (track) {
        openAddToPlaylistModal(track);
      }
    });
  });

  // Download Offline triggers
  grid.querySelectorAll('.btn-download-track').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const trackId = btn.getAttribute('data-id');
      const track = tracksToRender.find(t => t.id === trackId);
      if (track) {
        downloadTrack(track);
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
    <div class="charts-table-row" data-id="${esc(t.id)}" data-index="${idx}">
      <span class="col-rank">${idx + 1}</span>
      <div class="col-title">
        <img src="${esc(t.art)}" alt="${esc(t.title)}" loading="lazy" decoding="async">
        <div class="table-track-info">
          <div class="table-track-name">${esc(t.title)}</div>
          <div class="table-track-artist">${esc(t.artist)}</div>
        </div>
      </div>
      <div class="col-album">${esc(t.album || 'Single')}</div>
      <div class="col-genre">${esc(t.genre)}</div>
      <div class="col-time">${formatTime(t.duration)}</div>
      <div class="col-action">
        <button class="table-play-btn" data-id="${esc(t.id)}">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
        </button>
      </div>
    </div>
  `).join('');

  tableBody.querySelectorAll('.charts-table-row').forEach(row => {
    row.addEventListener('click', () => {
      const trackId = row.getAttribute('data-id');
      const track = state.playlist.find(t => t.id === trackId);
      if (track) {
        state.playNow(track);
      }
    });
  });
}

function renderFavoritesGrid() {
  const favGrid = document.getElementById('favoritesGrid');
  if (!favGrid) return;
  const favTracks = state.favorites.map(fid => state.findTrack(fid)).filter(Boolean);
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
    <div class="track-card" data-id="${esc(t.id)}">
      <div class="track-art-box">
        <img src="${esc(t.art)}" alt="${esc(t.title)}" loading="lazy" decoding="async">
        <div class="track-play-badge">
          <svg viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
        </div>
      </div>
      <div class="track-meta-title">${esc(t.title)}</div>
      <div class="track-meta-artist">${esc(t.artist)}</div>
      <div class="track-bottom-row">
        <span class="track-genre-tag">${esc(t.genre)}</span>
        <button class="card-like-btn liked" data-id="${esc(t.id)}">
          <svg viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
        </button>
      </div>
    </div>
  `).join('');

  favGrid.querySelectorAll('.track-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.card-like-btn')) return;
      const trackId = card.getAttribute('data-id');
      const track = state.findTrack(trackId);
      if (track) state.playNow(track);
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
    <div class="queue-item ${idx === state.currentIndex ? 'current-playing' : ''}" data-index="${idx}" data-id="${esc(t.id)}">
      <img src="${esc(t.art)}" alt="${esc(t.title)}" loading="lazy" decoding="async">
      <div class="queue-item-meta">
        <div class="queue-item-title">${esc(t.title)}</div>
        <div class="queue-item-artist">${esc(t.artist)}</div>
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
// PLAYLISTS VIEW CONTROLLER (vibra_playlists_v1)
// =============================================================================

function renderPlaylistsGrid() {
  const grid = document.getElementById('playlistsGrid');
  if (!grid) return;
  playlistManager.updateBadge();

  if (playlistManager.playlists.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 48px; color: var(--text-dim);">
        <p style="font-size: 16px; margin-bottom: 8px;">No custom playlists yet.</p>
        <span style="font-size: 13px;">Click "New Playlist" above to start building your personal library.</span>
      </div>`;
    return;
  }

  grid.innerHTML = playlistManager.playlists.map(pl => {
    let coverArt = 'assets/covers/cyberpunk.jpg';
    if (pl.trackIds.length > 0) {
      const firstTrack = state.findTrack(pl.trackIds[0]);
      if (firstTrack && firstTrack.art) coverArt = firstTrack.art;
    }

    return `
      <div class="playlist-card" data-id="${esc(pl.id)}">
        <div class="playlist-card-art">
          <img src="${esc(coverArt)}" alt="${esc(pl.name)}" loading="lazy" decoding="async">
        </div>
        <div class="playlist-card-title" title="${esc(pl.name)}">${esc(pl.name)}</div>
        <div class="playlist-card-desc">${esc(pl.desc || 'No description provided')}</div>
        <div class="playlist-card-meta">
          <span>${pl.trackIds.length} track${pl.trackIds.length === 1 ? '' : 's'}</span>
          <span>${new Date(pl.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}</span>
        </div>
      </div>
    `;
  }).join('');

  grid.querySelectorAll('.playlist-card').forEach(card => {
    card.addEventListener('click', () => {
      const id = card.getAttribute('data-id');
      openPlaylistDetail(id);
    });
  });
}

function openPlaylistDetail(playlistId) {
  const pl = playlistManager.playlists.find(p => p.id === playlistId);
  if (!pl) return;
  playlistManager.activePlaylistId = playlistId;

  const detailContainer = document.getElementById('playlistDetailContainer');
  const playlistsGridSection = document.getElementById('playlistsGrid')?.parentElement;
  if (!detailContainer) return;

  if (playlistsGridSection) playlistsGridSection.style.display = 'none';
  detailContainer.style.display = 'flex';

  document.getElementById('detailPlaylistName').textContent = pl.name;
  document.getElementById('detailPlaylistDesc').textContent = pl.desc || 'Custom curated Vibra playlist';
  document.getElementById('detailTrackCount').textContent = `${pl.trackIds.length} song${pl.trackIds.length === 1 ? '' : 's'}`;

  // Resolve tracks via global track registry
  const resolvedTracks = [];
  let totalSec = 0;
  for (const tid of pl.trackIds) {
    const t = state.findTrack(tid);
    if (t) {
      resolvedTracks.push(t);
      totalSec += t.duration || 180;
    }
  }

  const mins = Math.ceil(totalSec / 60);
  document.getElementById('detailDuration').textContent = `${mins} min`;

  // Update art collage
  const collageEl = document.getElementById('playlistArtCollage');
  if (collageEl && resolvedTracks.length > 0) {
    collageEl.innerHTML = `<img src="${esc(resolvedTracks[0].art)}" alt="${esc(pl.name)}">`;
  }

  // Render track rows
  const tracksListEl = document.getElementById('playlistTracksList');
  if (!tracksListEl) return;

  if (resolvedTracks.length === 0) {
    tracksListEl.innerHTML = `
      <div style="text-align: center; padding: 36px; color: var(--text-dim);">
        <p style="font-size: 15px;">This playlist is empty.</p>
        <span style="font-size: 13px; display: block; margin-top: 6px;">Add tracks using the "+" button on songs across Vibra.</span>
      </div>`;
    return;
  }

  tracksListEl.innerHTML = resolvedTracks.map((t, idx) => {
    const isPlayingThis = state.isPlaying && state.getCurrentTrack()?.id === t.id;
    return `
      <div class="playlist-track-row ${isPlayingThis ? 'active-playing' : ''}" data-id="${esc(t.id)}">
        <span class="pl-col-idx">${idx + 1}</span>
        <div class="pl-col-art">
          <img src="${esc(t.art)}" alt="${esc(t.title)}" style="width:100%;height:100%;object-fit:cover;border-radius:4px;">
        </div>
        <div class="pl-col-info" style="overflow:hidden;">
          <div style="font-size:14px;font-weight:600;color:var(--text-main);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(t.title)}</div>
          <div style="font-size:12px;color:var(--text-muted);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(t.artist)}</div>
        </div>
        <div class="pl-col-album" style="font-size:12px;color:var(--text-dim);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(t.album || 'Single')}</div>
        <div class="pl-col-dur" style="font-size:12px;color:var(--text-dim);">${formatTime(t.duration)}</div>
        <div class="pl-col-actions" style="display:flex;align-items:center;justify-content:center;">
          <button class="btn-remove-from-playlist" data-id="${esc(t.id)}" title="Remove from playlist" style="background:none;border:none;color:var(--text-dim);cursor:pointer;padding:6px;">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
      </div>
    `;
  }).join('');

  // Row click -> play
  tracksListEl.querySelectorAll('.playlist-track-row').forEach(row => {
    row.addEventListener('click', (e) => {
      if (e.target.closest('.btn-remove-from-playlist')) return;
      const tid = row.getAttribute('data-id');
      const track = resolvedTracks.find(t => t.id === tid);
      if (track) state.playNow(track);
    });
  });

  // Remove from playlist
  tracksListEl.querySelectorAll('.btn-remove-from-playlist').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const tid = btn.getAttribute('data-id');
      playlistManager.removeTrack(pl.id, tid);
      showToast('Track removed from playlist', 'info');
      openPlaylistDetail(pl.id);
    });
  });
}

function closePlaylistDetail() {
  const detailContainer = document.getElementById('playlistDetailContainer');
  const playlistsGridSection = document.getElementById('playlistsGrid')?.parentElement;
  if (detailContainer) detailContainer.style.display = 'none';
  if (playlistsGridSection) playlistsGridSection.style.display = 'block';
  playlistManager.activePlaylistId = null;
  renderPlaylistsGrid();
}

function openAddToPlaylistModal(track) {
  if (!track) return;
  state.pendingTrackForPlaylist = track;
  const modal = document.getElementById('addToPlaylistModal');
  const pickerList = document.getElementById('playlistPickerList');
  const promptEl = document.getElementById('addToPlaylistPrompt');
  if (!modal || !pickerList) return;

  if (promptEl) {
    promptEl.textContent = `Select playlist for "${track.title}":`;
  }

  if (playlistManager.playlists.length === 0) {
    pickerList.innerHTML = `
      <div style="text-align: center; padding: 24px; color: var(--text-dim);">
        <p>No playlists found.</p>
        <button class="btn-primary" id="modalCreateFirstPlaylistBtn" style="margin-top:12px;">Create Playlist</button>
      </div>`;
    const btn = document.getElementById('modalCreateFirstPlaylistBtn');
    if (btn) {
      btn.addEventListener('click', () => {
        modal.style.display = 'none';
        document.getElementById('openCreatePlaylistBtn')?.click();
      });
    }
  } else {
    pickerList.innerHTML = playlistManager.playlists.map(pl => {
      const isAlreadyIn = pl.trackIds.includes(track.id);
      return `
        <div class="playlist-picker-item ${isAlreadyIn ? 'selected' : ''}" data-id="${esc(pl.id)}" style="display:flex;align-items:center;justify-content:space-between;padding:12px;background:rgba(255,255,255,0.04);border-radius:8px;margin-bottom:8px;cursor:pointer;border:1px solid ${isAlreadyIn ? 'var(--accent-primary)' : 'var(--glass-border)'};">
          <div>
            <div style="font-weight:600;font-size:14px;color:var(--text-main);">${esc(pl.name)}</div>
            <div style="font-size:12px;color:var(--text-muted);">${pl.trackIds.length} tracks</div>
          </div>
          <span style="font-size:18px;color:${isAlreadyIn ? 'var(--accent-primary)' : 'var(--text-dim)'};">${isAlreadyIn ? '✓' : '+'}</span>
        </div>
      `;
    }).join('');

    pickerList.querySelectorAll('.playlist-picker-item').forEach(item => {
      item.addEventListener('click', () => {
        const plId = item.getAttribute('data-id');
        const pl = playlistManager.playlists.find(p => p.id === plId);
        if (!pl) return;

        if (pl.trackIds.includes(track.id)) {
          playlistManager.removeTrack(plId, track.id);
          showToast(`Removed from "${pl.name}"`, 'info');
        } else {
          state.registerTrack(track);
          playlistManager.addTrack(plId, track.id);
          showToast(`Added to "${pl.name}"`, 'success');
        }
        openAddToPlaylistModal(track);
      });
    });
  }

  modal.style.display = 'flex';
}

function setupPlaylistEventListeners() {
  const createModal = document.getElementById('createPlaylistModal');
  const addModal = document.getElementById('addToPlaylistModal');
  const openCreateBtn = document.getElementById('openCreatePlaylistBtn');
  const closeCreateBtn = document.getElementById('closeCreatePlaylistModalBtn');
  const cancelCreateBtn = document.getElementById('cancelCreatePlaylistBtn');
  const saveCreateBtn = document.getElementById('saveNewPlaylistBtn');

  if (openCreateBtn && createModal) {
    openCreateBtn.addEventListener('click', () => {
      const nameInput = document.getElementById('newPlaylistNameInput');
      const descInput = document.getElementById('newPlaylistDescInput');
      if (nameInput) nameInput.value = '';
      if (descInput) descInput.value = '';
      createModal.style.display = 'flex';
      setTimeout(() => nameInput?.focus(), 50);
    });
  }

  const closeCreate = () => {
    if (createModal) createModal.style.display = 'none';
  };
  if (closeCreateBtn) closeCreateBtn.addEventListener('click', closeCreate);
  if (cancelCreateBtn) cancelCreateBtn.addEventListener('click', closeCreate);
  if (createModal) {
    createModal.addEventListener('click', (e) => {
      if (e.target === createModal) closeCreate();
    });
  }

  if (saveCreateBtn) {
    saveCreateBtn.addEventListener('click', () => {
      const name = document.getElementById('newPlaylistNameInput')?.value.trim();
      const desc = document.getElementById('newPlaylistDescInput')?.value.trim();
      if (!name) {
        showToast('Please enter a playlist name', 'error');
        return;
      }
      const newPl = playlistManager.create(name, desc);
      if (newPl) {
        closeCreate();
        renderPlaylistsGrid();
        showToast(`Created playlist "${name}"`, 'success');
      }
    });
  }

  // Add to Playlist modal close
  const closeAdd = () => {
    if (addModal) addModal.style.display = 'none';
  };
  const closeAddBtn = document.getElementById('closeAddToPlaylistModalBtn');
  const cancelAddBtn = document.getElementById('cancelAddToPlaylistBtn');
  if (closeAddBtn) closeAddBtn.addEventListener('click', closeAdd);
  if (cancelAddBtn) cancelAddBtn.addEventListener('click', closeAdd);
  if (addModal) {
    addModal.addEventListener('click', (e) => {
      if (e.target === addModal) closeAdd();
    });
  }

  // Detail view buttons
  const backBtn = document.getElementById('backToPlaylistsBtn');
  if (backBtn) backBtn.addEventListener('click', closePlaylistDetail);

  const renameBtn = document.getElementById('renamePlaylistBtn');
  if (renameBtn) {
    renameBtn.addEventListener('click', () => {
      const pl = playlistManager.playlists.find(p => p.id === playlistManager.activePlaylistId);
      if (!pl) return;
      const newName = prompt('Enter new playlist name:', pl.name);
      if (newName && newName.trim() && newName.trim() !== pl.name) {
        playlistManager.rename(pl.id, newName.trim());
        document.getElementById('detailPlaylistName').textContent = newName.trim();
        showToast('Playlist renamed', 'success');
      }
    });
  }

  const deleteBtn = document.getElementById('deletePlaylistBtn');
  if (deleteBtn) {
    deleteBtn.addEventListener('click', () => {
      const pl = playlistManager.playlists.find(p => p.id === playlistManager.activePlaylistId);
      if (!pl) return;
      if (confirm(`Delete playlist "${pl.name}"? This cannot be undone.`)) {
        playlistManager.delete(pl.id);
        closePlaylistDetail();
        showToast(`Deleted "${pl.name}"`, 'info');
      }
    });
  }

  const playAllBtn = document.getElementById('playEntirePlaylistBtn');
  if (playAllBtn) {
    playAllBtn.addEventListener('click', () => {
      const pl = playlistManager.playlists.find(p => p.id === playlistManager.activePlaylistId);
      if (!pl || pl.trackIds.length === 0) {
        showToast('Playlist has no tracks to play', 'info');
        return;
      }
      const tracks = [];
      for (const tid of pl.trackIds) {
        const t = state.findTrack(tid);
        if (t) tracks.push(t);
      }
      if (tracks.length > 0) {
        state.queue = [...tracks];
        state.currentIndex = 0;
        state.saveQueue();
        renderQueue();
        loadTrack(0, true);
        showToast(`Playing playlist "${pl.name}"`, 'success');
      }
    });
  }

  const playerAddBtn = document.getElementById('playerAddPlaylistBtn');
  if (playerAddBtn) {
    playerAddBtn.addEventListener('click', () => {
      const currentTrack = state.getCurrentTrack();
      if (currentTrack) {
        openAddToPlaylistModal(currentTrack);
      }
    });
  }
}

// =============================================================================
// OFFLINE DOWNLOADS VAULT CONTROLLER (vibra-offline IndexedDB)
// =============================================================================

async function downloadTrack(track) {
  if (!track) return;

  try {
    const existing = await OfflineVault.get(track.id);
    if (existing && existing.blob) {
      showToast(`"${track.title}" is already in offline vault`, 'info');
      updateOfflineUI();
      return;
    }

    showToast(`Downloading "${track.title}"...`, 'info');
    const streamUrl = await resolveStream(track);
    if (!streamUrl) {
      showToast('Could not resolve stream URL for offline download', 'error');
      return;
    }

    const res = await fetch(streamUrl);
    if (!res.ok) {
      throw new Error(`Download HTTP ${res.status}`);
    }
    const blob = await res.blob();

    await OfflineVault.save(track, blob);
    showToast(`Downloaded "${track.title}" for offline playback!`, 'success');
    await updateOfflineUI();
  } catch (err) {
    console.error('Offline download error:', err);
    if (err.name === 'QuotaExceededError' || err.code === 22) {
      try {
        const all = await OfflineVault.getAll();
        all.sort((a, b) => (a.addedAt || 0) - (b.addedAt || 0));
        const evictCount = Math.min(5, all.length);
        for (let i = 0; i < evictCount; i++) {
          await OfflineVault.delete(all[i].id);
        }
        showToast('Storage quota reached: cleared oldest downloads to make space', 'info');
        await updateOfflineUI();
      } catch (evictErr) {
        showToast('Storage quota exceeded! Clear old downloads in Settings.', 'error');
      }
    } else {
      showToast(`Download failed: ${err.message || 'Network error'}`, 'error');
    }
  }
}

async function updateOfflineUI() {
  try {
    const records = await OfflineVault.getAll();
    const countBadge = document.getElementById('downloadsCountBadge');
    if (countBadge) countBadge.textContent = records.length;

    const totalBytes = records.reduce((sum, r) => sum + (r.size || 0), 0);
    const totalMB = (totalBytes / (1024 * 1024)).toFixed(1);

    const storageUsage = document.getElementById('storageUsageLabel');
    if (storageUsage) {
      storageUsage.textContent = `${records.length} track${records.length === 1 ? '' : 's'} saved • ${totalMB} MB cached`;
    }

    // Toggle .downloaded on player download button
    const curTrack = state.getCurrentTrack();
    const downloadBtn = document.getElementById('playerDownloadBtn');
    if (downloadBtn && curTrack) {
      const isDownloaded = records.some(r => r.id === curTrack.id);
      downloadBtn.classList.toggle('downloaded', isDownloaded);
      downloadBtn.title = isDownloaded ? 'Downloaded (In Offline Vault)' : 'Download for offline playback';
    }

    renderDownloadsGrid(records);
  } catch (e) {
    console.warn('updateOfflineUI error:', e);
  }
}

function renderDownloadsGrid(records) {
  const grid = document.getElementById('downloadsGrid');
  if (!grid) return;

  if (!records || records.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 48px; color: var(--text-dim);">
        <p style="font-size: 16px; margin-bottom: 8px;">No offline downloads yet.</p>
        <span style="font-size: 13px;">Click the download arrow button on any playing song to save it for 100% offline playback with zero internet.</span>
      </div>`;
    return;
  }

  grid.innerHTML = records.map(r => {
    const meta = r.meta || {};
    const sizeMB = ((r.size || 0) / (1024 * 1024)).toFixed(1);
    const isPlayingThis = state.isPlaying && state.getCurrentTrack()?.id === r.id;

    return `
      <div class="track-card ${isPlayingThis ? 'active-playing' : ''}" data-id="${esc(r.id)}">
        <div class="track-art-box">
          <img src="${esc(meta.art || 'assets/covers/cyberpunk.jpg')}" alt="${esc(meta.title)}" loading="lazy">
          <div class="track-play-badge">
            <svg viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
          </div>
        </div>
        <div class="track-meta-title" title="${esc(meta.title)}">${esc(meta.title)}</div>
        <div class="track-meta-artist">${esc(meta.artist)}</div>
        <div class="track-bottom-row">
          <div style="display:flex;align-items:center;gap:6px;">
            <span class="offline-pill" style="font-size:10px;font-weight:700;background:rgba(0,240,255,0.15);color:var(--accent-secondary);padding:2px 6px;border-radius:4px;">${sizeMB} MB</span>
          </div>
          <div class="track-actions-group">
            <button class="card-action-btn btn-delete-offline" data-id="${esc(r.id)}" title="Delete from Offline Vault" style="color:#ff4757;">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');

  grid.querySelectorAll('.track-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('.btn-delete-offline')) return;
      const tid = card.getAttribute('data-id');
      const record = records.find(r => r.id === tid);
      if (record) {
        const offlineTrack = {
          id: record.id,
          title: record.meta.title,
          artist: record.meta.artist,
          album: record.meta.album,
          genre: record.meta.genre,
          duration: record.meta.duration,
          art: record.meta.art,
          audioUrl: URL.createObjectURL(record.blob),
          isPreview: false
        };
        state.playNow(offlineTrack);
      }
    });
  });

  grid.querySelectorAll('.btn-delete-offline').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.stopPropagation();
      const tid = btn.getAttribute('data-id');
      await OfflineVault.delete(tid);
      showToast('Removed track from offline vault', 'info');
      await updateOfflineUI();
    });
  });
}

function setupOfflineEventListeners() {
  const downloadBtn = document.getElementById('playerDownloadBtn');
  if (downloadBtn) {
    downloadBtn.addEventListener('click', () => {
      const cur = state.getCurrentTrack();
      if (cur) downloadTrack(cur);
    });
  }

  const clearAllBtn = document.getElementById('clearAllDownloadsBtn');
  if (clearAllBtn) {
    clearAllBtn.addEventListener('click', async () => {
      if (confirm('Clear all offline downloaded tracks from your device?')) {
        await OfflineVault.clearAll();
        showToast('All offline downloads cleared', 'info');
        await updateOfflineUI();
      }
    });
  }
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
      if (targetView === 'playlists') renderPlaylistsGrid();
      if (targetView === 'downloads') updateOfflineUI();
    });
  });
}

function setupPlayerEventListeners() {
  document.getElementById('masterPlayBtn').addEventListener('click', togglePlay);
  document.getElementById('heroPlayBtn').addEventListener('click', togglePlay);
  document.getElementById('nextTrackBtn').addEventListener('click', nextTrack);
  document.getElementById('prevTrackBtn').addEventListener('click', prevTrack);

  const handleTimeUpdate = (el, slot) => {
    if (audio.activeSlot !== slot) return;
    const cur = el.currentTime;
    const dur = el.duration || state.getCurrentTrack().duration;

    document.getElementById('currentTimeLabel').textContent = formatTime(cur);
    document.getElementById('durationTimeLabel').textContent = formatTime(dur);

    const pct = dur > 0 ? (cur / dur) * 100 : 0;
    document.getElementById('seekProgress').style.width = `${pct}%`;
    document.getElementById('seekThumb').style.left = `${pct}%`;

    // Buffer Fill line
    const buf = el.buffered;
    if (buf && buf.length > 0 && dur > 0) {
      const bufEnd = buf.end(buf.length - 1);
      const bufPct = Math.min(100, (bufEnd / dur) * 100);
      const seekBufEl = document.getElementById('seekBuffer');
      if (seekBufEl) seekBufEl.style.width = `${bufPct}%`;
    }

    // MediaSession Position State
    if ('mediaSession' in navigator && 'setPositionState' in navigator.mediaSession && dur > 0) {
      try {
        navigator.mediaSession.setPositionState({
          duration: Math.max(0, dur),
          playbackRate: state.playbackRate || 1.0,
          position: Math.min(cur, dur)
        });
      } catch (e) {}
    }

    // B5: Crossfade Trigger Check
    if (settings.crossfadeSec > 0 && dur > 0 && !audio.isCrossfading && state.repeatMode !== 'one') {
      const remaining = dur - cur;
      if (remaining <= settings.crossfadeSec && remaining > 0.4) {
        let nextIndex = state.currentIndex + 1;
        if (nextIndex >= state.queue.length) {
          if (state.repeatMode === 'all') nextIndex = 0;
          else nextIndex = -1;
        }
        if (nextIndex >= 0) {
          audio.triggerCrossfade(nextIndex);
        }
      }
    }

    // Persist position for mid-song refresh restore
    if (state.isPlaying && Math.abs(cur - (state.lastSavedPos || 0)) >= 1) {
      state.lastSavedPos = cur;
      localStorage.setItem('vibra_position', cur.toFixed(1));
    }

    // Synced lyrics update
    lyricsEngine.update(cur, 'lyricsScrollBox');
    lyricsEngine.update(cur, 'expandedLyricsScroll');

    // Real Delta Listening Stats
    if (state.isPlaying) {
      if (state.lastAudioTime !== null && state.lastAudioTime !== undefined) {
        const delta = cur - state.lastAudioTime;
        if (delta > 0 && delta < 2) {
          state.stats.totalSecondsListened += delta;
          if (Math.round(state.stats.totalSecondsListened) % 15 === 0) {
            state.saveStats();
            updateStatsUI();
          }
        }
      }
      state.lastAudioTime = cur;
    }
  };

  const handleEnded = (slot) => {
    if (audio.activeSlot !== slot || audio.isCrossfading) return;
    if (state.sleepTimerMinutes === -1) {
      pauseTrack();
      state.sleepTimerMinutes = 0;
      document.getElementById('sleepTimerBadge').textContent = 'Sleep';
      return;
    }
    nextTrack();
  };

  const handleError = (e, slot) => {
    if (audio.activeSlot !== slot) return;
    console.warn('Playback audio stream error on slot ' + slot + ':', e);
    showToast('Stream error encountered. Skipping to next track...', 'error');
    setTimeout(() => nextTrack(), 1200);
  };

  // Wire both dual-slot audio elements (nativeAudio and nativeAudioB)
  [audio.audioA, audio.audioB].forEach((el, idx) => {
    if (!el) return;
    const slot = idx === 0 ? 'A' : 'B';
    el.addEventListener('timeupdate', () => handleTimeUpdate(el, slot));
    el.addEventListener('ended', () => handleEnded(slot));
    el.addEventListener('error', (e) => handleError(e, slot));
  });

  // Seek Scrubber
  const seekTrack = document.getElementById('seekTrack');
  const seekTooltip = document.getElementById('seekTooltip');

  seekTrack.addEventListener('mousemove', (e) => {
    const rect = seekTrack.getBoundingClientRect();
    const pct = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    const activeEl = audio.getActiveElement();
    const dur = activeEl.duration || state.getCurrentTrack().duration;
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
    const activeEl = audio.getActiveElement();
    const dur = activeEl.duration || state.getCurrentTrack().duration;
    activeEl.currentTime = pct * dur;
  });

  // Volume
  const volumeTrack = document.getElementById('volumeTrack');
  const volumeFill = document.getElementById('volumeFill');
  const volumeMuteBtn = document.getElementById('volumeMuteBtn');

  const updateVol = (vol) => {
    state.volume = Math.max(0, Math.min(1, vol));
    audio.setVolume(state.volume);
    volumeFill.style.width = `${state.volume * 100}%`;
    localStorage.setItem('vibra_volume', state.volume.toString());

    const volHigh = volumeMuteBtn.querySelector('.vol-high');
    const volMuted = volumeMuteBtn.querySelector('.vol-muted');
    if (state.volume === 0 || state.isMuted) {
      if (volHigh) volHigh.style.display = 'none';
      if (volMuted) volMuted.style.display = 'block';
    } else {
      if (volHigh) volHigh.style.display = 'block';
      if (volMuted) volMuted.style.display = 'none';
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
    audio.audioA.muted = state.isMuted;
    audio.audioB.muted = state.isMuted;
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
    if (audio.audioA) audio.audioA.playbackRate = nextSpeed;
    if (audio.audioB) audio.audioB.playbackRate = nextSpeed;
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

  const currentGains = settings.eq?.gains || [0, 0, 0, 0, 0, 0];
  sliders.forEach(s => {
    const el = document.getElementById(s.id);
    const label = document.getElementById(s.valId);
    if (!el || !label) return;
    const initialVal = currentGains[s.idx] !== undefined ? currentGains[s.idx] : 0;
    el.value = initialVal;
    label.textContent = `${initialVal > 0 ? '+' : ''}${initialVal} dB`;

    el.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      label.textContent = `${val > 0 ? '+' : ''}${val} dB`;
      document.querySelectorAll('.eq-preset-btn').forEach(b => b.classList.remove('active'));
      audio.setGain(s.idx, val);
    });
  });

  // EQ Power Toggle
  const eqToggle = document.getElementById('eqPowerToggle');
  if (eqToggle) {
    eqToggle.checked = settings.eq ? settings.eq.enabled : true;
    eqToggle.addEventListener('change', (e) => {
      audio.setEqEnabled(e.target.checked);
    });
  }

  // Preset Buttons
  const activePreset = settings.eq?.preset || 'flat';
  document.querySelectorAll('.eq-preset-btn').forEach(btn => {
    if (btn.getAttribute('data-preset') === activePreset) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
    btn.addEventListener('click', () => {
      document.querySelectorAll('.eq-preset-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const preset = btn.getAttribute('data-preset');
      audio.applyPreset(preset);
    });
  });

  // B2: 3D Spatial Stereo Width Toggle
  const spatialToggle = document.getElementById('spatialAudioToggle');
  if (spatialToggle) {
    spatialToggle.checked = !!settings.spatialEnabled;
    spatialToggle.addEventListener('change', (e) => {
      audio.setSpatialEnabled(e.target.checked);
    });
  }

  // B3: Ultra Bass Harmonic Exciter Toggle
  const bassToggle = document.getElementById('bassHarmonicToggle');
  if (bassToggle) {
    bassToggle.checked = !!settings.exciterEnabled;
    bassToggle.addEventListener('change', (e) => {
      audio.setExciterEnabled(e.target.checked);
    });
  }
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
        searchOnlineMusic(q);
      } else {
        state.searchResults = [];
        renderTrackGrid(state.playlist);
      }
    }, 400);
  });

  clearBtn.addEventListener('click', () => {
    searchInput.value = '';
    clearBtn.style.display = 'none';
    state.searchResults = [];
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

  if (menuBtn && menu) {
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
        settings.theme = theme;
        saveSettings();
        localStorage.setItem('vibra_theme', theme);

        document.querySelectorAll('.theme-opt').forEach(o => o.classList.remove('active'));
        opt.classList.add('active');
        if (label) label.textContent = opt.textContent.split(' ')[1] || opt.textContent;

        const themeSel = document.getElementById('themeSel');
        if (themeSel) themeSel.value = theme;
      });
    });
  }

  // Set initial theme
  document.documentElement.setAttribute('data-theme', state.currentTheme);
}

function setupSleepTimer() {
  const sleepBtn = document.getElementById('sleepTimerBtn');
  const modal = document.getElementById('sleepModalBackdrop');
  const closeBtn = document.getElementById('closeSleepModalBtn');

  if (!sleepBtn || !modal) return;

  sleepBtn.addEventListener('click', () => modal.style.display = 'flex');
  if (closeBtn) closeBtn.addEventListener('click', () => modal.style.display = 'none');
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
    if (['input', 'textarea', 'select'].includes(document.activeElement?.tagName.toLowerCase())) return;

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
    } else if (e.key === 's' || e.key === 'S') {
      document.getElementById('shuffleBtn').click();
    } else if (e.key === 'r' || e.key === 'R') {
      document.getElementById('repeatBtn').click();
    } else if (e.key === ',') {
      const settingsBtn = document.getElementById('navSettingsBtn');
      if (settingsBtn) settingsBtn.click();
    }
  });
}

// =============================================================================
// SETTINGS VIEW CONTROLLER (Hot-Apply Multi-Source Settings Engine)
// =============================================================================

function setupSettingsControls() {
  // 1. Online Music Source Radios
  const sourceRadios = document.querySelectorAll('input[name="src"]');
  sourceRadios.forEach(radio => {
    if (radio.value === settings.activeSource) radio.checked = true;
    radio.addEventListener('change', () => {
      settings.activeSource = radio.value;
      saveSettings();
      // Re-trigger active search with newly selected provider
      const searchInput = document.getElementById('trackSearchInput');
      if (searchInput && searchInput.value.trim()) {
        searchOnlineMusic(searchInput.value.trim());
      }
    });
  });

  // Fallback source dropdown
  const fallbackSel = document.getElementById('fallbackSourceSel');
  if (fallbackSel) {
    fallbackSel.value = settings.fallbackSource;
    fallbackSel.addEventListener('change', (e) => {
      settings.fallbackSource = e.target.value;
      saveSettings();
    });
  }

  // 2. Connection & Worker Proxy
  const apiBaseInput = document.getElementById('apiBaseInput');
  if (apiBaseInput) {
    apiBaseInput.value = settings.apiBase || '';
    apiBaseInput.addEventListener('change', (e) => {
      settings.apiBase = e.target.value.trim().replace(/\/+$/, '');
      saveSettings();
    });
  }

  const timeoutSel = document.getElementById('timeoutSel');
  if (timeoutSel) {
    timeoutSel.value = settings.timeoutMs.toString();
    timeoutSel.addEventListener('change', (e) => {
      settings.timeoutMs = parseInt(e.target.value, 10);
      saveSettings();
    });
  }

  const testConnBtn = document.getElementById('testConnBtn');
  const testConnResult = document.getElementById('testConnResult');
  if (testConnBtn && testConnResult) {
    testConnBtn.addEventListener('click', async () => {
      const base = (apiBaseInput?.value || settings.apiBase || '').trim().replace(/\/+$/, '');
      if (!base) {
        testConnResult.textContent = '⚠️ Enter Worker URL first';
        testConnResult.className = 'test-conn-badge error';
        return;
      }
      testConnResult.textContent = 'Testing connection...';
      testConnResult.className = 'test-conn-badge';

      try {
        const res = await fetch(`${base}/api/health`, { signal: AbortSignal.timeout(settings.timeoutMs) });
        if (res.ok) {
          const data = await res.json();
          testConnResult.textContent = `✅ Online (${data.service || 'Gateway'})`;
          testConnResult.className = 'test-conn-badge success';
        } else {
          testConnResult.textContent = `❌ HTTP ${res.status}`;
          testConnResult.className = 'test-conn-badge error';
        }
      } catch (err) {
        testConnResult.textContent = `❌ ${err.message || 'Offline'}`;
        testConnResult.className = 'test-conn-badge error';
      }
    });
  }

  // 3. Lyrics Options
  const lyricsRadios = document.querySelectorAll('input[name="lyr"]');
  lyricsRadios.forEach(radio => {
    if (radio.value === settings.lyricsProvider) radio.checked = true;
    radio.addEventListener('change', () => {
      settings.lyricsProvider = radio.value;
      saveSettings();
    });
  });

  const lyricsPlainFallback = document.getElementById('lyricsPlainFallback');
  if (lyricsPlainFallback) {
    lyricsPlainFallback.checked = settings.lyricsPlainFallback;
    lyricsPlainFallback.addEventListener('change', (e) => {
      settings.lyricsPlainFallback = e.target.checked;
      saveSettings();
    });
  }

  // 4. Audio Engine & DSP
  const crossfadeRange = document.getElementById('crossfadeRange');
  const crossfadeValLabel = document.getElementById('crossfadeValLabel');
  if (crossfadeRange && crossfadeValLabel) {
    crossfadeRange.value = settings.crossfadeSec;
    crossfadeValLabel.textContent = settings.crossfadeSec;
    crossfadeRange.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      crossfadeValLabel.textContent = val;
      settings.crossfadeSec = val;
      saveSettings();
    });
  }

  const gaplessToggle = document.getElementById('gaplessToggle');
  if (gaplessToggle) {
    gaplessToggle.checked = settings.gapless;
    gaplessToggle.addEventListener('change', (e) => {
      settings.gapless = e.target.checked;
      saveSettings();
    });
  }

  const normalizeToggle = document.getElementById('normalizeToggle');
  if (normalizeToggle) {
    normalizeToggle.checked = settings.normalize;
    normalizeToggle.addEventListener('change', (e) => {
      audio.setNormalize(e.target.checked);
    });
  }

  // B7: Stereo width slider
  const spatialWidthRange = document.getElementById('spatialWidthRange');
  const spatialWidthValLabel = document.getElementById('spatialWidthValLabel');
  if (spatialWidthRange && spatialWidthValLabel) {
    spatialWidthRange.value = settings.spatialWidth ?? 1.4;
    spatialWidthValLabel.textContent = Number(settings.spatialWidth ?? 1.4).toFixed(1);
    spatialWidthRange.addEventListener('input', (e) => {
      const val = parseFloat(e.target.value);
      spatialWidthValLabel.textContent = val.toFixed(1);
      audio.setSpatialWidth(val);
    });
  }

  // B7: Exciter mix slider
  const exciterMixRange = document.getElementById('exciterMixRange');
  const exciterMixValLabel = document.getElementById('exciterMixValLabel');
  if (exciterMixRange && exciterMixValLabel) {
    exciterMixRange.value = settings.exciterMix ?? 0.15;
    exciterMixValLabel.textContent = Math.round(Number(settings.exciterMix ?? 0.15) * 100);
    exciterMixRange.addEventListener('input', (e) => {
      const val = parseFloat(e.target.value);
      exciterMixValLabel.textContent = Math.round(val * 100);
      audio.setExciterMix(val);
    });
  }

  const qualitySel = document.getElementById('qualitySel');
  if (qualitySel) {
    qualitySel.value = settings.quality;
    qualitySel.addEventListener('change', (e) => {
      settings.quality = e.target.value;
      saveSettings();
    });
  }

  // 5. Storage Quotas
  const storageCapSel = document.getElementById('storageCapSel');
  if (storageCapSel) {
    storageCapSel.value = settings.storageCapMB.toString();
    storageCapSel.addEventListener('change', (e) => {
      settings.storageCapMB = parseInt(e.target.value, 10);
      saveSettings();
    });
  }

  const autoCacheToggle = document.getElementById('autoCacheToggle');
  if (autoCacheToggle) {
    autoCacheToggle.checked = settings.autoCache;
    autoCacheToggle.addEventListener('change', (e) => {
      settings.autoCache = e.target.checked;
      saveSettings();
    });
  }

  const persistToggle = document.getElementById('persistToggle');
  if (persistToggle) {
    persistToggle.checked = settings.persistStorage;
    persistToggle.addEventListener('change', async (e) => {
      settings.persistStorage = e.target.checked;
      saveSettings();
      if (settings.persistStorage && navigator.storage && navigator.storage.persist) {
        await navigator.storage.persist().catch(() => {});
      }
    });
  }

  // Update real storage estimate
  const updateStorageEstimate = async () => {
    const line = document.getElementById('storageUsageLine');
    if (!line) return;
    if (navigator.storage && navigator.storage.estimate) {
      try {
        const est = await navigator.storage.estimate();
        const usedMB = ((est.usage || 0) / (1024 * 1024)).toFixed(1);
        const quotaMB = ((est.quota || 0) / (1024 * 1024)).toFixed(0);
        line.textContent = `${usedMB} MB cached / ${quotaMB} MB available`;
      } catch (e) {
        line.textContent = 'Storage estimate active';
      }
    }
  };
  updateStorageEstimate();

  const clearCacheBtn = document.getElementById('clearCacheBtn');
  if (clearCacheBtn) {
    clearCacheBtn.addEventListener('click', async () => {
      if ('caches' in window) {
        const keys = await caches.keys();
        await Promise.all(keys.map(k => caches.delete(k)));
      }
      updateStorageEstimate();
      alert('Local audio cache has been cleared.');
    });
  }

  // 6. Library & Data (Backup/Export/Import)
  const resultLimitSel = document.getElementById('resultLimitSel');
  if (resultLimitSel) {
    resultLimitSel.value = settings.resultLimit.toString();
    resultLimitSel.addEventListener('change', (e) => {
      settings.resultLimit = parseInt(e.target.value, 10);
      saveSettings();
    });
  }

  const exportDataBtn = document.getElementById('exportDataBtn');
  if (exportDataBtn) {
    exportDataBtn.addEventListener('click', () => {
      const backupData = {
        app: 'Vibra',
        version: '2.0.0',
        exportedAt: new Date().toISOString(),
        settings,
        favorites: state.favorites,
        stats: state.stats,
        queue: state.queue,
        playlists: loadJson('vibra_playlists', [])
      };

      const blob = new Blob([JSON.stringify(backupData, null, 2)], { type: 'application/json' });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = `vibra_backup_${new Date().toISOString().slice(0, 10)}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(a.href);
    });
  }

  const importDataBtn = document.getElementById('importDataBtn');
  const importFileInput = document.getElementById('importFileInput');
  if (importDataBtn && importFileInput) {
    importDataBtn.addEventListener('click', () => importFileInput.click());
    importFileInput.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const imported = JSON.parse(event.target.result);
          if (imported.settings) {
            settings = Object.assign({}, DEFAULT_SETTINGS, imported.settings);
            saveSettings();
          }
          if (Array.isArray(imported.favorites)) {
            state.favorites = imported.favorites;
            localStorage.setItem('vibra_favorites', JSON.stringify(state.favorites));
          }
          if (imported.stats) {
            state.stats = imported.stats;
            state.saveStats();
          }
          if (Array.isArray(imported.playlists)) {
            localStorage.setItem('vibra_playlists', JSON.stringify(imported.playlists));
          }
          if (Array.isArray(imported.queue)) {
            state.queue = imported.queue;
            state.saveQueue();
          }
          alert('Vibra library & settings successfully restored!');
          location.reload();
        } catch (err) {
          alert('Failed to parse backup JSON file: ' + err.message);
        }
      };
      reader.readAsText(file);
    });
  }

  const resetAllBtn = document.getElementById('resetAllBtn');
  if (resetAllBtn) {
    resetAllBtn.addEventListener('click', () => {
      if (confirm('Are you sure you want to completely reset all Vibra playlists, favorites, and settings? This cannot be undone.')) {
        localStorage.clear();
        location.reload();
      }
    });
  }

  // 7. Appearance & Visuals
  const themeSel = document.getElementById('themeSel');
  if (themeSel) {
    themeSel.value = state.currentTheme;
    themeSel.addEventListener('change', (e) => {
      const theme = e.target.value;
      document.documentElement.setAttribute('data-theme', theme);
      state.currentTheme = theme;
      settings.theme = theme;
      saveSettings();
      localStorage.setItem('vibra_theme', theme);

      const label = document.getElementById('currentThemeLabel');
      if (label) label.textContent = theme;
    });
  }

  const visModeSel = document.getElementById('visModeSel');
  if (visModeSel) {
    visModeSel.value = settings.visMode;
    visModeSel.addEventListener('change', (e) => {
      settings.visMode = e.target.value;
      saveSettings();
      if (visualizer) visualizer.mode = e.target.value;
      document.querySelectorAll('.vis-mode-btn').forEach(b => {
        b.classList.toggle('active', b.getAttribute('data-vismode') === e.target.value);
      });
    });
  }

  const reduceMotionToggle = document.getElementById('reduceMotionToggle');
  if (reduceMotionToggle) {
    reduceMotionToggle.checked = settings.reduceMotion;
    const applyMotion = (reduce) => {
      if (reduce) {
        document.documentElement.setAttribute('data-reduce-motion', 'true');
      } else {
        document.documentElement.removeAttribute('data-reduce-motion');
      }
    };
    applyMotion(settings.reduceMotion);

    reduceMotionToggle.addEventListener('change', (e) => {
      settings.reduceMotion = e.target.checked;
      saveSettings();
      applyMotion(e.target.checked);
    });
  }
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
  navigator.mediaSession.setActionHandler('seekforward', (details) => {
    const skip = details.seekOffset || 10;
    audio.audioElement.currentTime = Math.min(audio.audioElement.duration || 0, audio.audioElement.currentTime + skip);
  });
  navigator.mediaSession.setActionHandler('seekbackward', (details) => {
    const skip = details.seekOffset || 10;
    audio.audioElement.currentTime = Math.max(0, audio.audioElement.currentTime - skip);
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
  setupSettingsControls();
  setupMediaSessionHandlers();
  setupMobileGestures();
  setupPlaylistEventListeners();
  setupOfflineEventListeners();

  // Initial load: resume persisted track in queue and update library UI
  loadTrack(state.currentIndex, false);
  renderTrackGrid();
  renderFavoritesGrid();
  playlistManager.updateBadge();
  updateOfflineUI();
  updateStatsUI();

  // Register service worker if available
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('sw.js').catch(() => {});
  }
});


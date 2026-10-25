# Vibra 🎵 — Next-Gen Music Streaming & Audio Visualizer

[![Build Android APK](https://github.com/nehalrahamana-maker/Vibra/actions/workflows/build-apk.yml/badge.svg)](https://github.com/nehalrahamana-maker/Vibra/actions/workflows/build-apk.yml)
[![GitHub Release](https://img.shields.io/github/v/release/nehalrahamana-maker/Vibra?color=fa2d48&label=Latest%20APK)](https://github.com/nehalrahamana-maker/Vibra/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An ultra-sleek, modern, and high-fidelity music streaming app & audio visualizer inspired by SimpMusic, Spotify, and Apple Music. Vibra features real-time cloud search, live synchronized karaoke lyrics, a 6-band studio equalizer, and an automated Android APK pipeline.

![Vibra Banner](assets/covers/cyberpunk.jpg)

---

## 📱 Download for Android (.APK)

You can download and install the official Android APK directly on your phone:

1. Go to **[Vibra GitHub Releases](https://github.com/nehalrahamana-maker/Vibra/releases)** (or check the **Actions** tab artifacts).
2. Download **`Vibra-v1.0.0.apk`**.
3. Open the file on your Android device and tap **Install**.

---

## ✨ Features

- 🎧 **Cloud Music Search & Streaming**: Instant search and playback powered by online streaming engines.
- 🎤 **Live Synced Lyrics (LRCLIB)**: Word-for-word and line-by-line synchronized karaoke lyrics with smooth auto-scroll.
- 🎚️ **6-Band Studio Equalizer**: Web Audio API Biquad filter chain (60Hz, 150Hz, 400Hz, 1kHz, 2.4kHz, 15kHz) with presets (*Bass Boost, EDM, Vocal Clarity, Rock, Acoustic*).
- 🌊 **Real-Time Visualizer**: 60 FPS HTML5 Canvas frequency bars, oscilloscope waveforms, and radial pulses.
- 🎨 **4 Adaptive Themes**: Apple Music Glass, Cyberpunk Neon, Material 3 Expressive, and Midnight AMOLED.
- 📂 **Local Audio Import**: Drag and drop your own MP3, WAV, or FLAC files to play through the custom equalizer and visualizer.
- 📱 **Multiplatform**: Native Android APK build + Progressive Web App (PWA) with Windows Media Key integration.

---

## 🛠️ Tech Stack & Architecture

- **Frontend & Audio**: HTML5, Vanilla CSS3 (Custom Glassmorphism), Modern JavaScript (ES6+)
- **Audio Engine**: Web Audio API (`AudioContext`, `BiquadFilterNode`, `AnalyserNode`)
- **Lyrics Provider**: LRCLIB REST API
- **Mobile Packaging**: Android Native WebView Wrapper (Android SDK 34, AndroidX, Java 17)
- **CI/CD Pipeline**: GitHub Actions for automated cloud APK building and GitHub Releases

---

## 💻 Local Development

To run Vibra on your computer:

```bash
# Clone the repository
git clone https://github.com/nehalrahamana-maker/Vibra.git

# Open index.html in your browser or run a local server
Start-Process index.html
```

---

## 👨‍💻 Author

Created with pride by **[Kazi Nehal](https://github.com/nehalrahamana-maker)**.  
Computer Science & Engineering (CSE) Student.

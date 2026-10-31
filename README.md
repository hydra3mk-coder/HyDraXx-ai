# HyDraXx 👁️✨ `v1.0.0-BETA`
> **An intelligent, expressive dark AI companion living inside your Android device.**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Status](https://img.shields.io/badge/Status-BETA%20Early%20Access-orange.svg)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Gemini](https://img.shields.io/badge/AI-Gemini%202.5%20Flash-orange.svg)](https://ai.google.dev)
[![ElevenLabs](https://img.shields.io/badge/Voice-ElevenLabs-black.svg)](https://elevenlabs.io)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Download APK](https://img.shields.io/badge/Download-APK%20(v1.0.0--beta)-brightgreen?logo=android)](release/HyDraXx-v1.0.0-beta.apk)

---

## 📲 Direct APK Download (Android)

You can download the pre-built APK directly from the repository and install it immediately on your phone:

📥 **[HyDraXx-v1.0.0-beta.apk](./release/HyDraXx-v1.0.0-beta.apk)** (~23 MB)

> **Installation note**: Since this is a direct APK build (side-loaded), enable *"Install unknown apps"* in your Android Settings when prompted by your file manager.

---

## 🌟 Overview

**HyDraXx** is not an ordinary chatbot. It is a living, expressive AI companion designed to live natively inside your Android phone. Featuring an OLED pitch-black aesthetic with glowing expressive eyes, HyDraXx listens, responds in real-time, learns your personal preferences, falls asleep when idle, and interacts directly with your device's apps and hardware.

---

## 🚀 Key Features

* **👁️ Living Animated Expressions & Audio Reactivity**:
  * Procedurally animated glowing eyes supporting 9 distinct moods (*Neutral, Happy, Curious, Surprised, Focused, Love, Tired, Sleeping, Peeking*).
  * **Real-Time Voice Reactivity**: Microphonic RMS volume dynamically pulses and ripples the eyes as you speak.
  * Interactive petting: Stroke or drag across HyDraXx's eyes for playful blushing, hearts, and giggle reactions.

* **🏝️ Expandable Dynamic Island**:
  * Summons automatically when you say **"Hey Hydra"** or tap the island chip.
  * Sits at the top of the display and springs open with live waveform visualizers and speech transcription.

* **💤 Autonomous Idle Sleep Engine**:
  * Naturally progresses through curiosity, yawning/drowsiness, and peaceful deep sleep (`z Z` floating particles) when left idle.
  * Wakes up instantly on touch or voice input.

* **🧠 Dual-Brain Intelligence (Gemini + Offline Smart Brain)**:
  * **Gemini 2.5 Flash**: Deep contextual reasoning, long-term memory injection, and phone action planning.
  * **On-Device Offline Brain**: High-speed local NLP executing device actions with zero latency and full privacy.

* **🎙️ Lifelike ElevenLabs Voice Engine**:
  * Supports high-fidelity ElevenLabs voice profiles (*Bella, Elli, Rachel, Antoni, Josh*) alongside Android Text-To-Speech with Arabic and English fluency.

* **📱 Real Android Phone Controls**:
  * **App Launcher**: Searches and launches installed apps (YouTube, WhatsApp, Camera, Maps, etc.).
  * **Hardware Toggles**: Camera flashlight, ringer modes (*Normal, Vibrate, Silent*), timers, alarms, and dialer.
  * **Live Telemetry**: Real-time battery percentage, charging state, free RAM, and storage.

* **💾 Permanent Memory & Notes (Room Database)**:
  * Persistent storage for learned user facts and voice to-do tasks.

* **⚡ Default Assistant Integration & Floating Presence**:
  * Configured as a selectable Android **Default Digital Assistant App** (`ACTION_ASSIST`).
  * Miniature floating screen overlay (`SYSTEM_ALERT_WINDOW`) accessible anywhere.

---

## 🛠️ Tech Stack & Architecture

* **Language**: Kotlin (100%)
* **UI**: Jetpack Compose + Material Design 3 (OLED Dark Palette)
* **Architecture**: Clean Architecture / MVVM with Kotlin Coroutines & StateFlow
* **Database**: Room Database with TypeConverters
* **AI Engine**: Direct HTTP integration with Gemini 2.5 Flash API
* **Speech & Audio**: Android `SpeechRecognizer`, `TextToSpeech`, ElevenLabs REST API + `MediaPlayer`

---

## 📦 How to Build & Run

### Prerequisites
* Android Studio Ladybug or newer
* Android SDK 36 (minSdk 24)
* JDK 17 / 21

### Building from Source
```bash
# Clone the repository
git clone https://github.com/your-username/hydraxx-android.git
cd hydraxx-android

# Build Debug APK
./gradlew assembleDebug

# Run unit and Robolectric tests
./gradlew testDebugUnitTest
```

---

## 🔑 Configuration (Optional)

Configure your API keys in `.env` or directly inside the in-app **Companion Settings** sheet:
```env
# Optional: Gemini API Key for cloud intelligence
GEMINI_API_KEY=your_gemini_api_key_here
```

---

## 📄 License
This project is open-source under the MIT License.

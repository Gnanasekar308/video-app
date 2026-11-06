# VideoPlayer

Standalone, local-only Android video player (package `com.videoplayer.app`) with a Private Vault and a Background Music ON/OFF option. Premium dark UI with **light blue, white and black**.

> Build: open in Android Studio (or run `./gradlew assembleDebug`, Gradle wrapper 8.7 included).

## Features
- Home: Continue Watching, Recent Videos, Favorites, Folders, Vault shortcut, Search, Settings
- Bottom navigation: Home · Library · Vault · Settings
- Local video discovery (MediaStore), folder browsing, filename search, sort by name/date/duration/size, thumbnails
- Player: play/pause, ±10s, previous/next, seek bar, speed (0.5–2.0x), aspect modes (Fit/Fill/Original/Zoom), fullscreen, rotation, resume
- Gestures: double-tap seek, brightness (left), volume (right), horizontal seek, tap to toggle controls, lock mode
- Subtitles (embedded + external SRT, on/off, language, size) and audio track selection
- Background Music ON/OFF (audio-track switching architecture; limited by design)
- Favorites, history, Continue Watching, local playlists
- Private Vault: PIN, biometric placeholder, import, encrypted-storage architecture, hidden from library, vault playback, export with re-auth, auto-lock on background
- No cloud, AI services, casting, streaming or downloading (no INTERNET permission)

## Folder structure
```
VideoPlayer/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── app/
│   ├── build.gradle.kts, proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/ (values: colors, strings, themes · drawable · mipmap)
│       └── java/com/videoplayer/app/
│           ├── MainActivity.kt, VideoPlayerApp.kt
│           ├── data/{model,local,repository}
│           ├── viewmodel/        LibraryViewModel, VaultViewModel
│           ├── player/           PlayerManager, TrackHelper, BackgroundMusicController, PlayerModels
│           ├── vault/            VaultManager, VaultCrypto, PinStore, BiometricHelper, VaultSession
│           ├── ui/{theme,components,navigation,home,library,vault,settings,player}
│           └── utils/            Formatters, Permissions
└── docs/PLAYER_UI_STATES.md
```

## Modules
- **data** – models, SharedPreferences-backed `LocalStore`, `VideoRepository` (MediaStore), `UserDataRepository`
- **player** – Media3/ExoPlayer wrapper, track selection, external SRT, music controller
- **vault** – PIN hashing, AES-GCM (Keystore) storage, biometric helper, session lock
- **ui** – Jetpack Compose screens and player UI (controls, settings sheet, gesture guide)

## Current limitations
- Not built or tested
- Background music removal only works when a dialogue-focused audio track exists; no DSP/ML separation yet (extension point: `BackgroundMusicController`)
- Vault: PIN hashing is basic; encryption is whole-file; playback decrypts to cache (needs a streaming encrypted DataSource); export re-auth dialog and biometric button are TODO
- Playlists and Search have repository/viewmodel support, with limited dedicated UI
- Persistence uses SharedPreferences (swap for Room for large libraries)
- Picture-in-picture and background audio service are not implemented

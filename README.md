<p align="center">
  <img src="core/design-system/src/main/ic_launcher-playstore.png" width="120" alt="Santoro icon" />
</p>

<h1 align="center">Santoro</h1>

<p align="center">
  Your personal movie companion — discover, track, and organize your film journey.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI-FF6F00" />
</p>

---

## ✨ Features

| | Feature | Description |
|---|---|---|
| 🔍 | **Discover** | Browse trending, popular, top rated, upcoming, and genre-filtered movies |
| 📄 | **See All** | Full-screen paginated grids with infinite scroll for each browse section |
| 🎬 | **Movie Detail** | Cast, crew, tagline, ratings, runtime — all in one screen |
| ✅ | **Watched** | Track what you've seen with stats dashboard (total, hours, streaks) |
| 📌 | **Watchlist** | Save movies for later, swipe to remove |
| 🔎 | **Smart Search** | Real-time search with recent queries and trending suggestions |
| 🔗 | **Deep Links** | Open TMDB movie URLs directly in the app |
| ☁️ | **Cloud Sync** | Sign in with Google to sync your lists across devices |
| 📴 | **Offline Ready** | Browse cached data and manage lists without internet |
| 🔄 | **Pull to Refresh** | Swipe down to refresh any screen |
| 🎨 | **Theming** | Light, dark, and system theme support |
| 🌍 | **i18n** | English and Spanish |
| 📳 | **Haptic Feedback** | Tactile response on key actions |

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose · Material 3 |
| Architecture | Clean Architecture · MVI · Multi-module |
| DI | Hilt |
| Async | Coroutines · StateFlow |
| Network | Retrofit · OkHttp · Coil 3 |
| Local | Room · DataStore |
| Auth & Cloud | Firebase Auth · Firestore · Crashlytics · Analytics |
| Build | Gradle KTS · Version Catalogs · Convention Plugins |
| Testing | JUnit 5 · MockK · Kluent · Turbine · Paparazzi |

## 📸 Screenshots

<p align="center">
  <img src="docs/screenshots/browse.png" width="180" alt="Browse" />
  <img src="docs/screenshots/movie-detail.png" width="180" alt="Movie Detail" />
  <img src="docs/screenshots/watchlist.png" width="180" alt="Watchlist" />
  <img src="docs/screenshots/watched-stats.png" width="180" alt="Watched Stats" />
</p>

<p align="center">
  <img src="docs/screenshots/search.png" width="180" alt="Search" />
  <img src="docs/screenshots/settings.png" width="180" alt="Settings" />
</p>

## 📦 Module Structure

```
app/                    → Application entry point
feature/                → Feature modules (api + impl per feature)
  ├── search-movies/
  ├── movie-detail/
  ├── watchlist/
  ├── watched-movies/
  ├── settings/
  └── login/
core/                   → Shared modules
  ├── design-system/    → Theme, components, icons
  ├── domain/           → Models, Result type
  ├── data/             → Repositories, mappers  ├── database/         → Room DB, schemas
  ├── network/          → Retrofit setup, interceptors
  ├── ui/               → Shared UI utilities
  ├── auth/             → Firebase Auth wrapper
  ├── sync/             → Firestore sync logic
  ├── build-config/     → Build-time constants
  └── string-resources/ → Centralized i18n strings
library/                → Reusable library modules
  ├── remote-config/    → Firebase Remote Config
  └── secure-storage/   → Encrypted key-value store
build-logic/            → Convention plugins (Gradle)
```

## 🔑 API

This product uses the [TMDB API](https://www.themoviedb.org/) but is not endorsed or certified by TMDB.

<p align="center">
  <a href="https://www.themoviedb.org/">
    <img src="https://www.themoviedb.org/assets/2/v4/logos/v2/blue_short-8e7b30f73a4020692ccca9c88bafe5dcb6f8a62a4c6bc55cd9ba82bb2cd95f6c.svg" width="200" alt="TMDB Logo" />
  </a>
</p>

API key is managed via Firebase Remote Config.

## 📄 License

This project is licensed under the [MIT License](LICENSE).

Documentation (`docs/`) is licensed under [CC BY 4.0](docs/LICENSE).

---

<p align="center">
  Made with 🖤 by <a href="https://github.com/asensiodev">@asensiodev</a>
</p>

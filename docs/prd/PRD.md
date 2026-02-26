# Product Requirements Document — Santoro

| Field        | Value                          |
|--------------|--------------------------------|
| **Version**  | 1.2                            |
| **Status**   | ✅ Current                     |
| **Date**     | 2026-02-26                     |
| **Author**   | @asensiodev                    |
| **Platform** | Android (Native)               |

---

## 1. Product Overview

**Santoro** is a native Android application that lets users discover, track, and organize movies they want to watch or have already watched. It connects to The Movie Database (TMDB) API to surface real-time movie data and stores user lists locally, with optional cloud sync via Firebase.

### 1.1 Vision

A minimal, polished personal movie companion — fast to browse, friction-free to track.

### 1.2 Target Audience

- Casual movie-goers who want a lightweight tracker.
- Users who prefer a local-first experience with optional account sync.

---

## 2. Goals & Non-Goals

### Goals
- Allow users to **discover** movies by popularity, genre, trends, and free-text search.
- Allow users to **track** movies as Watched or add them to a personal Watchlist.
- Allow users to **view rich detail** (cast, crew, genres, runtime, rating) for any movie.
- Provide a **frictionless onboarding** via anonymous sign-in with an optional Google account upgrade.
- Keep the app **functional offline** for personal lists (local DB).

### Non-Goals (v1.0)
- Social features (sharing, friends, public lists).
- Custom user-created lists beyond Watched + Watchlist.
- Movie ratings/reviews authored by the user.
- TV Shows / Episodes.
- Push notifications.

---

## 3. Features

### 3.1 Authentication (F-01)

| Attribute       | Detail                                                     |
|-----------------|------------------------------------------------------------|
| **Entry point** | Login screen shown on first launch                         |
| **Options**     | Sign in with Google · Continue as Guest (anonymous)        |
| **Persistence** | Firebase Auth session persisted across app restarts        |
| **Upgrade**     | Anonymous user can link a Google account from Profile      |
| **Conflict**    | Account collision dialog when Google account already exists|
| **Sign-out**    | Available from Settings                                    |

**User flows:**
1. First launch → Login screen → `Sign in with Google` → authenticated → Home.
2. First launch → Login screen → `Continue as Guest` → anonymous session → Home.
3. Settings → Sign out → Login screen.
4. Profile → `Link Google Account` → Google sign-in → account merged.

**States handled:** Loading · Success · Error (banner) · Account collision dialog.

---

### 3.2 Discover / Search Movies (F-02)

Entry: bottom navigation tab "Search" (Home tab).

#### 3.2.1 Default / Browse mode (no query)
When the search field is empty the screen shows curated sections:

| Section            | Source              |
|--------------------|---------------------|
| Now Playing        | TMDB `/now_playing` |
| Trending           | TMDB `/trending`    |
| Popular            | TMDB `/popular` (paginated, infinite scroll) |
| Top Rated          | TMDB `/top_rated`   |
| Upcoming           | TMDB `/upcoming`    |
| By Genre (× 12)    | TMDB `/discover` filtered by genre ID |

**Genre chips** available: Action · Comedy · Horror · Animation · Documentary · Drama · History · Music · Mystery · Sci-Fi · Thriller · Western.

Hero carousel (M3 `HorizontalMultiBrowseCarousel`) shows Now Playing movies at the top.

#### 3.2.2 Search mode (query typed)
- Real-time search as user types.
- Results displayed in a grid (2 columns).
- Results can be further filtered by the genre chip selection.
- Paginated with infinite scroll (`loadMoreSearchedMovies`).
- Empty state when no results found.

**States handled:** Loading · Content · Error (retry) · Empty.

---

### 3.3 Movie Detail (F-03)

Accessible by tapping any movie card from Search, Watchlist, or Watched.

#### Information displayed
| Section          | Content                                            |
|------------------|----------------------------------------------------|
| Hero              | Backdrop image with gradient overlay, title, tagline |
| Meta              | Release date · Runtime · Production country · TMDB rating + vote count |
| Genres            | Chip row (tappable, decorative)                   |
| Overview          | Full plot summary                                  |
| Cast              | Horizontal scroll list (photo + name + character) |
| Key Crew          | Director, Producer, Screenplay (horizontal scroll)|

#### User actions
| Action             | Description                                                 |
|--------------------|-------------------------------------------------------------|
| **Mark as Watched** | Toggles `isWatched` flag, records `watchedAt` timestamp. Animated icon. |
| **Add to Watchlist** | Toggles `isInWatchlist` flag. Animated icon.               |
| **Back**            | Top-bar back navigation.                                    |

Both state flags are persisted in the local Room database and reflected across all screens in real time.

**States handled:** Loading · Content · Error.

---

### 3.4 Watchlist (F-04)

Entry: bottom navigation tab "Watchlist".

- Lists all movies the user has added to their watchlist.
- **Search** bar filters locally by title.
- Each item shows: poster thumbnail · title · release year · rating.
- Tap navigates to Movie Detail.
- *(Planned)* Swipe-to-remove / explicit remove action.

**States handled:** Loading · Content · Error (retry) · Empty.

---

### 3.5 Watched Movies (F-05)

Entry: bottom navigation tab "Watched".

- Lists all movies the user has marked as watched.
- Grouped by **month/year** of the `watchedAt` timestamp.
- **Summary card** at the top: total number of movies watched.
- **Search** bar filters locally by title.
- Grid layout (2 columns per group section).
- Tap navigates to Movie Detail.

**States handled:** Loading · Content · Error (retry) · Empty.

---

### 3.6 Profile (F-06)

Entry: bottom navigation tab "Profile" (or Settings → Profile).

- Displays user avatar, display name, and email.
- Shows **linked account status** (Google or Anonymous).
- Anonymous users see a prompt to **link a Google account**.
- `Link Google Account` action triggers Google One Tap / Sign-In flow.
- Success triggers a **bottom sheet** confirmation.
- Account collision (email already linked to another UID) triggers an **alert dialog** with merge/cancel option.
- Links to App Settings screen.
- *(Planned)* Help / Support.

**States handled:** Loading · Content · Error (retry banner) · Anonymous mode.

---

### 3.7 Settings (F-07)

Entry: Profile → App Settings.

- App version displayed at the bottom.
- **Sign out** action (with loading overlay).
- *(Planned)* Appearance (theme toggle — Light/Dark/System).
- *(Planned)* Language selection.

---

## 4. Architecture & Tech Stack

| Layer         | Technology                                |
|---------------|-------------------------------------------|
| Language      | Kotlin (latest stable)                    |
| UI            | Jetpack Compose + Material 3              |
| Architecture  | Clean Architecture · Vertical Slicing     |
| DI            | Hilt                                      |
| Async         | Coroutines + StateFlow                    |
| Local storage | Room (single `movies` table)              |
| Network       | Retrofit + OkHttp                         |
| Auth          | Firebase Authentication                   |
| Remote config | Firebase Remote Config (TMDB API key)     |
| Secure storage| EncryptedSharedPreferences                |
| Image loading | Coil 3                                    |
| Build system  | Gradle KTS + Version Catalogs             |
| Modularisation| Multi-module (feature/api + feature/impl + core/* + library/*) |

---

## 5. Data Model (Domain)

```
Movie
  id, title, overview, posterPath, backdropPath, releaseDate
  popularity, voteAverage, voteCount
  genres: List<Genre>
  productionCountries: List<ProductionCountry>
  cast: List<CastMember>
  crew: List<CrewMember>
  runtime: Int?
  director: String?
  isWatched: Boolean
  isInWatchlist: Boolean
  watchedAt: Long?

SantoroUser
  uid, email, displayName, photoUrl, isAnonymous
```

---

## 6. Navigation

Bottom Navigation Bar (4 tabs):

| Tab      | Route              | Icon        |
|----------|--------------------|-------------|
| Search   | SearchMoviesRoute  | Search      |
| Watchlist| WatchlistRoute     | Bookmarks   |
| Watched  | WatchedMoviesRoute | Checkmark   |
| Profile  | SettingsRoute      | Person      |

Modal navigation:
- Any movie card → `MovieDetailRoute(movieId: Int)`
- Profile → `AppSettingsRoute`

---

## 7. UX & Design Principles

- **No blank screens.** All screens handle Loading / Content / Error / Empty states explicitly.
- **Retry mechanisms** on all error states.
- **Local-first:** Watchlist and Watched operate from local DB; network is additive.
- **Animations:** State transitions on Watched/Watchlist toggles use animated icons.
- **Dark + Light** theme supported via Material 3 dynamic color.

---

## 8. Open Items / Known Gaps (v1.0)

| ID   | Item                                              | Priority |
|------|---------------------------------------------------|----------|
| F-04 | Remove from Watchlist (swipe or button) not wired | High     |
| F-10 | Appearance / theme toggle not implemented         | Medium   |
| F-11 | Language selector not implemented                 | Low      |
| F-12 | Help / Support screen not implemented             | Low      |
| F-13 | No cloud sync for Watched / Watchlist lists       | Medium   |
| F-14 | No offline cache for movie browsing data          | Medium   |

---

## 9. Planned Features (Backlog)

Features approved for a future release. Each will get a PRP before implementation starts.

### F-08 — Pull-to-Refresh (Browse & Search)

| Attribute   | Detail |
|-------------|--------|
| **Scope**   | Search/Browse screen (`SearchMoviesScreen`) |
| **Trigger** | User pulls down on the browse content or search results list |
| **Behaviour** | Forces a full cache bypass for the current view — re-fetches all curated sections (or current search query) from TMDB, updates Room cache, and hides the offline banner if previously shown |
| **Rationale** | Currently the cache expires naturally via TTL (30 min curated / 5 min search). Pull-to-refresh gives the user explicit control to get fresh data on demand |
| **States** | Refresh indicator visible during fetch · content replaces inline · Error toast if network unavailable (stale data remains) |
| **Notes** | Implemented via `PullToRefreshBox` (M3). Invalidates cache for current sections via `BrowseCacheLocalDataSource.clearSection()` before re-fetching |

### F-09 — MVI Architecture Migration

| Attribute   | Detail |
|-------------|--------|
| **Scope**   | All feature ViewModels across the project |
| **Pattern** | MVI — `Intent → State → Effect` replacing the current `StateFlow + direct update` approach |
| **Rationale** | As the app grows, the current ViewModel pattern mixes state updates with side-effect logic. MVI enforces a single unidirectional data flow, making state transitions deterministic and testable |
| **Key changes** | Introduce `UiIntent` sealed classes per feature · ViewModels expose a single `process(intent: UiIntent)` entry point · One-time side effects (navigation, toasts) handled via a dedicated `UiEffect` channel (`Channel<UiEffect>`) · `UiState` becomes the sole source of truth |
| **Migration strategy** | Feature-by-feature migration. Start with `search-movies` (most complex), then `movie-detail`, `watchlist`, `watched-movies`, `settings`. No big-bang rewrite |
| **Scope of PRP** | One PRP per feature module migrated, or a single umbrella PRP with one phase per feature |
| **Notes** | No new external library required. Pattern implemented with plain Kotlin `sealed interface` + `Channel`. Existing test patterns (GIVEN/WHEN/THEN) remain unchanged |

---

## 10. Version History

| Version | Date       | Summary                        |
|---------|------------|--------------------------------|
| 1.0     | 2026-02-25 | Initial PRD — baseline feature set documented |
| 1.1     | 2026-02-26 | Add F-08 Pull-to-Refresh and F-09 MVI Migration to planned backlog |
| 1.2     | 2026-02-26 | Unify G-XX gap IDs into F-XX sequence (G-01→F-04, G-02→F-10, G-03→F-11, G-04→F-12, G-05→F-13, G-06→F-14) |

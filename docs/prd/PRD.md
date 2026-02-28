# Product Requirements Document — Santoro

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field        | Value                          |
|--------------|--------------------------------|
| **Version**  | 1.4                            |
| **Status**   | ✅ Current                     |
| **Date**     | 2026-02-28                     |
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
| **Status**      | ✅ Shipped                                                 |
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

| Attribute | Detail |
|-----------|--------|
| **Status** | ✅ Shipped |

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

**Genre chips** available: Action · Comedy · Horror · Animation · Documentary · Drama · History · Music · Mystery · Sci-Fi �� Thriller · Western.

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

| Attribute | Detail |
|-----------|--------|
| **Status** | ✅ Shipped — share action added via [FIP-002](../plan/FIP-002-share-movie.md) |

Accessible by tapping any movie card from Search, Watchlist, or Watched.

#### Information displayed
| Section          | Content                                            |
|------------------|-----------------------------------------------------|
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

| Attribute | Detail |
|-----------|--------|
| **Status** | ✅ Shipped — swipe-to-remove added via [FIP-001](../plan/FIP-001-watchlist-remove.md) |

Entry: bottom navigation tab "Watchlist".

- Lists all movies the user has added to their watchlist.
- **Search** bar filters locally by title.
- Each item shows: poster thumbnail · title · release year · rating.
- Tap navigates to Movie Detail.
- *(Planned)* Swipe-to-remove / explicit remove action.

**States handled:** Loading · Content · Error (retry) · Empty.

---

### 3.5 Watched Movies (F-05)

| Attribute | Detail |
|-----------|--------|
| **Status** | ✅ Shipped |

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

| Attribute | Detail |
|-----------|--------|
| **Status** | ✅ Shipped |

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

| Attribute | Detail |
|-----------|--------|
| **Status** | ✅ Shipped — theme toggle via [FIP-005](../plan/FIP-005-theme-toggle.md), language via [FIP-007](../plan/FIP-007-language-selector.md) |

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

| ID   | Item                                              | Priority | Status |
|------|---------------------------------------------------|----------|--------|
| F-04 | Remove from Watchlist (swipe or button) not wired | High     | ✅ Shipped — [FIP-001](../plan/FIP-001-watchlist-remove.md) |
| F-10 | Appearance / theme toggle not implemented         | Medium   | ✅ Shipped — [FIP-005](../plan/FIP-005-theme-toggle.md) |
| F-11 | Language selector not implemented                 | Low      | ✅ Shipped — [FIP-007](../plan/FIP-007-language-selector.md) |
| F-12 | Help / Support screen not implemented             | Low      | 📋 Planned |
| F-13 | No cloud sync for Watched / Watchlist lists       | Medium   | ✅ Shipped — [FIP-003](../plan/FIP-003-firebase-sync.md) |
| F-14 | No offline cache for movie browsing data          | Medium   | ✅ Shipped — [FIP-004](../plan/FIP-004-browse-cache.md) |

---

## 9. Planned Features (Backlog)

Features approved for a future release. Each will get a FIP before implementation starts.

### F-08 — Pull-to-Refresh (Browse & Search)

| Attribute   | Detail |
|-------------|--------|
| **Status**  | 📋 Planned |
| **Scope**   | Search/Browse screen (`SearchMoviesScreen`) |
| **Trigger** | User pulls down on the browse content or search results list |
| **Behaviour** | Forces a full cache bypass for the current view — re-fetches all curated sections (or current search query) from TMDB, updates Room cache, and hides the offline banner if previously shown |
| **Rationale** | Currently the cache expires naturally via TTL (30 min curated / 5 min search). Pull-to-refresh gives the user explicit control to get fresh data on demand |
| **States** | Refresh indicator visible during fetch · content replaces inline · Error toast if network unavailable (stale data remains) |
| **Notes** | Implemented via `PullToRefreshBox` (M3). Invalidates cache for current sections via `BrowseCacheLocalDataSource.clearSection()` before re-fetching |

### F-09 — MVI Architecture Migration

| Attribute   | Detail |
|-------------|--------|
| **Status**  | ✅ Shipped — [FIP-006](../plan/FIP-006-mvi-migration.md) |
| **Scope**   | All feature ViewModels across the project |
| **Pattern** | MVI — `Intent → State → Effect` replacing the current `StateFlow + direct update` approach |
| **Rationale** | As the app grows, the current ViewModel pattern mixes state updates with side-effect logic. MVI enforces a single unidirectional data flow, making state transitions deterministic and testable |
| **Key changes** | Introduce `UiIntent` sealed classes per feature · ViewModels expose a single `process(intent: UiIntent)` entry point · One-time side effects (navigation, toasts) handled via a dedicated `UiEffect` channel (`Channel<UiEffect>`) · `UiState` becomes the sole source of truth |
| **Migration strategy** | Feature-by-feature migration. Start with `search-movies` (most complex), then `movie-detail`, `watchlist`, `watched-movies`, `settings`. No big-bang rewrite |
| **Scope of FIP** | One FIP per feature module migrated, or a single umbrella FIP with one phase per feature |
| **Notes** | No new external library required. Pattern implemented with plain Kotlin `sealed interface` + `Channel`. Existing test patterns (GIVEN/WHEN/THEN) remain unchanged |

### F-15 — Movie Detail: Tagline Display

| Attribute   | Detail |
|-------------|--------|
| **Status**  | ✅ Shipped — [FIP-009](../plan/FIP-009-movie-detail-tagline.md) |
| **Scope**   | `MovieDetailScreen` — `MovieHeaderSection` |
| **Current state** | The PRD spec mentions a tagline in the hero section, but the current implementation only shows title + rating. The `Movie` domain model does not yet include a `tagline` field |
| **Behaviour** | If TMDB returns a non-empty tagline, display it below the title in the hero area in italic style (`MaterialTheme.typography.bodyMedium`, `onSurfaceVariant`). Hidden when empty |
| **Rationale** | The tagline is often the most evocative piece of copy for a film. Apps like Letterboxd and JustWatch display it prominently |
| **Notes** | Requires adding `tagline: String?` to the `Movie` domain model, the Room entity, the TMDB DTO, and the UI model |

### F-16 — Movie Detail: Hardcoded Strings Cleanup

| Attribute   | Detail |
|-------------|--------|
| **Status**  | ✅ Shipped — [FIP-008](../plan/FIP-008-movie-detail-strings-cleanup.md) |
| **Scope**   | `MovieDetailScreen.kt` |
| **Current state** | "Overview", "Watchlist", and "Watched" button labels are hardcoded strings in production Composables — violating the project's no-hardcoded-strings rule |
| **Behaviour** | Move all hardcoded strings to `core/stringresources`. Keys: `movie_detail_section_overview`, `movie_detail_action_watchlist`, `movie_detail_action_watched` |
| **Rationale** | Consistency, i18n readiness, and adherence to project coding standards |
| **Priority** | High — coding standard violation |

### F-17 — Watchlist & Watched: Consistent Movie Card Layout

| Attribute   | Detail |
|-------------|--------|
| **Status**  | ✅ Shipped — [FIP-010](../plan/FIP-010-watched-card-layout.md) |
| **Current state** | Both Watchlist and Watched use the exact same `MovieCard` component (copied code): a `Card` with fixed `height(size128)`, a centered `Column`, and an image sized at `size160` — the poster does not fill the card and no metadata is visible |
| **Behaviour** | Each screen gets a layout appropriate to its purpose: **Watchlist** → horizontal row item (poster + title + year + genre chips); **Watched** → grid card with full-bleed poster + bottom gradient overlay showing title + watched date. Both are purpose-built for their context, not shared |
| **Rationale** | Search = browse catalogue (visual grid, no metadata needed). Watchlist = manage your queue (list rows, metadata helps identify quickly). Watched = remember what you saw (visual grid, poster evokes memory, watched date contextualises). Top-tier apps (Letterboxd, Trakt, JustWatch) use layout patterns tailored to each list's purpose |

### F-18 — Search: Section Headers with "See All" Navigation

| Attribute   | Detail |
|-------------|--------|
| **Status**  | 📋 Planned |
| **Scope**   | `SearchMoviesScreen` — browse mode curated sections |
| **Current state** | Curated sections (Trending, Popular, Top Rated, Upcoming, By Genre) are displayed without a "See All" affordance. Users cannot explore a full paginated list for a specific section |
| **Behaviour** | Add a `See All →` text button next to each section header. Tapping navigates to a new full-screen paginated list (`MovieListRoute`) filtered to that section. The list uses infinite scroll and the same 2-column grid |
| **Rationale** | A common UX pattern in media apps (Netflix, Apple TV+, Letterboxd). Reduces the "I want to browse more of X" friction |
| **Notes** | Requires a new `movie-list` feature module (or reuse of search grid) and additions to the navigation graph |

### F-19 — Movie Detail: User Personal Rating

| Attribute   | Detail |
|-------------|--------|
| **Status**  | 📋 Planned |
| **Scope**   | `MovieDetailScreen` · `Movie` domain model · Room schema |
| **Current state** | Users can only mark a movie as Watched/Watchlist. There is no way to record how much they liked it |
| **Behaviour** | Add a 1–5 star rating widget (half-star optional) below the Watched/Watchlist action row, visible only when `isWatched = true`. Rating is stored locally (`userRating: Float?` on the `Movie` entity). The Watched screen summary card updates to show average personal rating |
| **Rationale** | Personal ratings are the #1 most-requested feature in movie-tracking apps. Enables future features like sorting/filtering by rating |
| **Notes** | Rating is strictly local for v1. Cloud sync (F-13) will include it when implemented. No public review text in scope |

### F-20 — Watched Screen: Richer Stats Dashboard

| Attribute   | Detail |
|-------------|--------|
| **Status**  | 📋 Planned |
| **Scope**   | `WatchedMoviesScreen` — summary card at top |
| **Current state** | The summary card only shows total movies watched. The Watched screen groups movies by month/year but offers no other insights |
| **Behaviour** | Expand the summary section into a small stats dashboard with: total movies watched · total runtime (hours) · favourite genre (most frequent) · longest watched streak (weeks). Cards use M3 `ElevatedCard` in a horizontal scroll row |
| **Rationale** | Stats/insights are a proven engagement driver for tracking apps (Spotify Wrapped effect). Adds delight without requiring new data sources |
| **Notes** | All data is derived from the existing Room `movies` table. No new network calls required |

### F-21 — Skeleton Loading States

| Attribute   | Detail |
|-------------|--------|
| **Status**  | 📋 Planned |
| **Scope**   | All list/grid screens: Search browse, Watchlist, Watched, Movie Detail |
| **Current state** | All loading states show a centered `LoadingIndicator` (spinner). This is functional but feels dated compared to top-tier apps |
| **Behaviour** | Replace loading spinners with shimmer skeleton placeholders that mirror the target layout (cards, rows, text lines). Use a shimmer animation library or implement via `Brush.linearGradient` with animated offset |
| **Rationale** | Skeleton screens reduce perceived loading time and provide layout continuity. Used by Netflix, Letterboxd, and all top media apps |
| **Notes** | Can be extracted as a shared `ShimmerBox` composable in `core/designsystem`. Each feature defines its own skeleton layout |

### F-22 — Search: Recent & Trending Searches

| Attribute   | Detail |
|-------------|--------|
| **Status**  | 📋 Planned |
| **Scope**   | `SearchMoviesScreen` — search field interaction |
| **Current state** | Tapping the search field immediately shows the browse grid. There is no search history or query suggestions |
| **Behaviour** | When the search field is focused but empty, show two sections: (1) **Recent searches** — last 5 queries, stored locally via DataStore, tappable to re-run; (2) **Trending searches** — sourced from TMDB `/trending` titles as quick-access chips. Clearing the field returns to this state |
| **Rationale** | Reduces re-search friction. Standard UX in all search-heavy apps (YouTube, Spotify, Letterboxd) |
| **Notes** | Recent searches stored in DataStore (not Room — ephemeral, no sync needed). Max 5 entries. Clear-all option available |

---

## 10. Version History

| Version | Date       | Summary                        |
|---------|------------|--------------------------------|
| 1.0     | 2026-02-25 | Initial PRD — baseline feature set documented |
| 1.1     | 2026-02-26 | Add F-08 Pull-to-Refresh and F-09 MVI Migration to planned backlog |
| 1.2     | 2026-02-26 | Unify G-XX gap IDs into F-XX sequence (G-01→F-04, G-02→F-10, G-03→F-11, G-04→F-12, G-05→F-13, G-06→F-14) |
| 1.3     | 2026-02-28 | Add F-15 through F-22 — UI improvement features identified from code review |
| 1.4     | 2026-02-28 | Add Status field to all features. Link completed FIPs. Update §8 gaps table with Status column. Rename PRP → FIP (Feature Implementation Plan) |

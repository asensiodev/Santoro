# FIP — Deep Link: Open TMDB Movie URLs in Santoro

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                              |
|------------------------|--------------------------------------------------------------------|
| **FIP ID**             | FIP-014                                                            |
| **Version**            | 1.0                                                                |
| **Status**             | ✅ Done                                                            |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — MVP Feature Suggestions §9              |
| **Feature**            | Deep Link: Open TMDB Movie URLs in Santoro                        |
| **Date**               | 2026-03-08                                                         |
| **Author**             | @asensiodev                                                        |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                          |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

FIP-002 (Share Movie) already sends a share text containing a TMDB URL (`https://www.themoviedb.org/movie/{id}`). When the recipient taps that link, it currently opens in the browser — even if they have Santoro installed. By registering an Android App Link for the TMDB movie URL pattern, tapping the link will open the movie directly inside the app, creating a seamless viral loop: **Share → Tap → Movie Detail in Santoro**.

Android App Links (verified via Digital Asset Links) provide the strongest association: the link opens in the app automatically without a disambiguation dialog. However, TMDB's domain (`themoviedb.org`) is not ours, so we **cannot** host a `/.well-known/assetlinks.json` file there. We will use a standard **deep link** (`intent-filter` with `autoVerify` omitted) instead, which shows the Android disambiguation dialog ("Open with…") when the user taps a TMDB movie URL. This is the same approach used by apps like Letterboxd and JustWatch for third-party URLs.

---

## 2. Goals

- When a user taps a `https://www.themoviedb.org/movie/{id}` link and selects Santoro from the chooser, the app opens directly on the Movie Detail screen for that movie.
- Handle both `http` and `https` schemes.
- Handle authenticated and unauthenticated states gracefully — if the user is not logged in, show the login screen first, then navigate to the movie after auth.
- Minimal code footprint: reuse existing `MovieDetailRoute` navigation, no new screens.

---

## 3. Non-Goals

- **Android App Links verification** — we don't control `themoviedb.org`, so we cannot host `assetlinks.json`. Disambiguation dialog is expected.
- Custom Santoro deep link scheme (`santoro://movie/{id}`) — no benefit without a web fallback; TMDB URLs are sufficient.
- Deep links for other screens (Watchlist, Watched, Search) — out of scope.
- Universal Links / Dynamic Links (Firebase) — over-engineered for this use case.

---

## 4. User Stories

| ID    | As a…             | I want to…                                                   | So that…                                             | Acceptance Criteria |
|-------|--------------------|--------------------------------------------------------------|------------------------------------------------------|---------------------|
| US-01 | Link recipient     | Tap a TMDB movie URL and have it open in Santoro             | I can see the movie detail without leaving the app   | Tapping `https://www.themoviedb.org/movie/27205` opens Movie Detail for movie ID 27205 when user picks Santoro from the disambiguation dialog |
| US-02 | Link recipient     | Be redirected to the browser if I don't pick Santoro         | I still get the TMDB page as fallback                | Choosing browser from the disambiguation dialog opens the URL normally |
| US-03 | Unauthenticated user | Tap a TMDB link when not logged in                          | I'm not stranded on a blank screen                   | Login screen shown first. After auth, user lands on Home (deep link intent consumed gracefully — no crash) |

---

## 5. UX / Flows

### Flow A — Authenticated user taps TMDB link

```
User taps https://www.themoviedb.org/movie/27205
  → Android disambiguation dialog: "Open with Santoro / Browser"
    → User picks Santoro
      → MainActivity receives intent with TMDB URL data
        → Parses movie ID (27205) from the URL path
          → Navigates to MovieDetailRoute(movieId = 27205)
            → Movie Detail loads via existing ViewModel (fetches from API/cache)
```

### Flow B — Unauthenticated user taps TMDB link

```
User taps https://www.themoviedb.org/movie/27205
  → Android disambiguation dialog → picks Santoro
    → MainActivity receives intent
      → Auth state = Unauthenticated → Login screen shown
        → User authenticates → navigated to TabHost (Home)
          → Deep link intent is NOT replayed (movie ID discarded)
```

**Rationale for Flow B:** Trying to navigate to Movie Detail before auth creates complex state management (pending deep link stored across auth flow). For MVP, discarding the deep link for unauthenticated users is acceptable. The user can re-tap the link after logging in. A follow-up could store the pending movie ID in `SavedStateHandle` and replay it post-auth.

### Edge cases

| Case                           | Behaviour                                               |
|--------------------------------|---------------------------------------------------------|
| URL has no valid movie ID      | Deep link ignored, app opens on Home (TabHost)          |
| URL path doesn't match pattern | Intent filter doesn't match, Android opens browser      |
| Movie ID valid but not in TMDB | Movie Detail shows its existing Error state with Retry  |
| App already open in foreground | `onNewIntent` triggers navigation to Movie Detail       |

---

## 6. Architecture

```
Android System (link tap)
       │
       ▼
MainActivity (intent-filter match)
       │ parses movieId from intent.data URI
       ▼
NavController.navigateToMovieDetail(movieId)
       │
       ▼
MovieDetailRoute (existing composable — unchanged)
       │
       ▼
MovieDetailViewModel (fetches movie — unchanged)
```

No new layers. The deep link is handled entirely in `MainActivity` by inspecting the incoming `Intent`.

---

## 7. Data Model

No new data models. No schema changes.

---

## 8. Modules Affected

- `app` — `AndroidManifest.xml` (intent-filter), `MainActivity.kt` (intent parsing + navigation)

No changes to feature modules, core modules, or domain layer.

---

## 9. Phases & Tasks

### Phase 1 — Intent Filter (Manifest)

- [x] Add `<intent-filter>` to `MainActivity` in `app/src/main/AndroidManifest.xml`:
  ```xml
  <intent-filter>
      <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <category android:name="android.intent.category.BROWSABLE" />
      <data
          android:scheme="https"
          android:host="www.themoviedb.org"
          android:pathPrefix="/movie/" />
      <data
          android:scheme="http"
          android:host="www.themoviedb.org"
          android:pathPrefix="/movie/" />
  </intent-filter>
  ```
- [x] Verify no `android:autoVerify="true"` is set (we don't control the domain)

### Phase 2 — Intent Parsing & Navigation

- [x] Create `DeepLinkHandler` object in `app/.../navigation/` with a pure function:
  ```
  fun parseMovieIdFromIntent(intent: Intent?): Int?
  ```
  - Extracts `intent?.data?.pathSegments` → expects `["movie", "{id}"]`
  - Parses the second segment as `Int` via `toIntOrNull()`
  - Returns `null` for any malformed or missing data
- [x] In `MainActivity.onCreate`, after determining `isAuthenticated = true`, check for a deep link movie ID:
  - Call `DeepLinkHandler.parseMovieIdFromIntent(intent)`
  - If non-null, store in a `var pendingDeepLinkMovieId: Int?` (local state or ViewModel)
- [x] In `SantoroApp` composable, accept an optional `deepLinkMovieId: Int?` parameter
  - Use a `LaunchedEffect(deepLinkMovieId)` to navigate to `MovieDetailRoute` after the NavHost is composed
  - Clear the pending ID after navigation to prevent re-navigation on recomposition
- [x] Override `onNewIntent` in `MainActivity` to handle links when the app is already open:
  - Parse the movie ID from the new intent
  - Trigger navigation to Movie Detail via the same mechanism

### Phase 3 — Tests

- [x] Unit: `DeepLinkHandler.parseMovieIdFromIntent` — valid TMDB URL → returns correct movie ID
- [x] Unit: `DeepLinkHandler.parseMovieIdFromIntent` — URL without movie segment → returns null
- [x] Unit: `DeepLinkHandler.parseMovieIdFromIntent` — URL with non-numeric ID → returns null
- [x] Unit: `DeepLinkHandler.parseMovieIdFromIntent` — null intent → returns null
- [x] Unit: `DeepLinkHandler.parseMovieIdFromIntent` — intent without data → returns null
- [x] Unit: `DeepLinkHandler.parseMovieIdFromIntent` — `http` scheme works same as `https`

---

## 10. Validation

| What                       | Result | Notes |
|----------------------------|--------|-------|
| Manual test on real device | ⏭️     |       |
| Manual test on emulator    | ⏭️     |       |
| Edge cases verified        | ⏭️     |       |
| Accessibility check        | ⏭️     |       |

**Manual test commands (adb):**
```bash
# Simulate tapping a TMDB link
adb shell am start -a android.intent.action.VIEW -d "https://www.themoviedb.org/movie/27205" com.asensiodev.santoro

# Non-existent movie
adb shell am start -a android.intent.action.VIEW -d "https://www.themoviedb.org/movie/999999999" com.asensiodev.santoro

# Malformed URL
adb shell am start -a android.intent.action.VIEW -d "https://www.themoviedb.org/movie/abc" com.asensiodev.santoro
```

---

## 11. Blockers

_None at planning time._

---

## 12. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Should we store the pending deep link movie ID for unauthenticated users and replay it post-auth? | **Deferred.** For MVP, the deep link is discarded if the user is not authenticated. Follow-up improvement. |
| 2 | Should we also match `themoviedb.org` (without `www`)? | Yes — add both `www.themoviedb.org` and `themoviedb.org` hosts to the intent-filter for coverage. |
| 3 | TMDB URLs sometimes include a slug after the ID (e.g. `/movie/27205-inception`). Does `pathPrefix="/movie/"` handle this? | Yes — `pathPrefix` matches any path starting with `/movie/`. The ID parser will extract only the numeric portion before the hyphen. |

---

## 13. Decisions

| # | Decision | Alternatives considered | Rationale |
|---|----------|------------------------|-----------|
| 1 | Standard deep link (no `autoVerify`) | Android App Links with `autoVerify` | We don't control `themoviedb.org` — cannot host `assetlinks.json`. Disambiguation dialog is acceptable |
| 2 | Parse intent in `MainActivity` directly | Use Navigation Compose deep link support (`deepLinks` param in `composable<>`) | Nav Compose deep links require a URI pattern that maps to a route. Our route uses type-safe args (`MovieDetailRoute(movieId: Int)`), and the TMDB URL structure doesn't match the auto-generated route pattern. Manual parsing is simpler and more explicit |
| 3 | `DeepLinkHandler` as a pure object with static function | Inject via Hilt | No dependencies needed — pure URL parsing. Object is simpler and fully unit-testable |
| 4 | Discard deep link for unauthenticated users (MVP) | Store pending link in `SavedStateHandle` and replay post-auth | Replay logic adds complexity (pending state across auth flow, process death restoration). Not worth it for MVP — user can re-tap the link |
| 5 | Match both `www.themoviedb.org` and `themoviedb.org` | Only `www` | Some users or apps may strip the `www` prefix. Costs one extra `<data>` element, zero code change |

---

## 14. Out of Scope / Follow-ups

- **Pending deep link replay post-auth** — store movie ID in `SavedStateHandle`, navigate after login completes.
- **Custom Santoro deep link scheme** (`santoro://movie/{id}`) — useful if we ever have our own web presence.
- **Firebase Dynamic Links** — deprecated by Google (sunset Sep 2025). Not viable.
- **Deep links for other screens** (watchlist, search query, etc.).
- **Share text update** — optionally include a Santoro-specific CTA ("Open in Santoro") in the share payload.

---

## 16. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-03-08 | Initial draft |


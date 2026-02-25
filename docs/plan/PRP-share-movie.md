# PRP — Share Movie

| Field           | Value                                          |
|-----------------|------------------------------------------------|
| **PRP ID**      | PRP-002                                        |
| **Version**     | 1.0                                            |
| **Status**      | ✅ Done                                        |
| **PRD ref**     | [PRD.md](../prd/PRD.md) — §F-03               |
| **Feature**     | Share a movie from Movie Detail                |
| **Date**        | 2026-02-25                                     |
| **Author**      | @asensiodev                                    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

Users discover movies in Santoro and want to recommend them to friends. There is currently no way to share a movie without leaving the app manually. A native Android Share Intent lets the user send movie info to any installed app (WhatsApp, Telegram, Gmail, Messages, etc.) in one tap, with zero additional infrastructure.

---

## 2. Goals

- User can share a movie directly from the Movie Detail screen.
- Share payload contains enough context for the recipient: title, year, rating, and a TMDB link.
- Uses the Android native Share Sheet — no custom UI, no hardcoded target apps.
- Zero new dependencies required.

### 2.1 Non-Goals

- Deep links / App Links so the recipient opens the movie inside Santoro (→ PRP-003).
- Sharing from Watchlist or Watched list cards (follow-up, out of scope here).
- Custom share card image / rich preview (follow-up).

---

## 3. User Story

| ID    | As a…    | I want to…                          | So that…                                      | Acceptance Criteria |
|-------|----------|-------------------------------------|-----------------------------------------------|---------------------|
| US-01 | app user | share a movie from its detail screen | my friends can discover it on any platform   | Tapping the share icon opens the native Android Share Sheet. The shared text includes the movie title, year, rating and TMDB URL. The sheet is populated with all installed apps that accept plain text. |

---

## 4. UX / Flow

```
MovieDetailScreen
  └── TopAppBar [share icon — top right, next to back arrow]
        └── tap → Android Share Sheet (system UI)
              └── user picks app (WhatsApp / Telegram / Gmail / …)
                    └── pre-filled text sent to chosen app
```

**Share text format:**
```
🎬 Inception (2010)
⭐ 8.8/10

Check it out on TMDB:
https://www.themoviedb.org/movie/27205
```

**Placement:** share `IconButton` in the `CollapsibleTopAppBar`, trailing end (right side), alongside the existing back button. Mirrors the pattern used in YouTube, Letterboxd, IMDb.

---

## 5. Phases & Tasks

### Phase 1 — Domain / helper

- [x] Create `ShareMovieHelper` in `feature/movie-detail/impl` — pure function that builds the share `Intent` from a `MovieUi` and launches `startActivity(Intent.createChooser(...))`
- [x] Add string resource `movie_detail_share_text` (format: `🎬 %1$s (%2$s)\n⭐ %3$.1f/10\n\nCheck it out on TMDB:\nhttps://www.themoviedb.org/movie/%4$d`)
- [x] Add string resource `movie_detail_share_chooser_title` (`"Share via"`)
- [x] Add string resource `movie_detail_share_icon_description` (`"Share movie"`)

### Phase 2 — Presentation layer

- [x] Add `onShareClicked: () -> Unit` param to `MovieDetailScreen` and `MovieDetailRoute`
- [x] Wire `onShareClicked` in `MovieDetailRoute` using `LocalContext` + `ShareMovieHelper`
- [x] Add share `IconButton` to `CollapsibleTopAppBar` (trailing end, tint matches existing back icon)
- [x] Pass `onShareClicked` down to `CollapsibleTopAppBar`

### Phase 3 — Tests

- [x] Unit: `ShareMovieHelper` — verify `EXTRA_TEXT` contains title, year, rating, and TMDB URL
- [x] Unit: `ShareMovieHelper` — verify null releaseDate handled gracefully

---

## 6. Technical Notes

### Modules affected
- `feature/movie-detail/impl` — only

### Share Intent construction
```kotlin
Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, shareText)
    putExtra(Intent.EXTRA_TITLE, chooserTitle)
}
// launched via: context.startActivity(Intent.createChooser(intent, null))
```

No new Gradle dependencies needed — `android.content.Intent` is part of the Android SDK.

### TMDB URL pattern
`https://www.themoviedb.org/movie/{movieId}` — stable, public, no auth required.

### TopAppBar placement
The share icon sits in `actions` of the existing `TopAppBar`. The back arrow stays in `navigationIcon`. No layout changes to the rest of the screen.

---

## 7. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Include poster image in the share payload as a Uri? | Out of scope — requires FileProvider setup. Follow-up. |

---

## 8. Out of Scope / Follow-ups

- **PRP-003 — Deep Links:** Share a Santoro deep link so recipients with the app open the movie directly; recipients without the app are redirected to the Play Store.
- Share from Watchlist / Watched list item (no detail screen required).
- Rich share card with poster image via `FileProvider`.

---

## 9. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-02-25 | Initial draft |

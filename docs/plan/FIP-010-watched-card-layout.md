# FIP — Watched & Watchlist: Purpose-Built Card Layouts

| Field                  | Value                                                         |
|------------------------|---------------------------------------------------------------|
| **FIP ID**             | FIP-010                                                       |
| **Version**            | 1.0                                                           |
| **Status**             | ✅ Done                                                       |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §F-17                              |
| **Feature**            | Replace broken MovieCard in Watched with full-bleed poster + gradient overlay |
| **Date**               | 2026-02-28                                                    |
| **Author**             | @asensiodev                                                   |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.

---

## 1. Context & Motivation

- **Watched `MovieCard`**: cloned from Search but broken — fixed `height(size128)`, image sized `size160` (doesn't fill), no metadata overlay. Visually poor.
- **Watchlist `WatchlistMovieItem`**: horizontal row with poster + title + year + rating + genres. Already correct for its purpose — not touched.
- Two hardcoded strings in `WatchedMoviesScreen`: `"Total Watched"` and `"$totalCount Movies"`.

---

## 2. Goals

- Watched grid card: full-bleed poster + bottom gradient overlay with title + watched date.
- Fix hardcoded strings in `WatchedMoviesScreen`.

## 3. Non-Goals

- Touching `WatchlistMovieItem` (already correct).
- Extracting a shared component to design-system (overkill for now).

---

## 8. Modules Affected

- `feature/watched-movies/impl` — `MovieCard.kt`, `WatchedMoviesScreen.kt`
- `core/string-resources` — add `watched_total_label`, `watched_total_count`

---

## 9. Phases & Tasks

### Phase 1 — String Resources

- [x] Add `watched_total_label` + `watched_total_count` (EN + ES)

### Phase 2 — Watched MovieCard

- [x] Rewrite `MovieCard` in `feature/watched-movies`: full-bleed poster, gradient overlay, title + watched date

### Phase 3 — WatchedMoviesScreen

- [x] Replace hardcoded `"Total Watched"` and `"$totalCount Movies"` with `stringResource`

---

## 16. Changelog

| Version | Date       | Summary        |
|---------|------------|----------------|
| 1.0     | 2026-02-28 | Initial draft + implementation |

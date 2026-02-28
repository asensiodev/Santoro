# FIP — Movie Detail: Hardcoded Strings Cleanup

| Field                  | Value                                                         |
|------------------------|---------------------------------------------------------------|
| **FIP ID**             | FIP-008                                                       |
| **Version**            | 1.0                                                           |
| **Status**             | ✅ Done                                                       |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §F-16                              |
| **Feature**            | Replace hardcoded strings in MovieDetailScreen with string resources |
| **Date**               | 2026-02-28                                                    |
| **Author**             | @asensiodev                                                   |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

`MovieDetailScreen.kt` contains three hardcoded strings in production Composables, violating the project's no-hardcoded-strings rule and breaking i18n readiness:

| Location              | Hardcoded value | Target key                        |
|-----------------------|-----------------|-----------------------------------|
| Overview section header | `"Overview"`  | `movie_detail_section_overview`   |
| WatchlistButton label | `"Watchlist"`   | `watchlist_icon_button` (exists)  |
| WatchedButton label   | `"Watched"`     | `watched_icon_button` (exists)    |

`watchlist_icon_button` and `watched_icon_button` already exist in `core/string-resources` (EN + ES). Only `movie_detail_section_overview` needs to be created.

---

## 2. Goals

- Zero hardcoded strings in `MovieDetailScreen.kt`.
- All string keys present in both `values/strings.xml` and `values-es/strings.xml`.

---

## 3. Non-Goals

- Refactoring any logic or layout in `MovieDetailScreen.kt`.
- Adding new UI strings beyond the three identified.

---

## 8. Modules Affected

- `core/string-resources` — add `movie_detail_section_overview` (EN + ES)
- `feature/movie-detail/impl` — replace hardcoded strings with `stringResource()`

---

## 9. Phases & Tasks

### Phase 1 — String Resources

- [x] Add `movie_detail_section_overview` to `values/strings.xml` (EN)
- [x] Add `movie_detail_section_overview` to `values-es/strings.xml` (ES)

### Phase 2 — MovieDetailScreen

- [x] Replace `"Overview"` with `stringResource(SR.string.movie_detail_section_overview)`
- [x] Replace `"Watchlist"` literals with `stringResource(SR.string.watchlist_icon_button)`
- [x] Replace `"Watched"` literals with `stringResource(SR.string.watched_icon_button)`

---

## 10. Validation

| What                        | Result | Notes |
|-----------------------------|--------|-------|
| Manual test on real device  | ⏭️     |       |
| Manual test on emulator     | ⏭️     |       |
| Edge cases verified         | ⏭️     |       |
| Accessibility check         | ⏭️     |       |

---

## 16. Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-02-28 | Initial draft + implementation |

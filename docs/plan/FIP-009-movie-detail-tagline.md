# FIP — Movie Detail: Tagline Display

| Field                  | Value                                                         |
|------------------------|---------------------------------------------------------------|
| **FIP ID**             | FIP-009                                                       |
| **Version**            | 1.0                                                           |
| **Status**             | ✅ Done                                                       |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §F-15                              |
| **Feature**            | Display movie tagline in hero section of MovieDetailScreen    |
| **Date**               | 2026-02-28                                                    |
| **Author**             | @asensiodev                                                   |
| **Definition of Done** | All checkboxes in all phases marked `[x]`                    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.

---

## 1. Context & Motivation

TMDB returns a `tagline` field for every movie. The current implementation does not include it in the domain model, DTO, Room entity, or UI. The PRD specifies it should appear in the hero section below the title, in italic, hidden when empty.

---

## 2. Goals

- Surface `tagline` from TMDB API all the way to the UI.
- Display it in `MovieHeaderSection`, below the title, only when non-empty.

---

## 3. Non-Goals

- Tagline in search cards or list items.
- Cloud sync of tagline (read-only field from API).

---

## 6. Architecture

```
MovieApiModel.tagline (DTO)
       │ MovieApiMapper
       ▼
Movie.tagline (domain)
       │ MovieMapper (DB) / MovieUiMapper
       ▼
MovieEntity.tagline (Room)   MovieUi.tagline (presentation)
                                    │
                              MovieDetailScreen → MovieHeaderSection
```

---

## 7. Data Model

- `MovieApiModel` — add `@SerializedName("tagline") val tagline: String?`
- `Movie` — add `val tagline: String? = null`
- `MovieEntity` — add `val tagline: String? = null` → Room migration 3→4
- `MovieUi` — add `val tagline: String? = null`

---

## 8. Modules Affected

- `core/domain` — `Movie.kt`
- `core/data` — `MovieApiModel.kt`, `MovieApiMapper.kt`
- `core/database` — `MovieEntity.kt`, `MovieMapper.kt`, `SantoroRoomDatabase.kt`, `DatabaseModule.kt`
- `feature/movie-detail/impl` — `MovieUi.kt`, `MovieUiMapper.kt`, `MovieDetailScreen.kt`

---

## 9. Phases & Tasks

### Phase 1 — Domain & Data Layer

- [x] Add `tagline: String? = null` to `Movie`
- [x] Add `tagline` to `MovieApiModel`
- [x] Map `tagline` in `MovieApiMapper.toDomain()`
- [x] Add `tagline` to `MovieEntity`
- [x] Add MIGRATION_3_4 (`ALTER TABLE movies ADD COLUMN tagline TEXT`)
- [x] Bump `SantoroRoomDatabase` to version 4
- [x] Register MIGRATION_3_4 in `DatabaseModule`
- [x] Map `tagline` in `MovieMapper.toDomain()` and `Movie.toEntity()`

### Phase 2 — Presentation Layer

- [x] Add `tagline: String? = null` to `MovieUi`
- [x] Map `tagline` in `MovieUiMapper.toUi()` (pass-through from domain)
- [x] Render tagline in `MovieHeaderSection` (italic, `bodyMedium`, hidden when null/blank)

### Phase 3 — Tests

- [x] Update `MovieUiMapperTest` to include tagline

---

## 16. Changelog

| Version | Date       | Summary        |
|---------|------------|----------------|
| 1.0     | 2026-02-28 | Initial draft + implementation |

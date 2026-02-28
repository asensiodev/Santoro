# FIP — Theme Toggle (Light / Dark / System)

| Field           | Value                                          |
|-----------------|------------------------------------------------|
| **FIP ID**      | FIP-005                                        |
| **Status**      | ✅ Done                                        |
| **PRD ref**     | [PRD.md](../prd/PRD.md) — §F-10               |
| **Feature**     | Let the user choose Light, Dark, or System theme |
| **Date**        | 2026-02-26                                     |
| **Author**      | @asensiodev                                    |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

`SantoroTheme` currently reads `isSystemInDarkTheme()` directly and has no user override.
`SettingsScreen` already renders an "Appearance" row with a `/* TODO */` click handler.
`UserPreferencesRepository` / `DefaultUserPreferencesRepository` already exist — we extend them to persist the theme choice.

---

## 2. Goals

- User can pick **Light**, **Dark**, or **System (follow device)** from Settings → Appearance.
- Choice persists across app restarts (via `SecureKeyValueStore`, same pattern as onboarding flag).
- `SantoroTheme` applies the override immediately — no restart required.
- `MainActivity` observes the preference and passes `darkTheme` down to `SantoroTheme`.

---

## 3. Non-Goals

- Dynamic color (Material You) — not planned.
- Per-feature theme overrides.
- AMOLED / pure-black variant.

---

## 6. Architecture

```
Settings UI
  └─ ThemeOption (Light | Dark | System)
       └─ SettingsViewModel.setTheme(ThemeOption)
            └─ SetThemeUseCase                    [core/domain]
                 └─ UserPreferencesRepository.setTheme()
                      └─ DefaultUserPreferencesRepository  [core/data]
                           └─ SecureKeyValueStore

MainActivity
  └─ MainActivityViewModel.themeOption: StateFlow<ThemeOption>
       └─ ObserveThemeUseCase                     [core/domain]
            └─ UserPreferencesRepository.theme: Flow<ThemeOption>

SantoroTheme(darkTheme = …)   [core/design-system]
```

---

## 7. Data Model

**`ThemeOption`** enum in `core/domain`:

```kotlin
enum class ThemeOption { LIGHT, DARK, SYSTEM }
```

Stored as a string key in `SecureKeyValueStore`:
`"theme_option"` → `"LIGHT"` | `"DARK"` | `"SYSTEM"` (default: `"SYSTEM"`)

---

## 9. Phases & Tasks

### Phase 1 — Domain

- [x] Add `ThemeOption` enum class to `core/domain/model/`
- [x] Add `theme: Flow<ThemeOption>` to `UserPreferencesRepository`
- [x] Add `suspend fun setTheme(option: ThemeOption)` to `UserPreferencesRepository`
- [x] Create `ObserveThemeUseCase` in `core/domain/usecase/`
- [x] Create `SetThemeUseCase` in `core/domain/usecase/`

### Phase 2 — Data

- [x] Implement `theme` flow in `DefaultUserPreferencesRepository`
- [x] Implement `setTheme()` in `DefaultUserPreferencesRepository`

### Phase 3 — `MainActivity` + `SantoroTheme`

- [x] Inject `ObserveThemeUseCase` into `MainActivityViewModel`
- [x] Expose `themeOption: StateFlow<ThemeOption>` from `MainActivityViewModel`
- [x] In `MainActivity.setContent`, collect `themeOption` and compute `darkTheme: Boolean`

### Phase 4 — Settings UI

- [x] Add `currentTheme: ThemeOption` to `SettingsUiState`
- [x] Inject `ObserveThemeUseCase` + `SetThemeUseCase` into `SettingsViewModel`
- [x] Observe theme in `SettingsViewModel.init`, update `uiState.currentTheme`
- [x] Add `fun setTheme(option: ThemeOption)` to `SettingsViewModel`
- [x] Add `showThemePicker: Boolean = false` to `SettingsUiState`
- [x] Add `fun onAppearanceClicked()` to `SettingsViewModel`
- [x] Create `ThemePickerBottomSheet` composable
- [x] Wire `onAppearanceClicked` in `SettingsScreen`
- [x] Show `ThemePickerBottomSheet` when `uiState.showThemePicker == true`
- [x] Add strings: `settings_appearance_system`, `settings_appearance_light`, `settings_appearance_dark`, `settings_appearance_sheet_title`

### Phase 5 — Tests

- [x] Unit — `DefaultUserPreferencesRepository`: theme init, setTheme
- [x] Unit — `SettingsViewModel`: onAppearanceClicked, setTheme, dismissThemePicker
- [x] Unit — `MainActivityViewModel`: themeOption StateFlow

---

## 10. Open Questions

| # | Question | Resolution |
|---|----------|------------|
| 1 | Where does the Appearance picker live — inline in `SettingsScreen` (e.g. radio group / segmented button) or a separate `AppearanceScreen`? | **Inline bottom sheet** — keeps navigation simple. A `ModalBottomSheet` with 3 options appears when tapping "Appearance". No new route needed. |
| 2 | Store preference in `SecureKeyValueStore` (already used) or plain `DataStore`? | **SecureKeyValueStore** — consistent with existing `UserPreferencesRepository` pattern. No new dependency. |
| 3 | Should `ThemeOption` live in `core/domain` or `core/design-system`? | **`core/domain`** — it's a user preference, not a design token. `core/design-system` only maps it to a `Boolean` for `SantoroTheme`. |

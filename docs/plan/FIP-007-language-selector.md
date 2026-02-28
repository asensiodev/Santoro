# FIP — Language Selector

| Field           | Value                                              |
|-----------------|----------------------------------------------------|
| **FIP ID**      | FIP-007                                            |
| **Version**     | 1.0                                                |
| **Status**      | ✅ Done                                            |
| **PRD ref**     | [PRD.md](../prd/PRD.md) — §3.7 Settings (F-11)    |
| **Feature**     | Language Selector (English / Spanish)              |
| **Date**        | 2026-02-26                                         |
| **Author**      | @asensiodev                                        |

---

> **Execution rule:** Work phase by phase, task by task. Mark each checkbox as you complete it.
> If anything is ambiguous or doesn't fit the plan — **stop and ask** before deciding.

---

## 1. Context & Motivation

The Settings screen has a "Language" item already rendered but wired to a no-op (`{}`).
The app ships only English strings today. We want to let users explicitly choose between
English and Spanish, persisting the choice across restarts.

Android 13+ (API 33) introduced `AppCompatDelegate.setApplicationLocales()` — the modern,
per-app locale API that does **not** require restarting the Activity and works without
touching system settings. Below API 33 it falls back gracefully via the AppCompat backport.

---

## 2. Goals

- [x] User can open a Language bottom sheet from Settings
- [x] Two options available: English (en) · Spanish (es)
- [x] Selection is persisted across app restarts
- [x] UI language updates immediately without Activity restart
- [x] Current selected language is highlighted in the bottom sheet

---

## 3. Non-Goals

- More than 2 languages (future follow-up)
- In-app locale override via manual `Configuration` wrapping (deprecated approach)
- Translating third-party content (movie titles / overviews from TMDB remain in the original language)

---

## 4. User Stories

| ID    | As a…     | I want to…                        | So that…                                  | Acceptance Criteria                                                   |
|-------|-----------|-----------------------------------|-------------------------------------------|-----------------------------------------------------------------------|
| US-01 | User      | tap Language in Settings          | open a language picker                    | Bottom sheet appears with EN / ES options                            |
| US-02 | User      | select Spanish                    | see the app in Spanish immediately        | All string resources switch to ES without restarting the Activity     |
| US-03 | User      | reopen the app after selecting ES | still see Spanish                         | Locale persisted — survives process kill                              |
| US-04 | User      | open the picker again             | see my current language highlighted       | The active locale has a checkmark or distinct visual treatment        |

---

## 5. UX / Flows

```
SettingsScreen
  └── tap "Language" row
        └── LanguagePickerBottomSheet (Modal)
              ├── 🇬🇧 English    [✓ if active]
              └── 🇪🇸 Spanish   [✓ if active]
                    └── tap option → locale changes → sheet dismisses → UI refreshes
```

The bottom sheet reuses the same `ModalBottomSheet` M3 component already used by the
Theme picker (`ThemePickerBottomSheet`) for visual consistency.

---

## 6. Architecture

### Locale API
Use `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))`
(AndroidX AppCompat). This:
- Works on API 21+
- On API 33+ delegates to the system per-app locale
- Automatically persists the choice (no manual SharedPreferences needed on API 33+)
- On older APIs, AppCompat handles persistence via `SharedPreferences` internally

### Reading current locale
`AppCompatDelegate.getApplicationLocales()` returns the currently set locale list.
If empty → system default → treat as English for the picker UI.

### No new dependency needed
`AppCompatDelegate` is already available via `appcompat` which is transitively included.

---

## 9. Phases & Tasks

### Phase 1 — Domain / Model

- [x] Create `AppLanguage` sealed class in `core/domain` with `English` and `Spanish` entries
- [x] Each entry carries a `tag: String` (e.g. `"en"`, `"es"`) and a `labelRes: Int` string resource id

### Phase 2 — String Resources

- [x] Add `values-es/strings.xml` to `core/string-resources` with Spanish translations of all existing strings
- [x] Add new strings for the language picker UI to both `values/strings.xml` and `values-es/strings.xml`:
  - `settings_language_picker_title`
  - `settings_language_english`
  - `settings_language_spanish`

### Phase 3 — Settings Feature (Presentation)

- [x] Add `LanguagePickerBottomSheet` composable in `feature/settings/impl`
- [x] Add `OnLanguageClicked` and `SetLanguage(language: AppLanguage)` to `SettingsIntent`
- [x] Add `showLanguagePicker: Boolean` and `currentLanguage: AppLanguage` to `SettingsUiState`
- [x] Implement `showLanguagePicker()` and `setLanguage()` private functions in `SettingsViewModel`
- [x] Wire `onLanguageClicked` in `SettingsScreenRoute` to dispatch `SettingsIntent.OnLanguageClicked`
- [x] Render `LanguagePickerBottomSheet` in `SettingsScreenRoute` when `showLanguagePicker == true`
- [x] Call `AppCompatDelegate.setApplicationLocales()` inside `setLanguage()` in the ViewModel

### Phase 4 — Tests

- [x] Unit test `SettingsViewModel`: `GIVEN OnLanguageClicked WHEN process THEN showLanguagePicker = true`
- [x] Unit test `SettingsViewModel`: `GIVEN SetLanguage(Spanish) WHEN process THEN showLanguagePicker = false`

---

## 11. Out of Scope / Follow-ups

- Additional languages beyond EN / ES
- Language displayed in the Settings row subtitle (current language name)

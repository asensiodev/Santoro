# Shipping Checklist — Google Play v1.0.0

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field       | Value              |
|-------------|--------------------|
| **Version** | 1.0                |
| **Status**  | 🔵 In Progress     |
| **Date**    | 2026-03-01         |
| **Target**  | Google Play — First public release |

---

## 1. Google Play Mandatory Requirements

These items **block** store submission. The app will be rejected without them.

### 1.1 Privacy Policy

- [x] Write a Privacy Policy covering: data collected (Firebase Auth UID, email, display name), analytics (Firebase Analytics), crash reporting (Crashlytics), third-party API (TMDB), local storage (Room, DataStore) → `docs/legal/PRIVACY-POLICY.md`
- [ ] Host the Privacy Policy at a public URL (GitHub Pages, Notion, or static site)
- [ ] Add a "Privacy Policy" item in `SettingsScreen` that opens the URL in a browser
- [ ] Add the Privacy Policy URL string resource (EN + ES)
- [ ] Enter the Privacy Policy URL in Google Play Console → App content → Privacy policy

### 1.2 Data Safety Form

- [ ] Complete the Data Safety form in Google Play Console → App content → Data safety
- [ ] Declare: email + name collected via Firebase Auth (account management)
- [ ] Declare: crash logs collected via Crashlytics (analytics)
- [ ] Declare: app activity collected via Firebase Analytics (analytics)
- [ ] Declare: no data shared with third parties
- [ ] Declare: no data sold
- [ ] Declare: users can request account deletion (Firebase Auth)

### 1.3 Content Rating

- [ ] Complete the content rating questionnaire in Google Play Console
- [ ] Expected rating: PEGI 3 / Everyone (movie info app, no user-generated content)

### 1.4 Target Audience & Ads Declaration

- [ ] Declare target audience (not directed at children under 13)
- [ ] Declare no ads in the app

---

## 2. Build & Release Configuration

### 2.1 Version Bump

- [ ] Update `versionName` to `"1.0.0"` in `gradle/libs.versions.toml`
- [ ] Verify `versionCode` is `1` (first release)

### 2.2 R8 / ProGuard (Code Shrinking)

- [ ] Enable `isMinifyEnabled = true` for release in `BuildType.kt` (app)
- [ ] Enable `isShrinkResources = true` for release in `BuildType.kt` (app)
- [ ] Keep library release `isMinifyEnabled = false` (consumer rules handle it)
- [ ] Verify existing `proguard-rules.pro` covers Credentials Manager
- [ ] Add ProGuard rules for Retrofit/Gson models (prevent serialization issues)
- [ ] Add ProGuard rules for Room entities if needed
- [ ] Build release APK/AAB and smoke test — verify no runtime crashes from R8

### 2.3 Signing

- [ ] Verify release signing config works (`signingConfigs.release` in `app/build.gradle.kts`)
- [ ] Upload signing key to Google Play App Signing (or use Play App Signing)
- [ ] Generate signed AAB: `./gradlew bundleRelease`

---

## 3. Store Listing

### 3.1 App Identity

- [ ] App name: "Santoro" (confirm availability)
- [ ] Short description (≤ 80 chars)
- [ ] Full description (≤ 4000 chars)
- [ ] App category: Entertainment
- [ ] Default language: English (US)

### 3.2 Graphics Assets

- [ ] App icon: 512×512 PNG (already exists: `ic_launcher-playstore.png` — verify quality)
- [ ] Feature graphic: 1024×500 PNG
- [ ] Phone screenshots: minimum 2, recommended 4–8 (capture key flows: search, detail, watchlist, watched, settings)
- [ ] Tablet screenshots: optional but recommended for broader reach

---

## 4. App Quality — Smoke Test Checklist

Run these on a **real device** with the release AAB before submitting.

### 4.1 Core Flows

- [ ] Fresh install → Login screen → Sign in with Google → Home
- [ ] Fresh install → Continue as Guest → Home → Browse movies
- [ ] Search: type query → results appear → tap movie → detail loads
- [ ] Movie Detail: mark Watched → icon animates → appears in Watched tab
- [ ] Movie Detail: add to Watchlist → icon animates → appears in Watchlist tab
- [ ] Watchlist: swipe to remove → movie removed
- [ ] Watched: stats dashboard shows correct data
- [ ] Settings: change theme (Light/Dark/System) → applies immediately
- [ ] Settings: change language → app restarts in selected language
- [ ] Settings: sign out → returns to Login screen
- [ ] Profile: link Google account (from anonymous) → success

### 4.2 Edge Cases

- [ ] Airplane mode: browse shows cached data + offline banner
- [ ] Airplane mode: Watchlist and Watched work fully offline
- [ ] Pull-to-refresh with no internet → stale data banner remains, no crash
- [ ] Search with empty results → empty state shown
- [ ] Recent searches: tap field → suggestions appear → tap chip → search runs
- [ ] Back navigation: no blank screens, no stuck states
- [ ] Rotate device: state preserved (search query, scroll position)

### 4.3 Performance

- [ ] Cold start < 3 seconds on mid-range device
- [ ] No jank on scroll in browse sections (60fps)
- [ ] No ANR on any flow
- [ ] Memory: no obvious leaks after navigating back and forth 10+ times

---

## 5. Legal & Compliance

- [ ] Verify TMDB API Terms of Use compliance (attribution required — check if logo/text is shown)
- [ ] Add TMDB attribution text somewhere visible (Settings or About section)
- [ ] Verify Firebase usage complies with Google Terms of Service
- [ ] Verify no copyrighted images are bundled (all movie posters are loaded from TMDB CDN at runtime — OK)

---

## 6. Post-Submission

- [ ] Monitor Crashlytics for crash spikes after first install wave
- [ ] Monitor Google Play Console for pre-launch report issues
- [ ] Prepare v1.0.1 hotfix branch in case of critical bugs
- [ ] Reply to any Google Play review team feedback within 48h

---

## Priority Order (Recommended)

1. **Privacy Policy** (§1.1) — absolute blocker
2. **R8 / ProGuard** (§2.2) — build stability, do early to catch issues
3. **Version bump** (§2.1) — trivial
4. **Smoke test** (§4) — catches bugs before submission
5. **TMDB attribution** (§5) — legal compliance
6. **Store listing** (§3) — can be done in parallel in Play Console
7. **Data Safety + Content Rating** (§1.2–1.4) — done in Play Console
8. **Post-submission monitoring** (§6) — after launch

---

## Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.0     | 2026-03-01 | Initial checklist |

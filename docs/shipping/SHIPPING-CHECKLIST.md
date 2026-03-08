# Shipping Checklist — Google Play v1.0.0

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field       | Value              |
|-------------|--------------------|
| **Version** | 1.2                |
| **Status**  | 🔵 In Progress     |
| **Date**    | 2026-03-05         |
| **Target**  | Google Play — First public release |

---

## 1. Google Play Mandatory Requirements

These items **block** store submission. The app will be rejected without them.

### 1.1 Privacy Policy

- [x] Write a Privacy Policy covering: data collected (Firebase Auth UID, email, display name), analytics (Firebase Analytics), crash reporting (Crashlytics), third-party API (TMDB), local storage (Room, DataStore) → `docs/legal/PRIVACY-POLICY.md`
- [x] Host the Privacy Policy at a public URL (GitHub Pages, Notion, or static site)
- [x] Add a "Privacy Policy" item in `SettingsScreen` that opens the URL in a browser
- [x] Add the Privacy Policy URL string resource (EN + ES)
- [x] Enter the Privacy Policy URL in Google Play Console → App content → Privacy policy

### 1.2 Data Safety Form- [ ] Complete the Data Safety form in Google Play Console → App content → Data safety
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

- [x] Update `versionName` to `"1.0.0"` in `gradle/libs.versions.toml`
- [x] Verify `versionCode` is `1` (first release)

### 2.2 R8 / ProGuard (Code Shrinking)

- [x] Enable `isMinifyEnabled = true` for release in `BuildType.kt` (app)
- [x] Enable `isShrinkResources = true` for release in `BuildType.kt` (app)
- [x] Keep library release `isMinifyEnabled = false` (consumer rules handle it)
- [x] Verify existing `proguard-rules.pro` covers Credentials Manager
- [x] Add ProGuard rules for Retrofit/Gson models (prevent serialization issues)
- [x] Add ProGuard rules for Room entities if needed
- [ ] Build release APK/AAB and smoke test — verify no runtime crashes from R8

### 2.3 Signing

- [x] Verify release signing config works (`signingConfigs.release` in `app/build.gradle.kts`)
- [ ] Upload signing key to Google Play App Signing (or use Play App Signing)
- [x] Generate signed AAB: `./gradlew bundleRelease`

---

## 3. Internal Testing Track (Closed Testing)

Google Play requires that new apps complete **at least 14 days of closed testing** with a minimum of **12 testers** who have opted in, before you can request production access.

> ⚠️ **No emulators.** Testers must opt in from Google Play on a **real device** signed in with the invited email. Emulator installs do not count.

**What Google actually checks:**
- The closed testing track has been **active for ≥ 14 days** with a published AAB.
- At least **12 testers** have opted in (accepted the invite).
- Testers do **NOT** need to use the app daily — they just need to accept the opt-in link and install the app. That's it. No daily usage required.
- Google does NOT verify that testers actively use the app during the 14 days — the requirement is just that the track exists with enough opt-ins for ≥ 14 days.

### 3.1 Setup

- [ ] Go to Google Play Console → **Testing → Closed testing** (or Internal testing)
- [ ] Create a new release: upload the signed AAB (`./gradlew bundleRelease`)
- [ ] Create an **email list** with 12+ Gmail addresses (friends, family, colleagues — they just need to accept the invite and install once)
- [ ] Add the email list to the testing track as testers
- [ ] Google generates an **opt-in link** — share it with all testers

### 3.2 Tester Instructions (copy-paste to testers)

> 1. Open the link I sent you (Google Play opt-in)
> 2. Tap "Accept" / "Become a tester"
> 3. Wait 2–5 minutes, then tap the Google Play install link (or search "Santoro" in Play Store — it appears under "Internal testing")
> 4. Install the app and open it at least once
> 5. That's it — no need to use the app daily or do anything else

### 3.3 Timeline & Checklist

- [ ] Upload first AAB to internal testing track
- [ ] Invite 12+ testers via email list
- [ ] Share opt-in link with all testers
- [ ] Verify at least 12 testers have opted in (Play Console → Testers tab)
- [ ] Confirm testers have installed and opened the app at least once
- [ ] Wait 14 days from first upload date
- [ ] Check for crashes in Crashlytics / Play Console pre-launch report
- [ ] After 14 days: request **production access** in Play Console

### 3.4 Tips

- **Need 12 emails fast?** Create a Google Sheet, ask friends/family to add their Gmail. Most people just need to tap a link and install — takes 2 minutes.
- **Testers don't see the app in Play Store search** — they must use the opt-in link first.
- **If a tester can't install:** make sure they accepted the opt-in AND are signed in to Play Store with the invited email.
- **You can keep uploading new AABs** to the internal track during the 14 days — testers auto-update.

---

## 4. Store Listing

### 4.1 App Identity

- [ ] App name: "Santoro" (confirm availability)
- [ ] Short description (≤ 80 chars)
- [ ] Full description (≤ 4000 chars)
- [ ] App category: Entertainment
- [ ] Default language: English (US)

### 4.2 Graphics Assets

- [ ] App icon: 512×512 PNG (already exists: `ic_launcher-playstore.png` — verify quality)
- [ ] Feature graphic: 1024×500 PNG
- [ ] Phone screenshots: minimum 2, recommended 4–8 (capture key flows: search, detail, watchlist, watched, settings)
- [ ] Tablet screenshots: optional but recommended for broader reach

---

## 5. App Quality — Smoke Test Checklist

Run these on a **real device** with the release AAB before submitting.

### 5.1 Core Flows

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

### 5.2 Edge Cases

- [ ] Airplane mode: browse shows cached data + offline banner
- [ ] Airplane mode: Watchlist and Watched work fully offline
- [ ] Pull-to-refresh with no internet → stale data banner remains, no crash
- [ ] Search with empty results → empty state shown
- [ ] Recent searches: tap field → suggestions appear → tap chip → search runs
- [ ] Back navigation: no blank screens, no stuck states
- [ ] Rotate device: state preserved (search query, scroll position)

### 5.3 Performance

- [ ] Cold start < 3 seconds on mid-range device
- [ ] No jank on scroll in browse sections (60fps)
- [ ] No ANR on any flow
- [ ] Memory: no obvious leaks after navigating back and forth 10+ times

---

## 6. Legal & Compliance

- [ ] Verify TMDB API Terms of Use compliance (attribution required — check if logo/text is shown)
- [ ] Add TMDB attribution text somewhere visible (Settings or About section)
- [ ] Verify Firebase usage complies with Google Terms of Service
- [ ] Verify no copyrighted images are bundled (all movie posters are loaded from TMDB CDN at runtime — OK)

---

## 7. Post-Submission

- [ ] Monitor Crashlytics for crash spikes after first install wave
- [ ] Monitor Google Play Console for pre-launch report issues
- [ ] Prepare v1.0.1 hotfix branch in case of critical bugs
- [ ] Reply to any Google Play review team feedback within 48h

---

## Priority Order (Recommended)

1. **Privacy Policy in app** (§1.1) — absolute blocker
2. **R8 / ProGuard** (§2.2) — build stability, do early to catch issues
3. **Version bump** (§2.1) — trivial
4. **Smoke test on real device** (§5) — catches bugs before submission
5. **Upload AAB to Internal Testing** (§3) — starts the 14-day clock ⏱️
6. **Invite 12 testers** (§3.2–3.3) — do ASAP after upload
7. **TMDB attribution** (§6) — legal compliance
8. **Store listing** (§4) — can prepare in parallel during 14-day wait
9. **Data Safety + Content Rating** (§1.2–1.4) — done in Play Console
10. **After 14 days: request production** → review → publish

---

## Changelog

| Version | Date       | Summary       |
|---------|------------|---------------|
| 1.2     | 2026-03-05 | Fix tester count to 12 (not 20), clarify testers only need to opt-in + install |
| 1.1     | 2026-03-01 | Add Internal Testing track (§3), reorder sections, clarify 14-day / 12-tester requirement |
| 1.0     | 2026-03-01 | Initial checklist |

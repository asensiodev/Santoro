# Guide — Firebase Firestore Setup

| Field       | Value                          |
|-------------|--------------------------------|
| **Type**    | Manual configuration guide     |
| **PRP ref** | [PRP-003](../plan/PRP-003-firebase-sync.md) |
| **Date**    | 2026-02-25                     |
| **Author**  | @asensiodev                    |

---

> **Before starting PRP-003 code execution**, complete every step in this guide and mark each checkbox.
> All steps are done in the [Firebase Console](https://console.firebase.google.com) — no code required.

---

## Step 1 — Verify Anonymous Auth is enabled

Santoro uses anonymous sign-in. Firestore security rules will be based on `request.auth.uid`, so Anonymous Auth must be active.

- [x] Open [Firebase Console](https://console.firebase.google.com) → select your **Santoro** project
- [x] Go to **Authentication** → **Sign-in method**
- [x] Confirm **Anonymous** provider is **Enabled** (toggle on if not)
- [x] Confirm **Google** provider is **Enabled** (toggle on if not)

---

## Step 2 — Create Firestore Database

- [x] In Firebase Console → go to **Firestore Database** (left sidebar, under "Build")
- [x] Click **Create database**
- [x] Select **Production mode** (not test mode — we'll write proper rules in Step 4)
- [x] Choose a **location** close to your target users:
  - Europe: `eur3` (Belgium + Netherlands multi-region) — recommended for EU users
  - US: `nam5` (Iowa + South Carolina multi-region)
  - ⚠️ **Location cannot be changed after creation** — choose carefully
- [x] Click **Done** — wait for provisioning (usually ~30 seconds)

---

## Step 3 — Understand the data schema

Santoro will use this Firestore structure. No action needed here — just read to understand what the code will create.

```
Firestore root
└── users/                          ← collection
    └── {uid}/                      ← document per user (uid from Firebase Auth)
        └── movies/                 ← subcollection
            └── {movieId}/          ← document per movie (TMDB movie ID as string)
                ├── movieId         : Number
                ├── title           : String
                ├── posterPath      : String?
                ├── isWatched       : Boolean
                ├── isInWatchlist   : Boolean
                ├── watchedAt       : Timestamp?  (null if not watched)
                └── updatedAt       : Timestamp   (last write — used for conflict resolution)
```

**Design decisions:**
- Only movies the user has interacted with (watched or watchlisted) are stored in Firestore.
- TMDB browse data (popular, trending, etc.) is **never** synced — it's always fetched from the API.
- `updatedAt` is the conflict resolution key: the most recent write wins (last-write-wins strategy).

---

## Step 4 — Set Security Rules

These rules ensure users can only read and write their own data.

- [x] In Firestore Database → click the **Rules** tab
- [x] Replace the existing rules with the following:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users can only access their own data
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;

      // Movies subcollection inherits the uid check
      match /movies/{movieId} {
        allow read, write: if request.auth != null && request.auth.uid == uid;
      }
    }
  }
}
```

- [x] Click **Publish**
- [x] Verify the rules are saved (the editor shows the timestamp of the last publish)

**What these rules do:**
- `request.auth != null` → user must be authenticated (anonymous counts)
- `request.auth.uid == uid` → user can only touch documents under their own `uid` path

---

## Step 5 — Add Firestore to the Android project (gradle dependency)

The `google-services.json` is already in the project. You just need to declare the Firestore dependency.

- [x] Open `gradle/libs.versions.toml`
- [x] In the `[libraries]` section, add after the existing `firebase-*` entries:

```toml
firebase-firestore = { module = "com.google.firebase:firebase-firestore-ktx" }
```

> The version is managed by the Firebase BOM already declared in the project (`firebase-bom`), so no version number needed here.

- [x] Save the file — the PRP-003 implementation will reference this dependency when needed

---

## Step 6 — Verify setup in Firebase Console

- [x] Go to **Firestore Database** → **Data** tab
- [x] The database should show an empty root (no collections yet — they'll be created by the app on first sync)
- [x] Go to **Firestore Database** → **Usage** tab
- [x] Confirm reads/writes/deletes are all at 0 (baseline before the app writes anything)

---

## Step 7 — (Optional but recommended) Enable Firestore offline persistence review

Firestore SDK has its own offline cache on the client side. Santoro uses **Room as the single source of truth** and does NOT rely on Firestore's built-in offline cache — but it's good to know it exists.

No action needed. Just awareness:
- Firestore SDK caches data locally by default on Android.
- Santoro's architecture will **disable or ignore** this to avoid a double-cache situation and keep Room as the only local truth.

---

## Step 8 — Notify agent to start PRP-003

- [x] All steps above are ✅ checked
- [x] Tell the agent: *"Firestore setup done, start PRP-003"*

---

## Troubleshooting

| Problem | Solution |
|---|---|
| "Firestore Database" not visible in sidebar | Make sure you're in the correct Firebase project |
| Rules won't publish — syntax error | Copy-paste the rules block exactly, Firestore rules are whitespace-sensitive |
| `google-services.json` mismatch | Re-download it from Project Settings → Your apps → Android app → `google-services.json` |
| App crashes with `PERMISSION_DENIED` | Security rules are wrong or not published — re-check Step 4 |
| App crashes with `Failed to get service account credentials` | You're using an emulator without proper Google Play services — test on a real device or a Google Play-enabled emulator |

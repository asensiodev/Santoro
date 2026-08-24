# FIP — Client Account Deletion

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                       |
|------------------------|-------------------------------------------------------------|
| **FIP ID**             | FIP-022                                                     |
| **Version**            | 1.0                                                         |
| **Status**             | 🔵 In Progress                                              |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — §3.7 Settings                    |
| **Feature**            | Delete account data from Android before deleting Auth       |
| **Date**               | 2026-08-24                                                  |
| **Author**             | @asensiodev                                                 |
| **Definition of Done** | All checkboxes are marked `[x]` and validation is complete  |

---

> **Execution rule:** Work phase by phase. Stop and ask if implementation requires backend code, new persistence, App Check, account partitioning, or behavior outside this plan.

## 1. Context & Motivation

The current Settings deletion clears Room and deletes the Firebase Authentication user, but leaves `users/{uid}/movies/*` in Firestore. It also ignores the Room cleanup result and can delete local data before Auth reports success.

This version fixes the incorrect deletion claim entirely from Android using the Firebase SDKs already in the project. It intentionally accepts a best-effort boundary instead of introducing backend infrastructure.

## 2. Goals

- Require a fresh Google credential before destructive work.
- Delete all Firestore movie documents owned by the authenticated UID.
- Delete Firebase Auth only after Firestore succeeds.
- Clear Room only after both remote operations succeed.
- Preserve coroutine cancellation and expose failures through the existing localized Settings error.
- Cover the new ordering and failure boundaries with existing MockK/Kluent test patterns.

## 3. Non-Goals And Accepted Limits

- No Cloud Functions, TypeScript, Blaze plan, App Check, server locks, or new CI stack.
- No DataStore marker, operation ID, process-death recovery, or global recovery UI.
- No per-user Room partitioning, account switching redesign, or F-28 implementation.
- No recent-search deletion; recent searches are device-local and not tied to the Firebase UID.
- No WorkManager redesign or guarantee against an already-running upload.
- No guarantee that another device with a still-valid token cannot write during the short Firestore-to-Auth deletion window.
- If the process stops or a later step fails after Firestore deletion, the user may need to retry. The app must not report success unless every step returns success.

## 4. User Flow

```text
Confirm deletion
    → request Google credential
    → reauthenticate current Firebase user
    → delete users/{uid}/movies documents from Firestore
    → delete Firebase Auth user
    → clear Room user data
    → existing auth-state navigation returns to Login
```

Credential cancellation, wrong-account reauthentication, Firestore failure, Auth failure, and Room failure use the existing deletion error feedback. No raw SDK error is displayed.

## 5. Architecture

```text
SettingsViewModel
    ├─ GoogleSignInHelper
    └─ DeleteAccountUseCase
          ├─ AuthRepository.reauthenticateWithGoogle
          ├─ SyncRepository.deleteUserData
          ├─ AuthRepository.deleteAccount
          └─ DatabaseRepository.clearAllUserData
```

`core/sync` owns Firestore deletion because it already owns the `users/{uid}/movies` schema. Settings must not import Firestore SDK classes.

## 6. Modules Affected

- `core/auth`
- `core/sync`
- `feature/settings/impl`
- `docs/prd`

## 7. Phases & Tasks

### Phase 1 — Authentication

**Data sources**
- Google ID token returned by the existing `GoogleSignInHelper`.
- Current Firebase user from `FirebaseAuth`.

**Side effects**
- ✅ Allowed: reauthenticate only the current Firebase user and delete that user through existing Firebase Auth APIs.
- ❌ Forbidden: sign in as another user, link accounts, persist tokens, or add backend calls.

- [x] Add `reauthenticateWithGoogle(expectedUid, idToken)` and `deleteAccount(expectedUid)` to Auth datasource/repository contracts.
- [x] Implement reauthentication with `FirebaseUser.reauthenticate(GoogleAuthProvider.getCredential(...))`.
- [x] Verify the current Firebase UID immediately before reauthentication and deletion, preserve cancellation, and return `Result.failure` for missing user, wrong account, or SDK failure.
- [x] Add/update Auth unit tests.

### Phase 2 — Firestore User Data Deletion

**Data sources**
- Authenticated UID supplied by the Settings deletion use case.
- Existing Firestore path `users/{uid}/movies/{movieId}`.

**Side effects**
- ✅ Allowed: read and delete documents only below the supplied user's `movies` collection and delete the empty parent user document.
- ❌ Forbidden: delete other collections, change Firestore rules, add server state, or clear local data.

- [x] Add `deleteUserData(uid)` to `FirestoreMovieDataSource` and `SyncRepository`.
- [x] Fetch all movie document references from `Source.SERVER` and delete them in batches of at most 500 writes.
- [x] Delete the parent `users/{uid}` document after all movie batches succeed.
- [x] Preserve cancellation and stop on the first failed batch.
- [x] Add datasource and repository tests for empty, multi-batch, success, failure, and cancellation paths.

### Phase 3 — Settings Orchestration

**Data sources**
- Current non-anonymous `SantoroUser` already observed by `SettingsViewModel`.
- Google credential requested after the existing confirmation dialog.

**Side effects**
- ✅ Allowed: invoke only the four ordered operations defined in §4 and use existing Settings loading/error UI.
- ❌ Forbidden: add navigation destinations, persistence, recovery state, recent-search cleanup, or mult-account behavior.

- [x] Request the Google credential only after account-deletion confirmation.
- [x] Prevent duplicate deletion jobs while credential/deletion work is active.
- [x] Change `DeleteAccountUseCase` ordering to reauthenticate → Firestore → Auth → Room.
- [x] Return immediately on each failure and never execute a later step.
- [x] Check and propagate the Room cleanup result.
- [x] Keep existing auth-state-driven navigation after successful Auth deletion.
- [x] Update Settings and use-case tests for success, credential failure, and every operation boundary.

### Phase 4 — Documentation And Validation

- [x] Update PRD §3.7 to describe Firestore, Auth, and Room deletion ordering.
- [x] Run affected Auth, Sync, and Settings tests.
- [x] Run `./gradlew test detekt ktlintCheck koverVerify`.
- [x] Run `./gradlew assembleDebug assembleRelease`.
- [ ] Validate successful deletion and one failure path on a real device.
- [x] Record validation results below.

## 8. Validation

| What | Result | Notes |
|------|--------|-------|
| Auth tests | ✅ | Passed with `./gradlew :core:auth:test :core:sync:test :feature:settings:impl:test`. |
| Sync tests | ✅ | Passed with the affected-module test command. |
| Settings tests | ✅ | Passed with the affected-module test command. |
| Full unit/static/coverage validation | ✅ | `./gradlew test detekt ktlintCheck koverVerify assembleDebug assembleRelease` completed successfully. |
| Debug and release builds | ✅ | Both variants assembled successfully; Kotlin daemon failures used Gradle's successful fallback compiler strategy. |
| Real-device deletion | ⏳ | Scheduled for 2026-08-25. |

## 9. Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Delete directly with the Android Firestore SDK. | Avoids a new backend language, billing plan, deployment, and operational ownership. |
| 2 | Reauthenticate before Firestore deletion. | Minimizes the chance that Auth later rejects the destructive action for stale login. |
| 3 | Clear Room last. | Remote failure must not erase the only local copy before the account is deleted. |
| 4 | Accept process-death and cross-device race limitations. | The product requested a small release rather than distributed deletion infrastructure. |

## 10. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-08-24 | Initial client-only deletion plan. |

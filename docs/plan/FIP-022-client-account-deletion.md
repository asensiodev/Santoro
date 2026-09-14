# FIP — Client Account Deletion

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                       |
|------------------------|-------------------------------------------------------------|
| **FIP ID**             | FIP-022                                                     |
| **Version**            | 1.5                                                         |
| **Status**             | ✅ Done                                                     |
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
- Persist a local-cleanup marker before Auth deletion and recover it before exposing app content.
- Preserve coroutine cancellation and expose failures through the existing localized Settings error.
- Cover the new ordering and failure boundaries with existing MockK/Kluent test patterns.

## 3. Non-Goals And Accepted Limits

- No Cloud Functions, TypeScript, Blaze plan, App Check, server locks, or new CI stack.
- No operation history, remote deletion job, or cross-device recovery state.
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
    → acquire in-memory remote-deletion lease
    → recheck expected current UID
    → delete users/{uid}/movies documents from Firestore
    → persist local cleanup pending
    → delete Firebase Auth user
    → app-level recovery clears Room user data
    → clear local cleanup pending
    → existing auth-state navigation returns to Login
```

Credential cancellation, wrong-account reauthentication, Firestore failure, Auth failure, and Room failure use the existing deletion error feedback. No raw SDK error is displayed.

`SettingsViewModel` owns the deletion coroutine. While its remote lease is active and the same UID remains current, app entry retains the authenticated composition so destroying Settings cannot cancel deletion. A full-screen non-dismissible modal blocks all visuals, pointer input, keyboard/D-pad input, back, and accessibility access to that graph. Auth `null` or a different UID unmounts it immediately.

## 5. Architecture

```text
SettingsViewModel
    ├─ GoogleSignInHelper
    └─ DeleteAccountUseCase
          ├─ AuthRepository.reauthenticateWithGoogle
          ├─ AccountDeletionRecoveryRepository remote lease
          ├─ AuthRepository.currentUser expected-UID recheck
          ├─ SyncRepository.deleteUserData
          ├─ AccountDeletionRecoveryRepository.markLocalCleanupPending
          ├─ AuthRepository.deleteAccount

MainActivityViewModel
    ├─ AccountDeletionRecoveryRepository.isLocalCleanupPending
    └─ MovieMutationRepository.clearMovies
```

`core/sync` owns Firestore deletion because it already owns the `users/{uid}/movies` schema. Settings must not import Firestore SDK classes.

## 6. Modules Affected

- `core/auth`
- `core/domain`
- `core/string-resources`
- `core/sync`
- `feature/settings/impl`
- `app`
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
- [x] Verify the current Firebase UID before reauthentication and again after reauthentication immediately before `deleteUserData`; preserve cancellation and return `Result.failure` for missing user, wrong account, or SDK failure.
- [x] Add/update Auth unit tests.

### Phase 2 — Firestore User Data Deletion

**Data sources**
- Authenticated UID supplied by the Settings deletion use case.
- Existing Firestore path `users/{uid}/movies/{movieId}`.

**Side effects**
- ✅ Allowed: read and delete documents only below the supplied user's `movies` collection and delete the empty parent user document.
- ❌ Forbidden: delete other collections, change Firestore rules, add server state, or clear local data.

- [x] Add `deleteUserData(uid)` to `MovieSyncRemoteDataSource` and `SyncRepository`.
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
- [x] Change `DeleteAccountUseCase` ordering to reauthenticate → Firestore → cleanup marker → Auth.
- [x] Return immediately on each failure and never execute a later step.
- [x] Delegate post-Auth Room cleanup to app-level recovery.
- [x] Keep existing auth-state-driven navigation after successful Auth deletion.
- [x] Update Settings and use-case tests for success, credential failure, and every operation boundary.

### Phase 4 — Persistent Local Cleanup Recovery

**Data sources**
- A persisted boolean marker written after Firestore deletion and before Firebase Auth deletion.
- Current Firebase auth state observed by `MainActivityViewModel`.
- Existing Room user data accessed through `MovieMutationRepository`.

**Side effects**
- ✅ Allowed: persist or clear the cleanup marker, block app entry, clear Room, and retry local cleanup.
- ❌ Forbidden: retry remote deletion without a fresh credential, expose Login or authenticated content while cleanup is pending, or use WorkManager for the immediate Room operation.

- [x] Add `AccountDeletionRecoveryRepository` with durable checked writes and an observable pending state.
- [x] Mark cleanup pending before Auth deletion and clear the marker when Auth returns a normal failure.
- [x] Move post-Auth Room cleanup to the app-level lifecycle so Settings removal cannot cancel it.
- [x] Recover a marker found at process start before exposing authenticated content or Login.
- [x] Keep the marker and show a blocking localized Retry UI when Room cleanup or marker clearing fails.
- [x] Add repository, use-case, MainActivity ViewModel, cancellation, failure, and race-boundary tests.

The durable marker remains authoritative once written. The in-memory remote lease covers the earlier remote deletion window and is always released in `finally`. Marker recovery waits for lease completion rather than clearing local state while Firestore deletion is still active.

### Phase 5 — Documentation And Validation

- [x] Update PRD §3.7 to describe Firestore, Auth, and Room deletion ordering.
- [x] Run affected Auth, Sync, and Settings tests.
- [x] Run `./gradlew test detekt ktlintCheck koverVerify`.
- [x] Run `./gradlew assembleDebug assembleRelease`.
- [x] Validate successful deletion and one failure path on a real device.
- [x] Record validation results below.
- [x] Validate navigation-triggered cleanup and process-restart recovery on a real device.

## 8. Validation

| What | Result | Notes |
|------|--------|-------|
| Auth tests | ✅ | Passed with `./gradlew :core:auth:test :core:sync:test :feature:settings:impl:test`. |
| Sync tests | ✅ | Passed with the affected-module test command. |
| Settings tests | ✅ | Passed with the affected-module test command. |
| Full unit/static/coverage validation | ✅ | `./gradlew test detekt ktlintCheck koverVerify assembleDebug assembleRelease` completed successfully. |
| Debug and release builds | ✅ | Both variants assembled successfully; Kotlin daemon failures used Gradle's successful fallback compiler strategy. |
| Real-device deletion | ✅ | Successful deletion and credential-cancellation path validated on a Pixel 9a debug build on 2026-08-30. |
| Persistent cleanup recovery | ✅ | Navigation-triggered cleanup and a persisted pending marker across process restart passed on a Pixel 9a debug build on 2026-08-30. |
| FIP-023 v4.1 compatibility | 🟢 Automated validation complete / manual validation pending | Account deletion still owns the only durable cleanup marker. Room cleanup uses `MovieMutationRepository`; no Auth, marker, and transient lease all make queued sync a terminal no-op. Repository checks prevent normal Firestore/merge work while deletion is active. Aggregate and instrumentation evidence is recorded in FIP-023; manual deletion-blocker validation remains pending. |

## 9. Decisions

### FIP-023 Compatibility - 2026-09-05

FIP-022 remains the only durable recovery marker. Its persisted marker and in-memory remote-deletion lease do not authorize FIP-023 session preparation, explicit logout, or Firestore sync.

The same-UID graph-retention modal is a lifecycle exception, not an authority exception. It preserves the Settings-owned deletion coroutine while making the retained graph non-interactive and inaccessible. Null/different Auth unmounts immediately. Marker recovery waits until the lease ends.

No normal Firestore sync starts or continues past a sync-repository request/chunk boundary while marker or lease is active. A retained feature job may enqueue WorkManager, but workers treat both deletion states as terminal no-ops. Preserving movie uploads has no product value once the user starts deleting the account, including after a failed attempt. The repository rechecks before each Firestore operation/chunk and before transactional merge. A request already accepted by Firebase remains non-revocable.

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Delete directly with the Android Firestore SDK. | Avoids a new backend language, billing plan, deployment, and operational ownership. |
| 2 | Reauthenticate before Firestore deletion. | Minimizes the chance that Auth later rejects the destructive action for stale login. |
| 3 | Clear Room last. | Remote failure must not erase the only local copy before the account is deleted. |
| 4 | Accept process-death and cross-device race limitations. | The product requested a small release rather than distributed deletion infrastructure. |
| 5 | Persist local cleanup before Auth deletion and recover at app entry. | Navigation or process death must not expose retained Room data after destructive remote work. |
| 6 | Use direct app-level recovery instead of WorkManager. | Room cleanup is immediate; the persisted marker already provides restart recovery without deferred scheduling. |
| 7 | Retain the same-UID graph behind a non-dismissible modal during the remote lease. | `SettingsViewModel` owns the coroutine; unmounting it would cancel deletion before the durable marker exists. |
| 8 | Recheck expected current UID after reauthentication immediately before Firestore deletion. | Fresh credentials do not prove that the same app-controlled Auth user remains current after suspension. |

## 10. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-08-24 | Initial client-only deletion plan. |
| 1.1     | 2026-08-30 | Record successful real-device deletion and cancellation validation. |
| 1.2     | 2026-08-30 | Add persistent local-cleanup recovery before production release. |
| 1.3     | 2026-09-01 | Record tested FIP-023 compatibility, cleanup priority, explicit logout confirmation clearing, and idempotent local cleanup. |
| 1.4     | 2026-09-05 | Align compatibility wording with FIP-023 v3.0: FIP-022 is the only durable marker, and its marker plus in-memory deletion lease block session, logout, and sync. |
| 1.5     | 2026-09-05 | Document the Settings-owned deletion lifecycle, same-UID full-screen lease blocker, deferred marker recovery, post-reauth expected-UID check, and strict worker/repository Firestore boundaries. |

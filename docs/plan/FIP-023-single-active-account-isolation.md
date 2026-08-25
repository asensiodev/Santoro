# FIP — Single Active Account Local Isolation

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                               |
|------------------------|---------------------------------------------------------------------|
| **FIP ID**             | FIP-023                                                             |
| **Version**            | 1.0                                                                 |
| **Status**             | 🟡 Draft                                                            |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — F-28 Single Active Account Local Isolation |
| **Feature**            | Isolate one shared Room movie dataset by its active Firebase UID    |
| **Date**               | 2026-08-25                                                          |
| **Author**             | @asensiodev                                                         |
| **Definition of Done** | All checkboxes are marked `[x]` and validation is complete          |

---

> **Execution rule:** Work phase by phase. Stop and ask if implementation requires per-user movie rows, multiple retained account datasets, new remote services, or behavior outside this plan.

## 1. Context & Motivation

Santoro stores watched and watchlist state in one shared Room `movies` table without recording which Firebase UID owns it. Normal logout retains those rows, so a later account can briefly see the previous account's data and may merge or upload it under the wrong UID.

The app will support one active local account dataset instead of full multi-account persistence. A persisted owner UID will identify that dataset. Authentication with a different UID must clear the previous owner's movie rows before authenticated screens or new sync work become available.

## 2. Goals

- Guarantee that a newly authenticated UID never sees Room movie state owned by another UID.
- Keep one shared local movie dataset and one persisted owner UID.
- Retain local movie data across logout and login with the same UID.
- Clear only user-owned movie state when the authenticated UID differs.
- Block authenticated content and sync scheduling until local account preparation succeeds.
- Prevent queued or in-flight work for an old UID from writing into the new owner's dataset.
- Preserve coroutine cancellation and expose a retryable preparation error.

## 3. Non-Goals

- No account selector or simultaneous account sessions.
- No `userId` column on every movie row and no per-user query partitioning.
- No retention of multiple local movie datasets.
- No changes to Firestore document ownership or security rules.
- No clearing of account-neutral browse cache or device-local recent searches.
- No automatic upload or adoption of data whose persisted owner differs from the authenticated UID.

## 4. User Flow

```text
Firebase emits authenticated UID
    → hold authenticated Room-backed screens
    → read persisted local owner UID
        → no owner: adopt current UID and retain existing movies
        → same UID: retain existing movies
        → different UID: clear movies and replace owner UID atomically
    → schedule sync carrying the prepared UID
    → expose authenticated navigation
```

If preparation fails, the app displays a blocking error state with Retry and Sign out. It must never expose the previous owner's Room-backed content.

## 5. Architecture

```text
MainActivityViewModel
    ├─ ObserveAuthStateUseCase
    ├─ PrepareLocalAccountUseCase
    │     └─ DatabaseRepository.prepareLocalAccount(uid)
    └─ SyncScheduler.schedule*(uid)

Room transaction
    ├─ local_account.owner_uid
    └─ movies

Worker(expectedUid)
    ├─ verify current Firebase UID
    ├─ perform remote operation for expectedUid
    └─ verify local owner inside guarded Room write
```

Account preparation is part of the state transition to `Authenticated`; it must not run in an independent collector that races with navigation.

## 6. Data Model

Add a single-row Room account metadata entity containing the owner UID of the shared `movies` dataset. The owner comparison, movie clear, and owner replacement for a UID change must execute in one Room transaction.

Migration behavior for an existing installation with movies but no metadata row is to adopt the currently authenticated UID without clearing. This preserves local data but intentionally cannot repair cross-account contamination that happened before this migration.

`browse_cache` and recent-search storage are not owned by this metadata row.

## 7. Modules Affected

- `core/database`
- `core/sync`
- `app`
- `core/string-resources`
- `docs/prd`

## 8. Phases & Tasks

### Phase 1 — Persist Local Account Ownership

**Data sources**
- Authenticated Firebase UID supplied by the app-level account preparation flow.
- Persisted owner UID from the new Room metadata row.
- Existing user-owned rows in the Room `movies` table.

**Side effects**
- ✅ Allowed: read/write the single owner row and clear only `movies` in one Room transaction.
- ❌ Forbidden: clear browse cache, clear recent searches, partition movie rows, or access Firebase from `core/database`.

- [ ] Add the single-row local account metadata entity and DAO.
- [ ] Add the Room migration and exported schema update.
- [ ] Add `DatabaseRepository.prepareLocalAccount(uid): Result<LocalAccountPreparation>`.
- [ ] Adopt the current UID when no owner exists without clearing existing movies.
- [ ] Return without mutation when the owner already matches.
- [ ] Atomically clear movies and replace the owner when the UID differs.
- [ ] Preserve cancellation and return failures without exposing a partially prepared owner.
- [ ] Add DAO, migration, repository, and preparation use-case tests.

### Phase 2 — Gate Authenticated App Entry

**Data sources**
- `ObserveAuthStateUseCase` emissions.
- Result of local account preparation for the emitted UID.
- User Retry and Sign out actions from the preparation error state.

**Side effects**
- ✅ Allowed: prepare the local owner, update app-level UI state, sign out on explicit action, and schedule sync after preparation succeeds.
- ❌ Forbidden: expose authenticated navigation before preparation, retry indefinitely, or clear data directly from a Composable.

- [ ] Add explicit preparing and retryable error states to the app-level authentication state.
- [ ] Process auth changes with latest-UID semantics so stale preparation cannot authorize an older UID.
- [ ] Expose `Authenticated` only after preparation succeeds for the current UID.
- [ ] Schedule periodic and immediate sync only after successful preparation.
- [ ] Add a localized blocking error UI with Retry and Sign out actions and previews.
- [ ] Add ViewModel and UI tests for adoption, same UID, changed UID, rapid UID changes, failure, retry, sign out, and cancellation.

### Phase 3 — Bind Sync Work To The Prepared UID

**Data sources**
- Expected UID stored in each WorkManager request.
- Current Firebase UID observed when a worker executes.
- Persisted Room owner UID checked at the local write boundary.

**Side effects**
- ✅ Allowed: enqueue work for the prepared UID, reject stale work, and guard Room mutations by owner UID.
- ❌ Forbidden: derive a missing expected UID from a later session, write after an owner mismatch, or upload one UID's local rows under another UID.

- [ ] Require the scheduler to receive and persist the prepared UID in periodic, immediate, and upload work requests.
- [ ] Treat legacy or malformed work without an expected UID as stale and perform no data mutation.
- [ ] Verify expected UID against current Firebase UID before remote work.
- [ ] Recheck ownership at every Room write boundary after remote work.
- [ ] Serialize owner replacement and guarded worker writes through Room transactions.
- [ ] Ensure old queued or in-flight work cannot repopulate or mark rows in a new owner's dataset.
- [ ] Update scheduler, worker, sync repository, and race-boundary tests.

### Phase 4 — Integration And Documentation

**Data sources**
- Existing logout flow and FIP-022 account-deletion flow.
- Existing app authentication journeys.

**Side effects**
- ✅ Allowed: retain owner metadata across normal logout and after account deletion; the next different UID must replace it during preparation.
- ❌ Forbidden: clear movies on normal same-account logout or broaden FIP-022 remote deletion behavior.

- [ ] Verify normal logout followed by the same UID retains local movies.
- [ ] Verify normal logout followed by a different UID clears before content and sync.
- [ ] Verify anonymous-to-Google linking with an unchanged UID retains local movies.
- [ ] Verify FIP-022 Room cleanup remains compatible with retained owner metadata.
- [ ] Add an app journey proving previous-account content is never visible to the next UID.
- [ ] Update PRD F-28 implementation status when complete.

### Phase 5 — Validation

- [ ] Run `./gradlew :core:database:test :core:sync:test :app:test`.
- [ ] Run affected Room migration and app journey instrumentation tests.
- [ ] Run `./gradlew test detekt ktlintCheck koverVerify`.
- [ ] Run `./gradlew assembleDebug assembleRelease`.
- [ ] Validate same-UID and different-UID login flows on a real device.
- [ ] Validate Retry and Sign out from the preparation error state.
- [ ] Record validation results below.

## 9. Validation

| What | Result | Notes |
|------|--------|-------|
| Database and migration tests | ⏳ | |
| Sync stale-work and race tests | ⏳ | |
| App state and journey tests | ⏳ | |
| Full unit/static/coverage validation | ⏳ | |
| Debug and release builds | ⏳ | |
| Real-device account isolation | ⏳ | |

## 10. Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Support one active local dataset instead of per-user Room partitioning. | Meets the isolation requirement with fewer schema and query changes. |
| 2 | Persist the owner UID in Room metadata. | Ownership and movie clearing can share one transactional boundary and survive process death. |
| 3 | Adopt existing movies when the metadata row is first introduced. | Avoids deleting unsynced data during upgrade; historical contamination remains an accepted migration limit. |
| 4 | Gate authenticated navigation on preparation. | Old Room data must never flash before a new UID is ready. |
| 5 | Carry an expected UID in every sync request and guard local writes. | Authentication can change while queued or remote work is running. |
| 6 | Keep owner metadata across logout and account deletion. | Same-UID login can retain data, while a later different UID still triggers an atomic clear. |

## 11. Out Of Scope / Follow-Ups

- Reconsider per-user Room partitioning only if Santoro later adds explicit account switching or offline retention for multiple accounts.
- Historical local contamination before the owner metadata migration is not detectable.

## 12. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-08-25 | Initial single-active-account isolation plan. |

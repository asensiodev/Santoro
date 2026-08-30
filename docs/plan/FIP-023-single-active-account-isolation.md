# FIP — Single Active Account Local Isolation

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                                                                                                     |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| **FIP ID**             | FIP-023                                                                                                                                   |
| **Version**            | 2.2                                                                                                                                       |
| **Status**             | 🟡 Draft — Audited                                                                                                                        |
| **PRD ref**            | [PRD.md](../prd/PRD.md) — F-28 Single Active Account Local Isolation                                                                       |
| **Feature**            | Isolate one shared Room movie dataset and clear it on explicit logout                                                                     |
| **Date**               | 2026-08-25                                                                                                                                |
| **Author**             | @asensiodev                                                                                                                               |
| **Definition of Done** | Every §10 invariant has automated evidence, CI/builds pass, real-device account/logout scenarios pass, and release safety is approved      |

---

> **Execution rule:** Work phase by phase. Establish a green baseline before each phase and mark tasks complete only after their evidence passes.
> Stop and ask if implementation requires per-user movie rows, multiple retained datasets, a generic pending-request queue, a sync-protocol redesign, a new backend, or fail-open authenticated content.

> **Privacy rule:** Unknown or mismatched local ownership fails closed. Authenticated Room-backed content remains hidden until the latest Firebase UID owns the prepared dataset.

## 0. Prerequisites

- Read [FIP-018](./FIP-018-architecture-reliability-refactor.md), especially focused capabilities, cancellation, and process restoration.
- Read completed [FIP-019](./FIP-019-high-risk-test-coverage.md) for WorkManager, migration, real-SQL, journey, and CI conventions.
- Read completed [FIP-022](./FIP-022-client-account-deletion.md) before changing app-entry recovery or cleanup ordering.
- Re-read current auth, Room, sync, logout, account-linking, app-entry, and test code before each affected phase.
- Run affected baselines first and preserve unrelated worktree changes.
- Preserve the accepted migration limitation: no trustworthy Room version-1 schema exists, so trustworthy validation begins at version 2.

## 1. Context And Motivation

Santoro stores watched and watchlist state in one unowned Room `movies` table. Normal logout currently retains those rows. A later Firebase UID can therefore see the previous account's local data or synchronize it under the wrong account.

The same risk exists beyond navigation:

1. `MainActivityViewModel` currently derives authenticated UI and schedules sync through separate collectors.
2. Workers obtain whichever Firebase UID is current when they execute and carry no dataset epoch.
3. Upload reads and download writes are not ownership-guarded transactionally.
4. Foreground movie mutations carry no account identity and can finish after an account switch.
5. Periodic work uses global `KEEP`, which would retain stale request input after account changes.
6. FIP-022 cleanup must invalidate work created before account deletion.

Santoro will keep one local dataset, not support full multi-account persistence. A fixed Room row stores owner UID plus monotonic generation. Together they create an in-memory `LocalDatasetToken(uid, generation)` used to reject stale operations.

Explicit registered logout uses the existing snapshot upload behavior pragmatically:

- Attempt to upload the current local sync snapshot.
- If it succeeds, continue logout without another prompt.
- If it fails, offer Retry, Sign out anyway, or Cancel.
- After success or explicit discard, clear local movies/owner and invalidate the generation.
- A later login, including the same UID, starts empty locally and restores cloud state when sync succeeds.

The app will not claim to know which rows are genuinely pending. Current `updatedAt > 0` means sync-eligible, not unacknowledged. Exact dirty tracking, outbox queues, per-row revisions, per-chunk acknowledgement, and a new Firestore protocol are intentionally deferred.

## 2. Goals

- Prevent a new Firebase UID from seeing another UID's Room movie state.
- Keep one shared dataset identified by persisted owner UID and generation.
- Hide authenticated content until preparation for the latest UID succeeds.
- Clear `movies` and owner UID on completed explicit registered logout.
- Clear previous-owner movies before exposing a different UID.
- Invalidate stale operations across `A → B`, `A → B → A`, explicit same-UID logout/login, and account deletion.
- Guard foreground movie mutations, upload reads, and download writes by the full dataset token.
- Let a user leave when pre-logout synchronization fails through explicit destructive confirmation.
- Recover a confirmed logout after process death without clearing or signing out a later app-controlled account.
- Preserve same-UID anonymous linking and ordinary process recreation.
- Preserve coroutine cancellation and transactional rollback.
- Put new contracts in pure domain code and keep Room/WorkManager/Firebase behind implementations.
- Keep implementation proportional: reuse current last-write-wins snapshot sync instead of redesigning delivery semantics.

## 3. Non-Goals

- No account selector, simultaneous sessions, or multiple retained datasets.
- No `userId` column on each movie and no per-user query partitioning.
- No exact pending/dirty-sync indicator.
- No `localRevision`, `syncedRevision`, dirty boolean, outbox, or generic `PendingApiRequest` table.
- No per-chunk or per-movie upload acknowledgement.
- No Firestore upload-result protocol redesign.
- No change to existing last-write-wins conflict policy.
- No maximum-five retry policy in this FIP; retry redesign requires separate measured evidence.
- No physical individual movie deletion/tombstone redesign.
- No guest-to-existing-account merge.
- No anonymous logout action; current same-UID link and collision-switch behavior remain.
- No clearing on unexplained Firebase auth loss; hide retained ownership until recovery or another UID preparation.
- No clearing of `browse_cache` or recent searches.
- No guarantee that an already accepted Firestore request can be revoked.
- No broad FIP-018 refactor beyond focused contracts required for isolation.

## 4. Invariants And Flows

### App Entry

```text
resolve FIP-022 deletion cleanup
    → resolve confirmed explicit-logout recovery
    → observe latest Firebase auth session through one shared flow
        → null: expose Login only when mandatory cleanup is resolved
        → UID: hide authenticated graph and prepare Room transactionally
            → no owner + no movies: assign UID and advance generation
            → no owner + movies: clear unknown rows, assign UID, advance generation
            → same UID: retain rows and generation
            → different UID: clear movies, replace UID, advance generation
        → revalidate latest auth
        → schedule generation-scoped sync
        → expose Authenticated(token)
```

FIP-022 recovery has strict priority over logout recovery, preparation, scheduling, Login, and authenticated navigation.

### Auth Transitions

| Transition | Required behavior |
|------------|-------------------|
| `null → A` | Prepare A, schedule A's generation, then expose A |
| duplicate `A → A` | No clear, duplicate preparation, or duplicate scheduling |
| process recreation as A | Revalidate and retain the same dataset/token |
| anonymous `A → linked A` | Verify same UID and retain dataset/navigation |
| collision-confirmed `A → B` | Keep existing guest-loss warning; clear A before exposing B |
| explicit registered logout A | Invalidate A, optionally sync, clear locally, then sign out expected A |
| completed logout `A → null → A` | Prepare a new empty A dataset with a newer generation |
| unexpected `A → null` | Hide A but retain local owner/data; no user-authorized discard is inferred |
| unexpected `A → B` | Do not upload after credentials changed; clear A before exposing B |
| `A → B → A` | Generation advances on replacements; old A operations remain stale |

### Token-Scoped Operations

`LocalDatasetToken(uid, generation)` is a pure in-memory value.

- Room validates owner UID and generation in the same transaction as each scoped read or write.
- A token mismatch returns a typed stale/no-op outcome, not a user-visible failure.
- Operational storage/network failures remain failures.
- `CancellationException` always propagates.
- Upload snapshot reads are token-guarded before Firestore.
- Download merge validates the token again before committing local writes.
- Foreground mutations use the token bound when their ViewModel was created.
- WorkManager persists expected generation only; current Firebase UID plus Room ownership reconstructs and validates the token.

### Explicit Registered Logout

Logout starts in app-level orchestration so navigation, Room cleanup, sync, and signout do not depend on a Settings destination remaining alive.

```text
user taps Logout
    → beginLogout(token) transaction
        → validate owner/generation
        → advance generation and return logoutToken(uid, newGeneration)
    → hide/destroy authenticated graph
    → upload current local sync snapshot with logoutToken
```

Advancing generation before upload invalidates old ViewModels/workers. Only the app-owned direct logout snapshot may use `logoutToken`; it is never exposed to feature ViewModels or WorkManager.

**Upload succeeds**

```text
persist confirmed logoutToken
    → clear movies and owner transactionally
    → sign out expected UID
    → clear confirmation
    → expose Login
```

**Upload fails**

Show a durable localized choice:

- Retry: retry the same token-guarded full snapshot.
- Sign out anyway: persist confirmation and continue cleanup without another upload.
- Cancel: retain rows and owner, clear logout UI, and expose a newly keyed graph using `logoutToken`.

Copy must say changes **may** be lost. It must not claim that unsynchronized rows were detected.

Before confirmation, every completion/action rechecks latest auth:

| Current auth | Required result |
|--------------|-----------------|
| expected A | Retry, Sign out anyway, or Cancel remain valid |
| null | Abort unconfirmed logout, retain A hidden, and expose Login |
| different B | Abort A network/UI actions, clear A during B preparation, and never re-expose A |

Pre-confirmation cancellation follows the same latest-auth reconciliation while still propagating `CancellationException`. Any already accepted A remote request remains the documented unavoidable race.

### Confirmed Logout Recovery

A separate `ExplicitLogoutRecoveryRepository` persists the confirmed logout token with checked writes. It is not the FIP-022 deletion boolean.

- Confirmation is persisted only after upload success or explicit discard.
- Process death before confirmation aborts logout safely; startup prepares the advanced same-owner token and schedules a current-generation full snapshot upload.
- Failure to persist confirmation stops before cleanup and offers Retry persistence or safe abort through the latest-auth matrix.
- Matching token cleanup clears movies/owner and is idempotent.
- Already-cleared owner with the expected generation is successful recovery.
- Different current owner/generation is stale and must never be cleared.
- Current auth expected UID: recheck immediately and execute expected-user signout.
- Current auth null: signout already completed.
- Current auth another UID: never sign it out; clear confirmation and prepare latest account after old cleanup is resolved.
- Once confirmation is persisted, cleanup is mandatory and offers Retry only; Cancel is no longer valid.
- If signout reports failure, re-read auth. Null finalizes; another UID is preserved; expected A clears confirmation, prepares a new empty dataset, and shows a localized signout error so logout can be attempted again.
- Failure to clear confirmation after completed cleanup/signout remains retryable and blocks new account entry until resolved.
- FIP-022 marker always wins and may reuse only the idempotent Room clear primitive, not logout marker semantics.

### Expected-User Signout

- Recheck current Firebase UID immediately before the synchronous local `signOut()` call.
- Null means signout already completed; a different UID is stale and must not be signed out.
- Firebase exposes no atomic `signOut(expectedUid)` and already-issued auth tasks may settle after caller cancellation. This narrow SDK race is accepted and reconciled by the authoritative auth observer plus Room generation guards.
- FIP-022 deletion retains orchestration precedence.

### Account Deletion

FIP-022 cleanup:

```text
durable deletion marker
    → clear movies
    → clear owner UID
    → advance generation once
    → clear any logout confirmation
    → clear deletion marker
```

Repeated cleanup after marker-clear failure remains idempotent.

## 5. Architecture

```text
Presentation
    MainActivity / feature ViewModels
        │ intents + LocalDatasetToken
        ▼
Domain (pure Kotlin)
    LocalAccountRepository
    AccountScopedMovieMutationRepository
    AccountScopedSyncStore
    SyncScheduler
    ExplicitLogoutRecoveryRepository
    ExpectedUserSignOut use case
        ▲
        │ implementations
Data / Infrastructure
    core/database: owner, generation, guarded transactions
    core/sync: current Firestore snapshot sync + WorkManager
    core/auth: expected-user signout/link guards
    app/data: checked explicit-logout confirmation persistence
```

App-level orchestration:

```text
FIP-022 recovery
    → explicit logout recovery
    → shared auth observation
    → account preparation
    → sync scheduling
    → Authenticated(token)
    → graph keyed by token
```

- `app` remains the composition root.
- Settings emits logout callback/intents; it does not coordinate Room, WorkManager, and Firebase directly.
- Room owns owner/generation atomicity through `SantoroRoomDatabase.withTransaction`.
- Domain imports no Android, Room, WorkManager, Firebase, DTO, entity, or `core/database` type.
- Presentation depends on `SyncScheduler`, not `WorkManagerSyncScheduler`.
- Broad `DatabaseRepository` consumers migrate only where scoped isolation requires it.
- Token passes through authenticated composable/route-builder parameters, never route data.
- Mutation-capable Hilt ViewModels receive token through assisted injection and bind it once.
- A mutable latest-token singleton is forbidden.
- Authenticated `NavController`, back stack, and ViewModels are keyed by UID plus generation.

## 6. Data Model And Migration

Room advances from version 5 to version 6 with one fixed-row entity:

```text
local_account_state
    slot: INTEGER NOT NULL PRIMARY KEY
    owner_uid: TEXT NULL
    generation: INTEGER NOT NULL
```

- Repository-managed slot is always `0`.
- Kotlin and WorkManager generation type is `Long`.
- `MIGRATION_5_6` creates the table and inserts owner null, generation `0L`.
- Fresh database creation inserts the same row.
- Migration code never reads Firebase.
- Structural migration preserves version-5 movies and browse cache.
- First preparation clears non-empty ownerless movies because their owner cannot be proven.
- Empty ownerless preparation assigns current UID and checked-increments generation.
- Same-owner preparation retains rows/generation unless explicit logout already cleared owner.
- Different-owner preparation clears movies, replaces owner, and checked-increments generation atomically.
- `beginLogout(token)` checked-increments generation while retaining owner/rows and returns the new logout token.
- Confirmed logout cleanup clears movies/owner without another generation increment.
- Account deletion clears movies/owner and increments generation if not already invalidated by an idempotent prior cleanup.
- Blank UID, missing/duplicate row, negative/overflow generation, or malformed confirmation fails closed.
- No revision/dirty columns are added to `movies`.
- No destructive migration fallback.

## 7. WorkManager And Sync

Current final-state last-write-wins behavior is retained.

- Per-movie upload requests remain; add expected generation input.
- Periodic and immediate requests carry expected generation.
- Immediate authenticated-session sync explicitly uploads the current full local snapshot before download/merge. This recovers rows whose older per-movie work became stale after generation invalidation or process death.
- Workers obtain current Firebase UID, construct an in-memory token, and require Room owner/generation match.
- Periodic unique work changes from `KEEP` to `UPDATE` so current generation replaces legacy input.
- Immediate and per-movie upload retain current `REPLACE` semantics.
- Missing/invalid generation or upload movie ID is terminal success/no-op.
- Remove `UploadWorker`'s malformed-request fallback to bulk upload.
- Guard single-movie and bulk snapshot reads by token before Firestore.
- Guard download merge by token before local commit.
- Existing operational retry behavior remains; retry limits are not redesigned here.
- Direct logout calls a renamed/clarified guarded `uploadLocalSnapshot(token)` contract rather than claiming exact pending detection.
- Partial bulk success followed by failure remains safe to retry because Firestore timestamp writes are idempotent.
- Cancel after logout failure and pre-confirmation process recovery schedule the same current-generation immediate full snapshot.
- WorkManager cancellation is defense in depth; token guards provide correctness.

## 8. Modules Affected

- `core/domain`: token, preparation, stale outcome, focused repository/scheduler/logout/auth contracts and use cases.
- `core/database`: Room version 6, fixed row, migration, preparation, guarded mutation/snapshot/merge/cleanup.
- `core/sync`: generation input, worker guards, periodic `UPDATE`, scheduler boundary, guarded snapshot API.
- `core/auth`: expected-UID signout and same-UID linking guards.
- `app`: authoritative recovery/auth/preparation/logout state machine, confirmation persistence, token propagation, Hilt, journeys.
- `feature/settings/impl`: logout callback and app-level choice rendering integration; remove direct long-lived logout orchestration.
- `feature/movie-detail/impl`: token-bound mutations and generation-scoped upload scheduling.
- `feature/watchlist/impl`: token-bound removal and generation-scoped upload scheduling.
- Other mutation features found in Phase 1 only.
- `core/string-resources`: English/Spanish sync-failure logout choice and recovery copy.
- `core/design-system` only if existing components cannot express three accessible actions cleanly.
- `architecture-tests`: domain purity and concrete-infrastructure boundaries.
- `.github/workflows/ci.yml` only if adopted tasks are not explicit.
- PRD, FIP-022 compatibility notes, and release guide after implementation validation.

No new Gradle module is planned.

## 9. Phases And Tasks

### Phase 1 — Baseline And Inventory

**Data sources**
- Current Room 5 schemas, auth, sync, WorkManager, logout, linking, deletion recovery, and tests.

**Side effects**
- ✅ Allowed: read-only inventory, baseline tests, schema hashes, failing regression tests.
- ❌ Forbidden: production changes before scoped callers and existing work policies are inventoried.

- [ ] Record green affected JVM, migration, instrumentation, architecture, Paparazzi, and journey baselines.
- [ ] Inventory every user-owned mutation, upload read, download write, scheduler call, worker input, auth mutation, logout caller, fake, and Hilt binding.
- [ ] Confirm WorkManager periodic `UPDATE` and Hilt assisted creation support at pinned versions.
- [ ] Record schema-5 hash and trusted migration paths.
- [ ] Add failing regressions for ownerless data, `A → B → A`, stale foreground mutation, stale upload read, stale download commit, periodic `KEEP`, logout sync failure choices, and process-death recovery.

### Phase 2 — Domain And Transactional Room Ownership

**Data sources**
- Authenticated UID, fixed owner/generation row, immutable dataset token.

**Side effects**
- ✅ Allowed: update fixed row and clear/mutate only `movies` inside transactions.
- ❌ Forbidden: Firebase access from Room, account-neutral clearing, entity leakage, or non-transactional token checks.

- [ ] Add pure `LocalDatasetToken`, preparation disposition, and scoped stale/outcome models in `core/domain`.
- [ ] Add focused local-account, scoped-mutation, scoped-sync-store, scheduler, explicit-logout-recovery, and expected-user-auth contracts/use cases.
- [ ] Add fixed-slot local account entity and DAO.
- [ ] Bump Room to version 6, register `MIGRATION_5_6`, initialize fresh database, and export `6.json`.
- [ ] Inject `SantoroRoomDatabase` and use `withTransaction` for preparation, begin logout, guarded operations, and cleanup.
- [ ] Implement privacy-first ownerless cleanup, same-owner retention, owner replacement, generation invalidation, and idempotent deletion/logout cleanup.
- [ ] Implement token-guarded foreground mutation, upload snapshot, and downloaded merge boundaries.
- [ ] Preserve cancellation through rollback and keep stale distinct from failure.
- [ ] Add real Room creation, 5→6/trusted 2→6 migration, fixed-row, rollback, concurrency, browse-cache, begin-logout, and idempotent-cleanup tests.

### Phase 3 — Auth And WorkManager Guards

**Data sources**
- Expected generation in WorkManager, current Firebase UID, persisted Room owner/generation.

**Side effects**
- ✅ Allowed: replace legacy request input, no-op stale work, and recheck expected UID immediately before signout.
- ❌ Forbidden: derive authority without Room validation, sign out mismatched app-controlled UID, or retry malformed/stale work.

- [ ] Add generation input to periodic, immediate, and per-movie upload requests.
- [ ] Change periodic work to `UPDATE`; preserve current immediate/upload `REPLACE` behavior.
- [ ] Treat missing/invalid generation/movie ID and stale owner as success/no-op.
- [ ] Remove malformed upload fallback to bulk sync.
- [ ] Pass validated token into guarded upload/download repository operations.
- [ ] Make immediate authenticated-session sync upload the guarded full snapshot before download/merge.
- [ ] Add expected-UID signout recheck immediately before Firebase signout and same-UID validation after successful anonymous linking.
- [ ] Add a regression where an accepted canceled auth task settles later; rely on authoritative auth reconciliation and generation guards rather than claiming atomic SDK serialization.
- [ ] Add scheduler, worker, repository, auth, legacy-input, stale-race, failure, and cancellation tests.

### Phase 4 — Authoritative App Entry And Logout

**Data sources**
- One shared auth observation, FIP-022 marker, optional confirmed logout token, preparation result, app intents.

**Side effects**
- ✅ Allowed: recover mandatory cleanup, prepare latest account, begin/cancel/confirm logout, clear expected dataset, sign out expected UID, schedule after preparation.
- ❌ Forbidden: independent auth routing/scheduling collectors, fail-open content, feature-owned cleanup after auth change, or signout of mismatched app-controlled account.

- [ ] Replace independent auth-routing/scheduling pipelines with one authoritative latest-session orchestration and one shared auth listener.
- [ ] Give FIP-022 strict priority over logout recovery and preparation.
- [ ] Model Loading/Preparing, Authenticated(token), Unauthenticated, deletion error, logout syncing, logout sync failure, cleanup error, and signout error as immutable durable state where acknowledgement is required.
- [ ] Implement `beginLogout` generation invalidation and immediate authenticated-graph disposal.
- [ ] Attempt guarded `uploadLocalSnapshot(logoutToken)` once per Retry action.
- [ ] On upload success, persist confirmation before cleanup.
- [ ] On failure, offer Retry, Sign out anyway, and Cancel with wording that some changes may be missing.
- [ ] On discard, persist confirmation without another upload.
- [ ] On Cancel, expose the same rows under the new logout token and schedule a current-generation immediate full snapshot.
- [ ] Reconcile every pre-confirmation completion, action, and cancellation against current auth A/null/B before restoring or retrying A.
- [ ] Stop before cleanup if confirmation persistence fails; offer Retry persistence or safe abort.
- [ ] Recover confirmed cleanup/signout idempotently after process death.
- [ ] Make post-confirmation cleanup mandatory Retry-only and make confirmation-clear failure block new entry until resolved.
- [ ] After signout failure, re-read auth and finalize null/B safely or restore expected A as an empty dataset with localized failure.
- [ ] Revalidate latest auth before scheduling/publishing Authenticated.
- [ ] Add ViewModel/app tests for every transition, process boundary, rapid intent, failure, cancellation, FIP precedence, and scheduler failure.

### Phase 5 — Token-Bound Features And UI

**Data sources**
- Token carried by authenticated composition and app-level logout state.

**Side effects**
- ✅ Allowed: assisted token binding, guarded existing mutations, localized three-action failure UI.
- ❌ Forbidden: latest-token singleton, route token, direct Room/Firebase orchestration in feature, or claims of exact pending data.

- [ ] Key complete authenticated graph/back stack by full token.
- [ ] Pass token through composable/route-builder parameters and Hilt assisted ViewModel creation callbacks.
- [ ] Bind one token per mutation-capable ViewModel lifetime.
- [ ] Pass token to guarded mutation and upload scheduling without changing current mutation UX.
- [ ] Replace Settings direct upload/signout coroutine with app-owned logout callback.
- [ ] Add localized sync-before-logout failure copy using “may be lost” wording.
- [ ] Add Retry, Sign out anyway, and Cancel actions with loading/disabled behavior, 48dp targets, semantics, and `@PreviewLightDark`.
- [ ] Add English/Spanish Compose/Paparazzi tests and app journeys.
- [ ] Add deterministic feature tests proving stale mutation cannot commit or schedule after generation change.

### Phase 6 — Integration, Documentation, And Release

- [ ] Verify successful explicit logout clears movies/owner and does not clear browse cache/recent searches.
- [ ] Verify same-UID login after logout starts empty and restores only remote state.
- [ ] Verify process recreation and same-UID anonymous link retain data.
- [ ] Verify collision/different UID clears before content; unexpected null retains hidden ownership.
- [ ] Verify FIP-022 cleanup clears logout confirmation and remains idempotent.
- [ ] Add categorical telemetry only for preparation, logout outcome, stale reason, and sanitized failure; never UID/movie/token.
- [ ] Scan logs/reports/WorkManager metadata for sensitive fixtures.
- [ ] Add dated FIP-022 compatibility note and update PRD F-28 after validation.
- [ ] Run affected JVM and instrumentation tests.
- [ ] Run `./gradlew :architecture-tests:test` and adopted Paparazzi tasks.
- [ ] Run `./gradlew test detekt ktlintCheck koverVerify assembleDebug assembleRelease`.
- [ ] Confirm CI API 35 boundary/journey job passes.
- [ ] Validate sync success, offline/failure Retry/Sign out anyway/Cancel, cleanup/signout recovery, A/B, same-UID relogin, linking/collision, and deletion on a real device.
- [ ] Install signed AAB from Internal and repeat smoke tests.
- [ ] Use staged Production rollout and forward-only schema-6 hotfix recovery.
- [ ] Record commands, device/API, counts, commit, CI URL, Internal version, rollout decision, and limits in §11.

## 10. Acceptance And Traceability

| Invariant | Required evidence |
|-----------|-------------------|
| Ownerless upgrade rows cannot cross accounts | Real Room 5→6 and preparation test |
| Fresh install has exactly one valid owner row | Room creation/fixed-row test |
| Owner replacement and cleanup are atomic | Real Room rollback/cancellation tests |
| `A → B → A` cannot revive old work | Generation repository/worker/journey tests |
| Old foreground mutation cannot repopulate B | Controlled feature race test |
| Upload A cannot read B rows | Guarded snapshot test proving no Firestore call |
| Download A cannot write after B preparation | Guarded merge rollback test |
| Legacy/current periodic work uses latest generation | Scheduler `UPDATE` test |
| Malformed/stale work no-ops without retry | Worker input/outcome tests |
| Logout invalidates old ViewModels/work before snapshot | Begin-logout generation and graph-disposal tests |
| Upload success clears and signs out | App/repository/journey tests |
| Upload failure offers Retry/Sign out anyway/Cancel | State/UI/journey tests |
| Discard performs no second upload | Mock verification and journey test |
| Cancel preserves rows under new generation | Room/app graph recreation test |
| Process death before confirmation safely aborts logout | App recreation test |
| Confirmed process-death recovery completes cleanup/signout | Recovery tests at each boundary |
| A recovery rechecks UID before signout and reconciles later auth changes | Expected-user auth race/observer test |
| Same-UID relogin after logout starts empty | Journey and real-device test |
| Process recreation without logout retains dataset | App recreation test |
| Same-UID link retains; collision switch clears | Auth/Profile integration and real-device tests |
| FIP-022 cleanup wins and remains idempotent | Combined recovery tests |
| Token change destroys old graph/ViewModels | Nav/back-stack/assisted-VM journey test |
| Domain stays pure and presentation avoids concrete infrastructure | Architecture tests |
| No sensitive identity/content reaches diagnostics | Observability/report scan |

## 11. Validation

| What | Result | Notes |
|------|--------|-------|
| Baseline and caller inventory | ⏳ | |
| Domain and architecture contracts | ⏳ | |
| Room 5→6 and trusted 2→6 migrations | ⏳ | Version 1 remains unclaimed |
| Ownership/generation transaction races | ⏳ | |
| Auth and worker generation guards | ⏳ | |
| App entry/logout recovery | ⏳ | |
| Foreground mutation isolation | ⏳ | |
| Logout failure UI and Paparazzi | ⏳ | |
| Settings/link/collision/deletion integration | ⏳ | |
| App journeys and process recreation | ⏳ | |
| Full unit/static/coverage validation | ⏳ | |
| Debug and release builds | ⏳ | |
| CI API 35 instrumentation | ⏳ | |
| Real-device validation | ⏳ | |
| Internal Play-installed smoke test | ⏳ | |
| Sensitive-output review | ⏳ | |
| Staged rollout and monitoring | ⏳ | |

## 12. Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Keep one active local dataset, not per-user partitioning. | Fits product scope with fewer schema/query changes. |
| 2 | Identify it with UID plus monotonic generation. | UID alone cannot invalidate A→B→A or same-UID relogin after logout. |
| 3 | Clear non-empty ownerless legacy rows. | Their owner cannot be proven. |
| 4 | Clear movies/owner on completed explicit logout. | Logout is a shared-device privacy boundary. |
| 5 | Advance generation at begin logout. | Immediately invalidates old features/work while allowing one app-owned final snapshot. |
| 6 | Reuse current full snapshot last-write-wins sync. | Proportional to Santoro's small final-state dataset. |
| 7 | Do not claim exact pending detection. | Current schema has no durable acknowledgement. |
| 8 | Show destructive choice only after sync failure. | Avoids permanent logout blocking without adding dirty tracking. |
| 9 | Persist confirmation only after sync success or explicit discard. | Process death before user commitment safely aborts logout. |
| 10 | Clear locally before expected-user signout. | Before confirmation logout is cancellable; after confirmation cleanup is mandatory, retryable, and process-recoverable while expected A remains authenticated. |
| 11 | Keep a separate checked logout confirmation from FIP-022. | Deletion and logout have different recovery semantics. |
| 12 | Put pure contracts in `core/domain`. | Preserves Clean Architecture. |
| 13 | Validate token in the same transaction as scoped operations. | Eliminates account TOCTOU windows. |
| 14 | Bind immutable token per ViewModel and key graph by it. | Prevents stale UI/back-stack authority. |
| 15 | Persist generation only in WorkManager input. | Avoids UID residue while Room validates current identity. |
| 16 | Change periodic `KEEP` to `UPDATE`; otherwise preserve work/retry policy. | Fixes stale generation without unrelated delivery redesign. |
| 17 | Do not add outbox, revisions, acknowledgements, or retry cap now. | Their cost exceeds the current product need. |
| 18 | Do not clear on unexplained auth null. | Auth loss is not discard consent. |
| 19 | Recheck expected UID immediately before signout and reconcile later auth emissions. | Proportional best effort for Firebase's non-atomic global signout API. |
| 20 | Preserve accepted already-issued Firestore race. | Client cleanup cannot revoke accepted remote work. |
| 21 | Release schema 6 progressively and recover forward only. | Schema-5 APK cannot reopen migrated state safely. |

## 13. Risks And Accepted Limits

- The app may show a sync-failure warning even when all local rows were already remote because exact pending state is intentionally unknown.
- Sign out anyway may lose unresolved local changes; copy and user confirmation make that explicit without claiming that no partial upload succeeded.
- Same-account offline continuity is intentionally lost after completed logout.
- Full snapshot upload may resend already-synced rows; Firestore timestamps keep it idempotent for Santoro's expected dataset size.
- Partial upload followed by failure may have saved some rows; retry is safe and discard may lose only the unresolved remainder.
- Current timestamp conflict policy and cross-device clock skew remain unchanged.
- Firestore requests accepted before logout/discard may still complete remotely.
- Unexpected auth switch cannot offer old-account sync after credentials changed.
- WorkManager operational retries remain current behavior; bounded retry is a separate follow-up if measured.
- Explicit logout confirmation temporarily stores UID/generation in private app storage; backup remains disabled and data is cleared on finalization.
- Firebase signout is not an atomic expected-UID compare-and-set; observer reconciliation remains necessary after the immediate UID recheck.
- Version-1 migration provenance remains untrusted.
- Rollback requires a newer schema-6-compatible build.

## 14. Observability And Privacy

Allowed categorical dimensions:

- Preparation disposition.
- Logout result: sync success, sync failure, discard, cancel, cleanup/signout failure.
- Stale reason and migration version.

Forbidden:

- UID, email, credentials, auth token, logout-confirmation payload, WorkManager input.
- Movie ID/title/list, exact row count, timestamps, search text, or database snapshots.
- Raw exception messages that may contain forbidden values.

UID may exist only in Firebase auth, fixed private Room ownership, transient checked logout confirmation, and in-memory token. WorkManager persists generation only. No UID/movie ID is added to new logs, analytics, Crashlytics keys, routes, screenshots, or generated reports.

## 15. Out Of Scope And Follow-Ups

- Exact dirty/pending state if product later needs a sync-status indicator.
- Full outbox/API-request queue for non-reducible future commands.
- Per-row sync acknowledgement and chunk-level delivery reporting.
- Bounded retry policy after measuring actual failures/battery behavior.
- Server-controlled conflict sequencing, sync leases, or cross-device logout.
- Guest explicit logout/reset and guest-to-existing-account merge.
- Per-user databases or encrypted key destruction.
- General repository segregation remains in FIP-018.
- Automatic Production deployment remains outside FIP-024.

## 16. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-08-25 | Initial single-active-account isolation plan. |
| 2.0     | 2026-08-30 | Audited auth, Room, sync, WorkManager, logout, linking, deletion recovery, architecture, privacy, migration, and rollout. |
| 2.1     | 2026-08-30 | Explored clear-on-logout with exact revision-backed pending sync and durable transition protocol. |
| 2.2     | 2026-08-30 | Simplified to owner/generation isolation, current snapshot upload, failure-only Retry/Discard/Cancel UX, and minimal confirmed-logout recovery. |

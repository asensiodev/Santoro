# FIP - Single Active Account Local Isolation

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 - see docs/LICENSE -->

| Field | Value |
|---|---|
| **FIP ID** | FIP-023 |
| **Version** | 4.1 |
| **Status** | 🟢 Local Validation Complete / External Validation Pending |
| **PRD ref** | [PRD.md](../prd/PRD.md) - F-28 Single Active Account Local Isolation |
| **Feature** | Clear the shared Room movie dataset at account boundaries |
| **Date** | 2026-09-05 |
| **Author** | @asensiodev |

## 1. Context

Santoro stores watched and watchlist state in one Room `movies` table. Released schema 5 has no account owner. Rows left after logout or account replacement could otherwise be displayed to, or uploaded for, another Firebase account.

Earlier local iterations added an owner row, schema 6, expected-UID WorkManager data, account-scoped repository outcomes, assisted feature ViewModels, and logout recovery machinery. None of those iterations shipped. Version 4.0 removes them and keeps the proportional boundary below.

## 2. Goals

- Hide authenticated content immediately when Auth becomes null or changes UID.
- Clear Room movies before exposing Login or publishing a replacement account.
- Clear again on the first login after an observed null to remove late local writes.
- Keep normal same-UID process recreation and duplicate Auth updates inexpensive.
- Prevent remote work captured for one UID from continuing under a different current Auth user.
- Preserve FIP-022 as the only durable destructive-operation recovery protocol.
- Keep Room schema 5 and avoid owner columns, per-user databases, generations, tokens, or outboxes.

## 3. Session Flow

`MainActivityViewModel` observes Auth through one replaying `MutableSharedFlow<SantoroUser?>` and processes entry with `collectLatest`.

| Transition | Behaviour |
|---|---|
| Upgrade or authenticated process recreation as A | Trust that Firebase still represents A, retain movies, and schedule sync. |
| `A -> A` | Update user and onboarding state in place without clearing or rescheduling. |
| `A -> null` | Hide A, clear movies, then expose Login. |
| `null -> B` | Clear movies again, schedule current-user sync, then publish B. |
| Direct `A -> B` | Hide A, clear movies, then schedule and publish B. |
| `A -> B -> A` | `collectLatest` cancels intermediate entry work; the sticky transition-cleanup flag makes the final entry clear before publication. |
| Anonymous A linked to Google A | Expected-UID linking retains the same dataset because the UID does not change. |
| Anonymous collision | Capture A before credential selection, sign out expected A after confirmation, return to Login, and let a later explicit login enter normally. |

Movie cleanup is fail-closed. Failure shows the blocking local-cleanup error and Retry repeats the latest session request. `CancellationException` propagates.

The upgrade policy deliberately trusts continuity of the persisted Firebase account. Schema-5 rows are retained and may be uploaded to that current account. Supporting an out-of-band account replacement during APK upgrade would require ownership metadata or destructive cleanup that would penalize the expected same-account user, so that case is an accepted limitation. Account changes observed by the running implementation still follow the cleanup rules above.

Santoro does not expose direct authenticated account replacement. Supported switching passes through Auth null and Login, so the first cleanup completes before another account can authenticate and login performs a second cleanup. If a future feature or external integration introduces direct `A -> B` replacement, queued workers could adopt B before app-entry cleanup while Room still contains A. That unsupported transition must reopen the session-authority design before release; owner metadata and a persistent prepared-session UID remain intentionally out of the current product scope.

## 4. Logout And Account Deletion

Registered logout is a direct expected-UID signout owned by `SettingsViewModel`:

```text
capture current registered UID
    -> SignOutUseCase(expectedUid)
    -> SignedOut or NoAuthenticatedUser: Auth-null flow performs Room cleanup
    -> mismatch, failure, or throw: remain signed in and show settings_logout_error
```

There is no pre-logout snapshot, alternate logout action, confirmation state machine, or durable logout marker. A very recent local mutation whose WorkManager upload has not run can be lost on logout; this is an accepted local-first tradeoff.

FIP-022 remains stronger because account deletion crosses an irreversible remote boundary. Its durable `local_cleanup_pending` marker and in-memory deletion lease retain their existing ordering and blocking UI. Marker recovery clears movies before clearing the marker.

## 5. Room And Sync

- `MovieMutationRepository` owns local movie mutations and `clearMovies()`.
- `SyncStore` snapshots local state and merges downloaded state.
- `RoomDatabaseRepository` implements both contracts with Room transactions.
- Cleanup deletes only `movies`; `browse_cache` and recent searches remain account-neutral.
- Room remains schema 5. Schema 6 and `MIGRATION_5_6` are absent because they never shipped.
- Feature ViewModels use normal `@HiltViewModel @Inject`; no UID is carried through routes, saved state, assisted factories, or feature navigation.
- WorkManager input contains only sync mode or movie ID. Workers obtain current Firebase Auth at execution time.
- Queued workers do not carry local-session readiness. This is safe for supported null-mediated account changes, but is not a guarantee for unsupported direct authenticated UID replacement.
- No Auth, a durable FIP-022 cleanup marker, or an active account-deletion lease is a terminal no-op. Once a user is deleting the account, preserving queued movie uploads has no credible product value.
- `DefaultSyncRepository` rechecks Auth plus FIP-022 before each Firestore request or upload chunk.
- Download authority is also checked inside the same Room transaction as the merge. Room serializes that transaction with cleanup: a preceding cleanup makes the check fail, while a following cleanup removes the completed old-account merge.
- Already accepted Firebase requests cannot be revoked.

Firestore decoding isolates each document. Invalid required ID/title, out-of-range IDs, or wrong-typed present optional fields skip that document. Missing optionals use defaults. Malformed genre JSON in a valid string becomes an empty genre list without dropping the movie.

## 6. Non-Goals

- Multiple retained account datasets or account switching UI.
- `owner_uid`, per-row UID, owner tables, Room schema 6, generations, or dataset tokens.
- Expected UID persisted in WorkManager.
- Dirty flags, outbox, acknowledgement protocol, or guaranteed upload-before-logout.
- Fail-open authenticated UI while cleanup is required.
- A second durable recovery marker beyond FIP-022.
- Product-level direct switching between two authenticated UIDs.

## 7. Implementation

- [x] Replace owner-scoped domain contracts with `MovieMutationRepository`, `SyncStore`, `SyncRepository`, and `SyncScheduler`.
- [x] Restore Room schema 5 and remove owner entity, DAO, migration, guards, and owner-only tests.
- [x] Implement latest-wins Auth-null, post-null login, direct UID-change, and duplicate-UID behaviour.
- [x] Record and test the accepted same-account upgrade assumption by retaining movies on authenticated recreation.
- [x] Simplify registered logout and anonymous collision handling.
- [x] Remove UID from feature construction, navigation, scheduler input, and workers.
- [x] Revalidate Auth/FIP-022 at Firestore boundaries and transactionally at downloaded merge.
- [x] Treat sync work encountered during account deletion as a terminal no-op.
- [x] Update English and Spanish logout failure copy.
- [x] Add focused JVM and Room race coverage.
- [x] Verify replacement UI and new sync scheduling remain blocked until cleanup finishes.
- [x] React to persisted guest-onboarding state without cleanup or duplicate sync scheduling.
- [x] Run the complete aggregate/static/coverage/build gate for the v4.1 onboarding correction.
- [x] Run current API 37 Room/app/feature instrumentation.
- [ ] Validate logout, account collision, deletion blocker, and same-account upgrade manually on a device.
- [ ] Validate through CI API 35 and the exact Internal artifact before release.

## 8. Validation

Validation evidence for the v4.1 onboarding correction:

- `./gradlew test detekt ktlintCheck koverVerify assembleDebug assembleRelease`: passed with 2447 tasks after the v4.1 onboarding correction and product-scope decision.
- `./gradlew :core:database:connectedDebugAndroidTest :feature:movie-detail:impl:connectedDebugAndroidTest :feature:watched-movies:impl:connectedDebugAndroidTest :feature:watchlist:impl:connectedDebugAndroidTest :app:connectedJourneyTestAndroidTest`: passed on Pixel_9a API 37 with Database 25/25, Movie Detail 1/1, Watched 1/1, Watchlist 2/2, and app journeys 10/10.
- `./gradlew :app:connectedJourneyTestAndroidTest`: v4.1 Hilt/session rerun passed 10/10 after the onboarding correction.
- `git diff --check`: passed.
- The known Kotlin daemon `NoSuchMethodError` used Gradle's successful fallback compiler. The existing R8 kotlinx-serialization rule warning remained non-fatal.

Subsequent guest-confirmation and pre-commit cleanup:

- Guest entry now requires confirmation before anonymous authentication. Login UI tests cover cancellation without sign-in and exactly one sign-in after confirmation.
- Account collision uses the shared English/Spanish resources, explicitly states that lists are not merged, and returns to Login after confirmation. Redundant feature-local strings were removed.
- The account-deletion blocker now uses an opaque surface while retaining the underlying composition.
- Focused Login instrumentation, Login unit tests, App/Login Detekt and ktlint passed after the dialog change. Settings unit tests and ktlint passed after resource consolidation.
- The full aggregate result above predates these final UI/resource changes. Manual account linking, collision, deletion-blocker, and Internal-artifact checks remain pending.

On 2026-09-14 the user reported that their manual tests passed and requested commit/push. Individual scenarios and the tested artifact were not enumerated, so external validation is not marked complete. The next-session repetition and outstanding release checks are tracked in [GUIDE-account-isolation-retest.md](../guides/GUIDE-account-isolation-retest.md).

## 9. Decisions

| # | Decision | Rationale |
|---|---|---|
| 1 | Clear one shared movie table at identity boundaries. | It is the smallest enforceable isolation model for the current product. |
| 2 | Trust same-account continuity across app upgrade. | It preserves the expected user's local state and avoids infrastructure for an unlikely out-of-band account replacement. |
| 3 | Recheck merge authority inside the Room transaction. | It closes the check-to-write race without global mutexes or ownership metadata. |
| 4 | Use current Auth in workers. | Work payloads remain minimal and obsolete queued work cannot carry another UID. |
| 5 | Drop queued sync encountered during account deletion. | A user deleting the account has no product need for pending movie uploads, even if that deletion attempt fails. |
| 6 | Keep direct logout without snapshot. | Isolation comes from Auth-null cleanup; guaranteed upload would require a larger acknowledgement design. |
| 7 | Keep FIP-022 durability only. | Account deletion has an irreversible remote/local gap; ordinary logout does not justify another protocol. |
| 8 | Treat direct authenticated account replacement as unsupported. | Current logout and collision flows pass through null and complete cleanup before Login; ownership infrastructure is disproportionate until the product introduces direct switching. |

## 10. Version History

| Version | Date | Summary |
|---|---|---|
| 2.x | 2026-08-25 to 2026-09-05 | Unshipped owner-generation and durable logout iterations. |
| 3.x | 2026-09-05 | Unshipped owner-only schema 6 and simplified logout iterations. |
| 4.0 | 2026-09-05 | Remove ownership architecture; use controlled runtime cleanup, trusted same-account upgrades, current-Auth sync, and transactional merge authority. |
| 4.1 | 2026-09-06 | Make the direct-replacement worker boundary an explicit unsupported-product limitation and reactively correct delayed guest-onboarding state. |

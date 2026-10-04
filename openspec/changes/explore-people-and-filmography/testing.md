# Person Exploration Testing Follow-up

Date: 2026-10-04.

## Scope and test boundaries

The user requested more extensive tests and TDD for future features. The previously approved person-exploration design already specifies screen/UI, state/retry/cancellation, repository data and connected navigation boundaries. This follow-up reuses that agreement and adds regression coverage at those boundaries. The optional boundary question does not introduce a new production contract. The repository-wide deferred FIP-020 is not marked complete or executed by this feature follow-up.

The original feature had Compose interaction tests, Paparazzi snapshots and app journeys; its development was not consistently test-first. These additional tests exercise already implemented behavior. No artificial red or retrospective TDD claim is made. Future features and fixes must follow the test-first workflow documented in the testing guide and referenced from AGENTS.md and OpenSpec task rules.

## Changes

| Class or supporting file | Behavior protected / purpose |
|---|---|
| `PersonDetailScreenTest` | Adds profile loading and error behavior, Back in both states, profile retry, empty filmography, omitted acting/crew sections, crew movie callbacks and role labels, short biography controls, independent credits loading, recovery recomposition, saved biography expansion and Spanish empty biography resources. Existing three interaction tests remain. |
| `PersonDetailViewModelTest` | Adds pending details state, biography availability before credits complete, duplicate credits recovery suppression, pending profile cancellation and cancellation delivered through a credits Result. Existing six tests remain. |
| `PersonRepositoryHttpTest` | New local HTTP integration tests through the real repository, Retrofit, Gson and mapper: complete wire decoding, grouped multi-role acting/crew credits, HTTP details failure/recovery, HTTP credits failure/recovery, identity mismatch rejection and malformed JSON result failure. Expected domain values are independently specified literals. |
| `.github/workflows/ci.yml` | Adds person Compose instrumentation to the existing emulator job, explicit person Paparazzi verification and visual failure artifacts. This configures CI; remote execution remains unverified locally. |
| `AGENTS.md` | Requires test-first feature/fix work and reading the testing guide; establishes English for engineering documentation and test names. |
| `docs/guides/GUIDE-testing-and-tdd.md` | Defines agreed test boundaries, genuine behavioral red/green evidence, regression coverage, Compose test selection and completion criteria. |
| `docs/README.md` | Makes the testing workflow discoverable. |
| `openspec/config.yaml` | Requires future task plans to use vertical test-first slices and record red/green commands and outcomes. |

No production class, resource or screenshot baseline is changed by this follow-up. Existing navigation journeys and six approved person screenshots are reused rather than duplicated.

## Validation

- Added 22 regression tests: 11 Compose interaction/state tests, 5 ViewModel tests and 6 HTTP integration tests.
- Current person JVM suite: 34 tests, zero failures/errors/skips. Includes mapper 5, repository unit 5, service 1, repository HTTP 6, ViewModel 11 and screenshots 6.
- Android test compilation and person ktlintCheck pass. Initial line-length and unresolved-import failures were corrected; these were setup failures, not behavioral TDD red.
- Pixel_9a API 37: all 14 Compose interaction tests and all 15 app journeys pass; zero failures/errors/skips.
- Six person screenshots pass independent verification, and person detekt passes. The six screenshots are included in the 34 JVM count; verification does not add six unique cases.
- CI configuration parses successfully with the system Ruby YAML parser; remote GitHub Actions execution remains unverified.
- OpenSpec strict validation and git diff whitespace validation pass.

Exact wrapper questions, nested commands and bounded answers are recorded in [testing-validation.md](testing-validation.md). The wrapper workflow finished successfully and removed only its own logs.

Keep the separate existing GenreChip mismatch and manual/live-service/process-restoration gates open. No production changes were needed, so no new aggregate release-readiness claim is made.

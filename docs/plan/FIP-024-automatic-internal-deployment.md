# FIP — Automatic Internal Testing Deployment

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field                  | Value                                                                  |
|------------------------|------------------------------------------------------------------------|
| **FIP ID**             | FIP-024                                                                |
| **Version**            | 1.0                                                                    |
| **Status**             | 🟡 Draft                                                               |
| **Guide ref**          | [GUIDE-ci-cd-release.md](../guides/GUIDE-ci-cd-release.md)              |
| **Feature**            | Publish a verified release AAB automatically to Play Internal testing  |
| **Date**               | 2026-08-30                                                             |
| **Author**             | @asensiodev                                                            |
| **Definition of Done** | All checkboxes are marked `[x]` and the first Internal upload is tested |

---

> **Execution rule:** Work phase by phase. Keep deployment disabled until the dry run, permission audit, release signing, and artifact verification all pass. Stop and ask before granting broader Google Cloud or Google Play permissions, adding a long-lived service-account key, or enabling Production deployment.

## 0. Prerequisites

- Google Play app signing and the existing upload key remain available.
- The release Firebase configuration is available without committing `google-services.json`.
- The developer can administer GitHub environments, Google Cloud IAM, and Santoro permissions in Play Console.
- Follow the security controls and setup details in `GUIDE-ci-cd-release.md`.

## 1. Context & Motivation

Santoro currently validates source changes in GitHub Actions but builds and uploads Play releases manually. A local debug build does not prove that the release bundle is signed correctly, receives the release Firebase configuration, passes release shrinking, or can be delivered by Play.

The Internal testing artifact should be built once from a trusted `main` commit, verified, uploaded automatically, installed through Play, and then promoted manually to Production without rebuilding. This removes artifact drift while keeping Production release authority outside CI.

## 2. Goals

- Deploy to Play Internal testing only after all existing CI and instrumented tests pass.
- Build and sign one traceable release AAB from the exact trusted commit.
- Generate a unique, monotonically increasing Play `versionCode` without editing source for every CI run.
- Authenticate GitHub to Google Cloud with short-lived Workload Identity credentials.
- Restrict the publishing identity to Santoro testing tracks and exclude Production access.
- Verify the AAB signature, record its SHA-256 checksum, and provide sanitized deployment metadata.
- Promote the tested Internal artifact manually to Production without rebuilding it.

## 3. Non-Goals

- No automatic Production deployment or staged Production rollout.
- No long-lived Google service-account JSON key.
- No deployment from pull requests, forks, untrusted branches, or local developer machines.
- No public retention of the AAB, R8 mapping, keystore, credentials, or access tokens.
- No replacement of existing verification jobs or reduction of release checks.

## 4. Release Flow

```text
Trusted push to main
    → static analysis, unit tests, coverage, and debug build
    → instrumented boundary and journey tests
    → generate unique versionCode
    → reconstruct release Firebase config and upload keystore
    → authenticate through Workload Identity
    → build and sign release AAB
    → lintVitalRelease and strict signature verification
    → calculate SHA-256
    → upload to Play Internal testing
    → install from Play and smoke-test
    → manually promote the same artifact to Production
```

## 5. Architecture

```text
GitHub Actions (trusted main push)
    ├─ existing verification jobs
    ├─ existing instrumented-tests job
    └─ deploy-internal job
          ├─ GitHub environment: internal
          ├─ OIDC → Google Workload Identity
          ├─ testing-only Play service account
          ├─ Gradle release signing
          └─ Google Play Internal track

Google Play Console
    └─ manual Internal → Production promotion
```

The deployment job must use `contents: read` and `id-token: write` only. Secret-bearing third-party actions must be reviewed and pinned to immutable commit SHAs.

## 6. Files And Systems Affected

- `.github/workflows/ci.yml`
- `build-logic/convention`
- GitHub environment `internal`
- GitHub Actions environment secrets and variables
- Google Cloud IAM and Workload Identity Federation
- Google Play Console users, permissions, and Internal testing
- `docs/guides/GUIDE-ci-cd-release.md` if implementation details change

## 7. Phases & Tasks

### Phase 1 — GitHub Deployment Boundary

- [ ] Protect `main` with required existing CI checks.
- [ ] Create the GitHub environment `internal` and restrict it to `main`.
- [ ] Add repository variable `INTERNAL_DEPLOY_ENABLED=false` as a safety switch.
- [ ] Add required environment variable names without storing credentials in repository variables.
- [ ] Confirm pull requests and forks cannot access deployment secrets.

### Phase 2 — Google Cloud And Play Identity

- [ ] Enable Google Play Android Developer API and IAM Service Account Credentials API.
- [ ] Create a dedicated `santoro-play-internal` service account without private keys.
- [ ] Configure a Workload Identity provider restricted to Santoro's numeric repository and owner IDs plus `refs/heads/main`.
- [ ] Allow only the repository-specific principal to impersonate the publishing service account.
- [ ] Grant the service account access only to Santoro and testing-track releases in Play Console.
- [ ] Verify it cannot publish to Production, access other apps, or view financial data.

### Phase 3 — Secrets, Signing, And Versioning

- [ ] Store upload-keystore content, alias, passwords, and release Firebase JSON only in the `internal` GitHub environment.
- [ ] Add Gradle `VERSION_CODE` property override with the version catalog as the local fallback.
- [ ] Generate a unique monotonic CI version code and document the irreversible selected range.
- [ ] Verify local fallback and CI override bundle metadata.
- [ ] Reconstruct secret files only under `$RUNNER_TEMP` and remove them in an `always()` step.

### Phase 4 — Internal Deployment Job

- [ ] Add `deploy-internal` after every existing verification and instrumented-test job.
- [ ] Restrict execution to trusted pushes to `main` with the safety switch enabled.
- [ ] Prevent a newer push from canceling an in-progress Play upload.
- [ ] Authenticate through OIDC using a reviewed action pinned to an immutable SHA.
- [ ] Build `bundleRelease lintVitalRelease` with secret Gradle properties supplied through environment variables.
- [ ] Verify the AAB signature strictly and calculate its SHA-256 checksum.
- [ ] Upload only to the configured Internal track with a reviewed action pinned to an immutable SHA.
- [ ] Write a sanitized summary containing commit, version, track, checksum, and result.
- [ ] Do not publish the AAB or R8 mapping as public GitHub artifacts.

### Phase 5 — Dry Run And First Deployment

- [ ] Validate workflow syntax, job dependencies, permissions, conditions, and immutable action references.
- [ ] Run release signing, shrinking, lint, signature, and metadata checks while deployment remains disabled.
- [ ] Audit the workflow from a secret-exposure and compromised-action perspective.
- [ ] Enable `INTERNAL_DEPLOY_ENABLED` only after the audit passes.
- [ ] Complete the first automatic Internal upload from `main`.
- [ ] Confirm Play displays the expected commit-derived version code and version name.
- [ ] Install from the Internal tester link and complete the release smoke test.
- [ ] Verify the service account still has no Production permission after deployment.

### Phase 6 — Promotion And Documentation

- [ ] Record the deployed commit SHA, AAB checksum, version code, and smoke-test result.
- [ ] Promote the exact tested Internal artifact manually to Production without rebuilding.
- [ ] Add English and Spanish release notes and review Play warnings and declarations.
- [ ] Monitor Crashlytics after Internal testing and Production promotion.
- [ ] Update the CI/CD guide and this FIP with final operational details.

## 8. Validation

| What | Result | Notes |
|------|--------|-------|
| GitHub environment and branch restrictions | ⏳ | |
| OIDC authentication and least-privilege audit | ⏳ | |
| Version-code override and bundle metadata | ⏳ | |
| Release signing and strict signature verification | ⏳ | |
| Automatic Internal upload | ⏳ | |
| Play-installed smoke test | ⏳ | |
| Same-artifact Production promotion | ⏳ | |

## 9. Decisions

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | Deploy automatically only to Internal testing. | CI can improve repeatability without receiving Production authority. |
| 2 | Promote manually to Production. | Production remains an explicit developer decision after smoke testing and Crashlytics review. |
| 3 | Use Workload Identity instead of a service-account JSON key. | Short-lived credentials reduce secret lifetime and rotation risk. |
| 4 | Generate CI version codes while retaining a local catalog fallback. | Every accepted Play upload is unique without making local builds depend on CI. |
| 5 | Verify and promote one AAB instead of rebuilding. | Internal testing must exercise the exact binary delivered to Production. |
| 6 | Keep deployment behind a repository safety switch during rollout. | Workflow code and infrastructure can be reviewed before the first upload. |

## 10. Out Of Scope / Follow-Ups

- Automatic Production deployment may be reconsidered only through a separate approved FIP.
- Release notes automation and staged Production rollout remain manual.
- Dependency provenance or SLSA attestation can be evaluated separately after the basic deployment path is stable.

## 11. Changelog

| Version | Date       | Summary |
|---------|------------|---------|
| 1.0     | 2026-08-30 | Initial plan for secure automatic Internal testing deployment. |

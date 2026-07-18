# Guide - CI/CD Release Automation

| Field | Value |
|---|---|
| **Type** | Educational setup and operations guide |
| **Scope** | GitHub Actions to Google Play Internal testing |
| **Production gate** | Manual promotion in Google Play Console |
| **Repository visibility** | Public |
| **Date** | 2026-07-18 |
| **Author** | @asensiodev |

---

## 1. Purpose

This guide teaches how to build a secure Android Continuous Integration and Continuous Delivery pipeline while configuring it for Santoro.

The target behavior is:

```text
Feature branch
      |
      v
Pull request
      |
      v
CI verification
      |
      v
Merge to main
      |
      v
Generate versionCode
      |
      v
Build and sign one AAB
      |
      v
Automatically upload to Internal testing
      |
      v
Manual smoke test
      |
      v
Manual Play Console promotion
      |
      v
Production at 100%
```

GitHub Actions will never receive permission to publish Santoro to Production. Production remains a deliberate action performed by the developer in Google Play Console.

### Learning objectives

After completing this guide, you should understand:

- The difference between CI, Continuous Delivery, and Continuous Deployment.
- Why an Android artifact should be built once and promoted unchanged.
- How Google Play tracks and version codes work.
- How GitHub Actions authenticates to Google Cloud without a private JSON key.
- How to sign Android releases on an ephemeral runner.
- How to protect secrets in a public repository.
- How to troubleshoot and reproduce this setup in another Android project.

---

## 2. Core Concepts

### 2.1 Continuous Integration

Continuous Integration validates code whenever it is proposed or merged.

Santoro's existing `.github/workflows/ci.yml` already performs CI by running:

- ktlint.
- Detekt.
- Unit tests.
- Architecture tests.
- Debug compilation.
- Instrumented tests.

CI answers this question:

> Is this code safe to integrate into `main`?

### 2.2 Continuous Delivery

Continuous Delivery creates a tested, signed, deployable artifact and makes it available for release while retaining a manual Production decision.

For Santoro, Continuous Delivery means:

- Every successful code merge to `main` creates a uniquely versioned AAB.
- The AAB is signed with the upload key.
- The AAB is uploaded automatically to Internal testing.
- Production requires a manual promotion in Play Console.

Continuous Delivery answers this question:

> Is the exact artifact ready for a human to release?

### 2.3 Continuous Deployment

Continuous Deployment publishes automatically to Production without human approval.

Santoro will not use Continuous Deployment.

### 2.4 Build once, promote unchanged

The AAB tested through Internal testing must be the AAB promoted to Production.

Do not rebuild for Production. A rebuild can differ because of dependency resolution, generated files, environment configuration, signing, timestamps, or an accidental source change.

Google Play promotion changes which track references an uploaded version. It does not require another upload or another `versionCode`.

### 2.5 Deployment is not release

Deployment makes software available in an environment. Release exposes it to users.

In this pipeline:

```text
Upload to Internal = automated deployment
Promote to Production = manual release decision
```

---

## 3. Google Play Model

### 3.1 Tracks

Google Play tracks represent audiences:

| Track | Audience | Santoro usage |
|---|---|---|
| Internal testing | Small trusted tester list | Automatic destination |
| Closed testing | Managed test cohort | Not required by this pipeline |
| Open testing | Public beta users | Not required by this pipeline |
| Production | All eligible users | Manual promotion at 100% |

### 3.2 Version name

`versionName` is user-facing and follows Semantic Versioning:

```text
1.0.30
1.0.31
1.1.0
```

Use a patch increment for compatible fixes and a minor increment for meaningful compatible features.

### 3.3 Version code

`versionCode` is an internal monotonically increasing integer used by Android and Google Play.

Google Play permanently consumes a version code once an artifact is uploaded. Drafts, rejected release attempts, and removed releases do not make the code reusable.

For this reason, CI will generate deployment version codes instead of requiring manual edits.

The local catalog value remains a fallback for developer builds. CI overrides it with:

```text
VERSION_CODE_BASE + GITHUB_RUN_NUMBER * 100 + GITHUB_RUN_ATTEMPT
```

Example:

```text
VERSION_CODE_BASE  = 100000
GITHUB_RUN_NUMBER  = 143
GITHUB_RUN_ATTEMPT = 1

versionCode = 100000 + 143 * 100 + 1
versionCode = 114301
```

Multiplying by 100 reserves retry values for each workflow run. Gaps are harmless.

If the workflow is deleted or recreated and its run number resets, move `VERSION_CODE_BASE` to a new range above every code already uploaded to Play.

---

## 4. Public Repository Security Model

A public repository can safely use CI/CD. Workflow definitions are expected to be public. Credentials are not.

### 4.1 Safe public information

The following values are identifiers, not credentials:

- Package name.
- Internal track name.
- Google Cloud project ID and project number.
- Workload Identity provider resource name.
- Service-account email.
- Version-code formula.
- Build and upload commands.

### 4.2 Secret information

Never commit or print:

- Upload keystore contents.
- Keystore passwords.
- Service-account private-key JSON.
- OAuth access tokens.
- Temporary Google credentials.
- Firebase administrative credentials.
- Secret values transformed into another encoding.

`google-services.json` contains project identifiers and client API keys, not a Firebase administrative private key. Firebase security must rely on Authentication, security rules, API restrictions, and App Check where appropriate. Santoro still keeps these files out of Git and supplies them through GitHub secrets.

### 4.3 Base64 is not encryption

The upload keystore will be Base64-encoded because GitHub secrets store text. Base64 only changes representation. GitHub's encrypted secret storage provides the protection.

### 4.4 Fork and pull-request behavior

GitHub does not send repository secrets to workflows triggered from forks. That is useful but not sufficient as the only control.

The release job must also enforce all of the following:

- Trigger only after a trusted push to `main`.
- Never run for `pull_request` events.
- Never use `pull_request_target` to execute pull-request code.
- Reference the `internal` GitHub environment.
- Request only `contents: read` and `id-token: write` permissions.

### 4.5 Third-party actions

A third-party action used by a signing job can read that job's files and environment. Pin every action in the deployment job to a full immutable commit SHA, not only a mutable tag such as `@v1`.

The guide may show `<PINNED_COMMIT_SHA>` placeholders. Resolve and review the current release before implementing the workflow.

### 4.6 Public artifacts

Do not upload the R8 mapping file as a generally accessible artifact from a public repository. The Crashlytics Gradle plugin already uploads the release mapping during a configured release build.

The AAB is not a credential, but Santoro does not need to retain it publicly because Google Play stores the uploaded artifact. Record its checksum and version metadata in the GitHub job summary.

---

## 5. Threats and Controls

| Threat | Control |
|---|---|
| Pull request attempts to read signing secrets | Deployment job runs only on trusted `main` pushes |
| Malicious fork | Fork workflows receive no repository secrets |
| Compromised action tag | Secret-bearing actions are pinned to commit SHAs |
| Leaked Google JSON key | Workload Identity uses short-lived credentials instead |
| Accidental Production deployment | Service account receives testing permission only |
| Secret printed in logs | Secrets are passed through environment variables and never inspected |
| Keystore committed accidentally | `*.jks` and `*.keystore` are ignored |
| Duplicate version code | CI generates a unique monotonic code |
| New push cancels an upload midway | Main deployment is not canceled in progress |
| Concurrent Google Play edits | A dedicated Internal deployment concurrency group serializes uploads |
| Production defect | Test internally, monitor Crashlytics, publish a corrected higher version |
| Repository compromise | Use 2FA, branch protection, least privilege, and credential rotation |

---

## 6. Final Credential Inventory

### 6.1 GitHub environment secrets

Create these in the `internal` GitHub environment:

| Secret | Description |
|---|---|
| `ANDROID_UPLOAD_KEYSTORE_BASE64` | Base64-encoded upload keystore |
| `ANDROID_UPLOAD_KEY_ALIAS` | Upload-key alias |
| `ANDROID_UPLOAD_STORE_PASSWORD` | Keystore password |
| `ANDROID_UPLOAD_KEY_PASSWORD` | Private-key password |
| `GOOGLE_SERVICES_RELEASE_JSON` | Release Firebase configuration |

### 6.2 GitHub environment variables

| Variable | Example |
|---|---|
| `PLAY_PACKAGE_NAME` | `com.asensiodev.santoro` |
| `PLAY_TRACK` | `internal` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | `projects/123456789/locations/global/workloadIdentityPools/github/providers/santoro` |
| `GCP_PLAY_SERVICE_ACCOUNT` | `santoro-play-internal@example.iam.gserviceaccount.com` |
| `VERSION_CODE_BASE` | `100000` |

### 6.3 Repository safety variable

Create this as a repository variable so the workflow can be installed without immediately uploading:

| Variable | Initial value |
|---|---|
| `INTERNAL_DEPLOY_ENABLED` | `false` |

Change it to `true` only after the dry run and permission audit succeed.

---

## 7. Phase 1 - Prepare GitHub

### Concept

A GitHub environment groups deployment credentials and records deployments separately from ordinary CI jobs.

### Manual setup

- [ ] Open the Santoro repository on GitHub.
- [ ] Go to **Settings -> Rules -> Rulesets** and protect `main`.
- [ ] Require pull requests and the existing CI status checks before merge.
- [ ] Do not require another person's approval when the repository has only one developer.
- [ ] Go to **Settings -> Environments**.
- [ ] Create an environment named `internal`.
- [ ] Limit deployment branches to protected branches or specifically to `main`.
- [ ] Do not configure a required reviewer because Internal deployment is automatic.
- [ ] Go to **Settings -> Secrets and variables -> Actions -> Variables**.
- [ ] Create `INTERNAL_DEPLOY_ENABLED` with value `false`.

GitHub Free supports environments for public repositories.

### Verification

- [ ] Confirm the environment is named exactly `internal`.
- [ ] Confirm only `main` is allowed to deploy.
- [ ] Confirm unverified code cannot be merged to `main`.
- [ ] Confirm no credentials have been placed in repository variables.

### What you learned

Secrets are encrypted values. Variables are visible configuration. Environments control when deployment-specific values become available.

---

## 8. Phase 2 - Configure Google Cloud

### Concept

GitHub Actions supports OpenID Connect. A workflow can present a signed GitHub identity token to Google Cloud. Google validates the repository and branch claims, then returns temporary credentials.

This is Workload Identity Federation.

### 8.1 Create or select a Google Cloud project

- [ ] Open [Google Cloud Console](https://console.cloud.google.com/).
- [ ] Select a project dedicated to deployment automation or an existing controlled project.
- [ ] Record the project ID.
- [ ] Record the numeric project number.

The Workload Identity resource uses the project number, not the project ID.

### 8.2 Enable APIs

- [ ] Enable the [Google Play Android Developer API](https://console.cloud.google.com/apis/library/androidpublisher.googleapis.com).
- [ ] Enable the IAM Service Account Credentials API.

### 8.3 Create a service account

- [ ] Go to **IAM & Admin -> Service Accounts**.
- [ ] Create `santoro-play-internal`.
- [ ] Do not create a JSON key.
- [ ] Do not grant Project Owner or Editor.
- [ ] Record the service-account email.

Google Cloud project roles do not grant Google Play access. Play access is configured separately in Play Console.

### 8.4 Create a Workload Identity pool

- [ ] Go to **IAM & Admin -> Workload Identity Federation**.
- [ ] Create a pool such as `github-actions`.
- [ ] Add an OpenID Connect provider such as `santoro-github`.
- [ ] Set the issuer URL to `https://token.actions.githubusercontent.com`.

Obtain Santoro's stable numeric GitHub identifiers:

```bash
gh api repos/asensiodev/Santoro \
  --jq '{repository_id: .id, owner_id: .owner.id}'
```

Numeric IDs are safer than repository names for authorization because names can change or, after deletion, potentially be reused.

Use these attribute mappings:

```text
google.subject                = assertion.sub
attribute.repository          = assertion.repository
attribute.repository_id       = assertion.repository_id
attribute.repository_owner_id = assertion.repository_owner_id
attribute.ref                 = assertion.ref
attribute.workflow            = assertion.workflow
```

Use a provider attribute condition that restricts identity to Santoro's immutable repository and owner IDs plus `main`:

```text
assertion.repository_id == 'REPOSITORY_ID' && assertion.repository_owner_id == 'OWNER_ID' && assertion.ref == 'refs/heads/main'
```

### 8.5 Allow service-account impersonation

Grant `roles/iam.workloadIdentityUser` on the service account to the repository principal from the pool.

The principal normally has this shape:

```text
principalSet://iam.googleapis.com/projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL_ID/attribute.repository_id/REPOSITORY_ID
```

Do not grant the role to every identity in the pool if the repository-specific principal is available.

### Verification

- [ ] Confirm the service account has no private keys.
- [ ] Confirm the provider condition names only Santoro's numeric IDs and `main`.
- [ ] Confirm the repository principal can impersonate only the publishing service account.
- [ ] Copy the provider resource name for the GitHub variable.

### What you learned

Authentication proves identity. Authorization defines what that identity may do. Workload Identity handles authentication; Google Play permissions handle authorization.

---

## 9. Phase 3 - Configure Google Play

### Concept

The service account must be invited to the Google Play developer account like another user. It should receive only the permissions required to publish Santoro to testing tracks.

### Manual setup

- [ ] Open [Google Play Console](https://play.google.com/console).
- [ ] Go to **Users and permissions**.
- [ ] Invite the service-account email.
- [ ] Grant access only to Santoro.
- [ ] Grant permission to view app information required for releases.
- [ ] Grant permission to release to testing tracks.
- [ ] Do not grant Production release permission.
- [ ] Do not grant financial, order, subscription, account-management, or administrator permissions.

Google may adjust permission labels over time. Review the permission description, not only its name.

### Verification exercise

Answer these questions before continuing:

1. Can the service account upload to Internal testing?
2. Can the service account access another app in the developer account?
3. Can the service account publish to Production?
4. Can the service account view financial data?

The correct answers are `yes`, `no`, `no`, and `no`.

### What you learned

Least privilege limits the impact of a workflow or credential compromise. Even a fully compromised Internal workflow must not be able to publish to Production.

---

## 10. Phase 4 - Add GitHub Values

### 10.1 Encode the upload keystore

On macOS, send the encoded keystore directly to GitHub CLI without creating a repository file:

```bash
base64 -i "$HOME/keystores/santoro-release.jks" \
  | gh secret set --env internal ANDROID_UPLOAD_KEYSTORE_BASE64
```

Base64 output must never be pasted into an issue, pull request, workflow, documentation, or chat.

### 10.2 Add password secrets

Run each command and enter the value when GitHub CLI prompts:

```bash
gh secret set --env internal ANDROID_UPLOAD_KEY_ALIAS
gh secret set --env internal ANDROID_UPLOAD_STORE_PASSWORD
gh secret set --env internal ANDROID_UPLOAD_KEY_PASSWORD
gh secret set --env internal GOOGLE_SERVICES_RELEASE_JSON
```

To load the Firebase file without displaying it:

```bash
gh secret set --env internal GOOGLE_SERVICES_RELEASE_JSON \
  < app/src/release/google-services.json
```

### 10.3 Add environment variables

```bash
gh variable set --env internal PLAY_PACKAGE_NAME \
  --body "com.asensiodev.santoro"

gh variable set --env internal PLAY_TRACK \
  --body "internal"

gh variable set --env internal VERSION_CODE_BASE \
  --body "100000"

gh variable set --env internal GCP_WORKLOAD_IDENTITY_PROVIDER \
  --body "projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL_ID/providers/PROVIDER_ID"

gh variable set --env internal GCP_PLAY_SERVICE_ACCOUNT \
  --body "santoro-play-internal@PROJECT_ID.iam.gserviceaccount.com"
```

### Verification

List names only:

```bash
gh secret list --env internal
gh variable list --env internal
```

- [ ] Confirm every required name exists.
- [ ] Confirm the secret command does not reveal values.
- [ ] Confirm `INTERNAL_DEPLOY_ENABLED` remains `false`.

### What you learned

Secret names are safe to document. Secret values should be write-only during normal operations.

---

## 11. Phase 5 - Support CI Version Codes

Santoro currently reads `versionCode` from `gradle/libs.versions.toml`. The build logic should allow a Gradle property override while retaining that catalog value as a local fallback.

Conceptual implementation:

```kotlin
fun Project.getVersionCode() =
    providers
        .gradleProperty("VERSION_CODE")
        .orElse(libs.findVersion("versionCode").get().toString())
        .get()
        .toInt()
```

CI can then run:

```bash
./gradlew bundleRelease -PVERSION_CODE=114301
```

The keystore credentials continue to use the existing property names in `app/build.gradle.kts`.

GitHub passes secret Gradle properties through environment variables so they do not appear in command-line logs:

```text
ORG_GRADLE_PROJECT_KEYSTORE_STORE_FILE
ORG_GRADLE_PROJECT_KEYSTORE_KEY_ALIAS
ORG_GRADLE_PROJECT_KEYSTORE_STORE_PASSWORD
ORG_GRADLE_PROJECT_KEYSTORE_KEY_PASSWORD
```

### Verification

- [ ] Run a local release build with a temporary higher `-PVERSION_CODE`.
- [ ] Inspect the generated bundle metadata.
- [ ] Confirm the catalog file did not change.
- [ ] Confirm a build without the override still uses the catalog value.

### What you learned

Build configuration can have a stable local default and a deployment-specific CI override without committing generated release numbers.

---

## 12. Phase 6 - Internal Deployment Job

The deployment job belongs after all existing CI jobs and uses the `internal` environment.

### Required job boundary

```yaml
deploy-internal:
  name: Deploy to Internal testing
  needs:
    - verification
    - instrumented-tests
  if: >-
    github.event_name == 'push' &&
    github.ref == 'refs/heads/main' &&
    vars.INTERNAL_DEPLOY_ENABLED == 'true'
  runs-on: ubuntu-latest
  environment: internal
  permissions:
    contents: read
    id-token: write
  concurrency:
    group: google-play-internal
    cancel-in-progress: false
```

The existing workflow-level concurrency should cancel stale pull-request runs but must not cancel a running `main` release upload.

### Required steps

1. Check out the exact trusted commit.
2. Set up Java and Gradle.
3. Generate a version code.
4. Reconstruct Firebase configuration.
5. Reconstruct the upload keystore in `$RUNNER_TEMP`.
6. Authenticate to Google Cloud with OIDC.
7. Build and sign the release AAB.
8. Run `lintVitalRelease`.
9. Verify the signature strictly.
10. Calculate the SHA-256 checksum.
11. Upload to Internal testing.
12. Write a sanitized job summary.
13. Remove temporary files in an `always()` step.

### Version-code generation

```bash
version_code=$((VERSION_CODE_BASE + GITHUB_RUN_NUMBER * 100 + GITHUB_RUN_ATTEMPT))
printf 'VERSION_CODE=%s\n' "$version_code" >> "$GITHUB_ENV"
```

`VERSION_CODE_BASE` is configuration, not a secret.

### Reconstruct the keystore safely

```bash
printf '%s' "$ANDROID_UPLOAD_KEYSTORE_BASE64" \
  | base64 --decode \
  > "$RUNNER_TEMP/santoro-upload.jks"
```

Never run `cat`, `keytool -list -v`, or diagnostic commands that print sensitive keystore metadata unless specifically required and reviewed.

### Authenticate with Workload Identity

The deployment job will use `google-github-actions/auth` pinned to an immutable commit:

```yaml
- name: Authenticate to Google Cloud
  id: google-auth
  uses: google-github-actions/auth@<PINNED_COMMIT_SHA>
  with:
    workload_identity_provider: ${{ vars.GCP_WORKLOAD_IDENTITY_PROVIDER }}
    service_account: ${{ vars.GCP_PLAY_SERVICE_ACCOUNT }}
    create_credentials_file: true
```

The resulting credential file contains temporary credentials and exists only on the ephemeral runner.

### Build the exact release artifact

```bash
./gradlew bundleRelease lintVitalRelease -PVERSION_CODE="$VERSION_CODE"
```

Secret Gradle properties are supplied as job environment variables. Do not place passwords in the command.

### Verify the artifact

```bash
jarsigner -verify -strict \
  app/build/outputs/bundle/release/app-release.aab

sha256sum \
  app/build/outputs/bundle/release/app-release.aab
```

### Upload to Internal

The upload action must be pinned to a reviewed commit SHA:

```yaml
- name: Upload to Google Play Internal testing
  uses: r0adkll/upload-google-play@<PINNED_COMMIT_SHA>
  with:
    serviceAccountJson: ${{ steps.google-auth.outputs.credentials_file_path }}
    packageName: ${{ vars.PLAY_PACKAGE_NAME }}
    releaseFiles: app/build/outputs/bundle/release/app-release.aab
    tracks: ${{ vars.PLAY_TRACK }}
    status: completed
```

Do not configure `production`, `userFraction`, or Production credentials in this workflow.

### Sanitized summary

The GitHub job summary should record:

- Commit SHA.
- `versionName`.
- Generated `versionCode`.
- Track.
- AAB SHA-256.
- Build and upload result.

It must not record credential values, keystore paths outside `$RUNNER_TEMP`, or temporary access tokens.

---

## 13. Phase 7 - Dry Run

Keep `INTERNAL_DEPLOY_ENABLED=false` while implementing and reviewing the deployment job.

### Repository verification

- [ ] Validate workflow YAML syntax.
- [ ] Confirm the job requires every CI job.
- [ ] Confirm the job condition excludes pull requests.
- [ ] Confirm Production is not mentioned as a target.
- [ ] Confirm action references use immutable SHAs.
- [ ] Confirm permissions are `contents: read` and `id-token: write` only.
- [ ] Confirm secrets are not used in `if` expressions.
- [ ] Confirm temporary files use `$RUNNER_TEMP`.

### Build verification

- [ ] Verify version-code override behavior.
- [ ] Verify release signing.
- [ ] Verify R8 completes.
- [ ] Verify `lintVitalRelease` passes.
- [ ] Verify strict AAB signature validation.
- [ ] Verify no secret or mapping file is uploaded as a public artifact.

### Security exercise

Read the workflow as if you were an attacker. For every secret-bearing step, ask:

1. Can pull-request code reach this step?
2. Can this command print a secret?
3. Can a third-party action read more than it needs?
4. Can this identity publish to Production?
5. Can two uploads run concurrently?

Do not enable deployment until every answer is understood.

---

## 14. Phase 8 - First Internal Deployment

### Enable the safety switch

```bash
gh variable set INTERNAL_DEPLOY_ENABLED --body "true"
```

### Controlled first run

- [ ] Merge a reviewed code change to `main`.
- [ ] Observe all CI jobs.
- [ ] Confirm deployment waits for instrumented tests.
- [ ] Confirm the generated version code is higher than every Play version.
- [ ] Confirm Workload Identity authentication succeeds.
- [ ] Confirm release signing succeeds.
- [ ] Confirm the release appears in Internal testing only.
- [ ] Confirm the GitHub summary contains no sensitive values.
- [ ] Confirm the service account still lacks Production access.

Every upload consumes its generated version code. A failed upload after Play accepts the AAB may still require the next generated code.

### Install and smoke-test

- [ ] Install from the Internal tester link.
- [ ] Confirm the displayed app version.
- [ ] Test Google sign-in and sign-out.
- [ ] Test Browse and Search.
- [ ] Test Retry and pull-to-refresh.
- [ ] Test movie details.
- [ ] Test watched and watchlist mutations.
- [ ] Test Firebase synchronization.
- [ ] Test saved results without a network connection.
- [ ] Test light and dark themes.
- [ ] Review Crashlytics before Production promotion.

---

## 15. Manual Production Promotion

Production remains outside GitHub Actions.

After the Internal artifact passes smoke testing:

1. Open Google Play Console.
2. Open Santoro.
3. Go to **Testing -> Internal testing**.
4. Select the tested release and verify its `versionCode`.
5. Choose **Promote release**.
6. Select **Production**.
7. Add or review English and Spanish release notes.
8. Review warnings and declarations.
9. Start the Production rollout at 100%.
10. Monitor publication and Crashlytics.

Do not upload a newly built AAB. Promotion must use the same Internal release.

### Production checklist

- [ ] Internal `versionCode` matches the tested installation.
- [ ] GitHub commit SHA matches the deployment summary.
- [ ] AAB checksum was recorded by CI.
- [ ] Smoke testing passed.
- [ ] Crashlytics shows no blocking regression.
- [ ] Release notes are correct in English and Spanish.
- [ ] Production rollout is 100% as intentionally selected.

---

## 16. Workflow Operations

### Normal feature release

```text
Open feature branch
-> open pull request
-> wait for CI
-> review workflow-sensitive changes carefully
-> merge to main
-> wait for automatic Internal deployment
-> smoke-test
-> manually promote in Play Console
```

### Fix after Internal testing

If Internal testing finds a defect:

```text
Fix on a short-lived branch
-> merge through CI
-> automatic new Internal artifact
-> new generated versionCode
-> retest
```

Do not try to replace the previous AAB under the same code.

### Production hotfix

Create the fix from current `main`, run the same pipeline, test through Internal, then promote the corrected release. Google Play requires a higher version code.

### Documentation-only changes

The current CI ignores `docs/**` and Markdown-only changes. They do not create unnecessary Internal releases.

---

## 17. Troubleshooting

| Problem | Likely cause | Resolution |
|---|---|---|
| Deployment job is skipped | Safety variable is false or event is not a `main` push | Check `INTERNAL_DEPLOY_ENABLED`, event, and branch |
| OIDC permission denied | Provider condition, principal binding, or `id-token` permission is wrong | Compare repository casing, branch claim, project number, pool, and service account |
| Play package not found | App permission missing or package mismatch | Confirm `com.asensiodev.santoro` and Play app access |
| Play permission denied | Service account lacks testing-track rights | Review Santoro-specific Play permissions |
| Keystore decode fails | Base64 value is malformed | Recreate the secret directly from the original keystore |
| Keystore password fails | Alias or password secret is wrong | Compare with local `~/.gradle/gradle.properties` without printing values |
| Release build is unsigned | Gradle property names or keystore path are wrong | Confirm `ORG_GRADLE_PROJECT_*` names and reconstructed file path |
| Version code already used | Base range is too low or workflow numbering reset | Increase `VERSION_CODE_BASE` above the highest Play code |
| Edit conflict | Another Play edit or upload is open | Wait, discard stale console edits, and rerun with a new generated code if needed |
| Upload reaches Production | Play permissions or target track are dangerously broad | Halt work, remove Production permission, audit workflow and Play history |
| Secrets appear in logs | A command or action printed them | Cancel the run, rotate affected credentials, and remove unsafe logging |

---

## 18. Incident Response

### Upload keystore exposed

1. Remove the exposed value from active workflows.
2. Treat deletion from Git history as insufficient by itself.
3. Request an upload-key reset through Google Play App Integrity.
4. Generate and register a replacement upload key.
5. Replace GitHub signing secrets.
6. Audit Play Console users and release history.

Google Play App Signing protects the final app-signing key. The local key used by Santoro is the upload key.

### Workload Identity access misconfigured

1. Disable the Workload Identity provider or remove the IAM binding.
2. Remove the service account from Play Console if necessary.
3. Audit GitHub workflow runs and Google Cloud audit logs.
4. Correct repository, branch, and principal restrictions.
5. Re-enable only after verification.

### Secret accidentally committed

1. Revoke or rotate the secret immediately.
2. Do not rely on deleting the file in a later commit.
3. Remove it from history where practical.
4. Audit logs and external service activity.
5. Add or strengthen ignore and scanning rules.

### Bad release promoted to Production

Google Play does not provide a general instant rollback to an arbitrary old AAB after a completed rollout. Prepare a corrected release with a higher version code, validate it through Internal testing, and promote it as quickly as safely possible.

---

## 19. Reusing This Setup

For another Android application:

1. Replace the package name.
2. Create a separate Play service account with access only to that app.
3. Restrict Workload Identity to the new repository.
4. Use that application's upload keystore and Firebase configuration.
5. Select a version-code base above its highest Play code.
6. Preserve the trusted-main-only deployment boundary.
7. Preserve manual Production promotion unless the risk model explicitly changes.
8. Test with deployment disabled before the first upload.

Do not share one broadly privileged publishing identity across unrelated public repositories when separate identities are practical.

---

## 20. Practical Tips

- Treat workflow changes like production code.
- Require CI before merging workflow changes.
- Review action release notes before updating pinned SHAs.
- Keep Production permission out of automation when it is not needed.
- Use one artifact and one version code across track promotion.
- Keep `versionName` meaningful and `versionCode` mechanical.
- Expect version-code gaps; never optimize for consecutive numbers.
- Do not test uploads by reusing a real code.
- Keep an offline encrypted backup of the upload keystore.
- Keep passwords in a password manager.
- Enable 2FA on GitHub, Google, and Play accounts.
- Monitor Crashlytics after every Production release.
- Disable Internal deployment immediately if workflow behavior is unexpected.

---

## 21. Knowledge Check

You should be able to answer these questions before considering the setup complete:

1. Why is Internal upload part of CD rather than only CI?
2. Why must Production use the same AAB tested internally?
3. Why can a consumed Play version code not be reused?
4. What is the difference between an upload key and the Play app-signing key?
5. Why is Base64 not encryption?
6. What does Workload Identity replace?
7. Why does the workflow need `id-token: write`?
8. Why must the Play service account lack Production permission?
9. Why should secret-bearing actions be pinned to commit SHAs?
10. What should happen if an Internal release fails smoke testing?

Expected summary:

```text
CI validates code.
CD builds, signs, and deploys to Internal.
Workload Identity supplies temporary Google credentials.
GitHub secrets protect the upload key.
Least privilege prevents automated Production access.
The developer promotes the tested artifact manually.
```

---

## 22. Official References

- [GitHub Actions secrets](https://docs.github.com/en/actions/security-for-github-actions/security-guides/using-secrets-in-github-actions)
- [GitHub deployment environments](https://docs.github.com/en/actions/deployment/targeting-different-environments/managing-environments-for-deployment)
- [GitHub OpenID Connect](https://docs.github.com/en/actions/security-for-github-actions/security-hardening-your-deployments/about-security-hardening-with-openid-connect)
- [Google Play Developer API setup](https://developers.google.com/android-publisher/getting_started)
- [Google Workload Identity Federation](https://cloud.google.com/iam/docs/workload-identity-federation)
- [Google GitHub authentication action](https://github.com/google-github-actions/auth)
- [Google Play upload action](https://github.com/r0adkll/upload-google-play)
- [Android app signing](https://developer.android.com/studio/publish/app-signing)
- [Google Play release tracks](https://support.google.com/googleplay/android-developer/answer/9845334)

---

## 23. Completion Record

- [ ] GitHub `internal` environment configured.
- [ ] Workload Identity provider restricted to Santoro `main`.
- [ ] Internal-only Play service account configured.
- [ ] GitHub secrets and variables configured.
- [ ] CI version-code override implemented and tested.
- [ ] Internal deployment job implemented with pinned actions.
- [ ] Dry-run security review completed.
- [ ] First automatic Internal upload completed.
- [ ] Internal smoke test completed.
- [ ] Manual Production promotion rehearsed.
- [ ] Developer can explain every item in the Knowledge Check.

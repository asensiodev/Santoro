# Guide — Prepare Santoro Releases and Deploy to Internal Testing

<!-- © 2026 Ángel Asensio (@asensiodev) · Licensed under CC BY 4.0 — see docs/LICENSE -->

| Field | Value |
|---|---|
| Audience | Maintainer configuring Android delivery for the first time |
| Updated | 2026-10-07 |
| Application | `com.asensiodev.santoro` |
| Current Production version | `1.0.34`, version code `42`, confirmed by the maintainer |
| Development workflow | Direct commits and ordinary pushes to `main`; PRs optional |
| Intended trigger | A manual GitHub Actions button or GitHub CLI command |
| Automated destination | Google Play Internal testing only |
| Production decision | Manual promotion of the tested artifact in Play Console |
| Implementation status | Preparation guide; publishing workflow and external setup remain unverified |

This is the single setup, implementation and operational guide for preparing delivery, testing a release through Play, and promoting it to Production. Follow its stages and use the preparation checklist and release-evidence table below to track progress; no separate release FIP is required.

Creating this document does not configure GitHub or Google Cloud, change application versions, upload a bundle, or publish a release. Follow the stages in order and keep publishing disabled until the rehearsal passes.

## Contents

1. [Understand the route from code to an update](#1-understand-the-route-from-code-to-an-update)
2. [Record the next release and the values you need](#2-record-the-next-release-and-the-values-you-need)
3. [Understand and protect main](#3-understand-and-protect-main)
4. [Create the internal GitHub environment](#4-create-the-internal-github-environment)
5. [Create the Google publishing identity](#5-create-the-google-publishing-identity)
6. [Give that identity access to Santoro in Play](#6-give-that-identity-access-to-santoro-in-play)
7. [Store the release configuration](#7-store-the-release-configuration)
8. [Implement the manual release workflow](#8-implement-the-manual-release-workflow)
9. [Rehearse without uploading](#9-rehearse-without-uploading)
10. [Run the first Internal deployment](#10-run-the-first-internal-deployment)
11. [Test the actual Play update](#11-test-the-actual-play-update)
12. [Promote the same artifact to Production](#12-promote-the-same-artifact-to-production)
13. [Repeat releases and recover from failures](#13-repeat-releases-and-recover-from-failures)

## 1. Understand the route from code to an update

A source commit, an Android bundle, and a published release are different things. A commit identifies the code. An Android App Bundle (AAB) is the signed build uploaded to Play. Play uses it to deliver APKs suitable for each device. A release assigns an uploaded version to an audience.

The intended Santoro flow is:

```text
Work on main → Commit → Ordinary push → CI runs
                                                 |
                                  You choose Run workflow
                                                 |
                         Validate the exact selected commit
                                                 |
                          Build and sign one release AAB
                                                 |
                           Upload to Internal testing
                                                 |
                         Install from Play and smoke-test
                                                 |
                         Manually promote to Production
```

**Continuous Integration (CI)** checks proposed changes. It continues to run on relevant pushes to `main` and any optional pull requests. **Continuous Delivery** automates preparing and delivering a release candidate. A manual trigger can start a fully automated delivery process; you do not have to upload the AAB yourself. Production remains a separate decision after testing.

For Santoro, start with manual delivery. This lets you group several changes into a release instead of distributing every source change. Automatic Internal uploads can be added later if testers need every change. Neither approach inherently makes the build safer: trustworthy source, tests, signing, credentials and permissions provide the controls.

Build once and promote unchanged. Testing one build and rebuilding for Production would weaken the connection between what you tested and what you shipped. Play can reuse an uploaded bundle in another release; track promotion does not require a fresh upload. See [Play release preparation](https://support.google.com/googleplay/android-developer/answer/9859348?hl=en).

## 2. Record the next release and the values you need

### What version numbers mean

`versionName` is the label people see, such as `1.0.34`. `versionCode` is the integer Android and Play use to distinguish upgrades. Increasing only the displayed name does not produce a valid update if the code is already used. See [Android versioning](https://developer.android.com/studio/publish/versioning).

The maintainer confirmed `1.0.34 (42)` is already in Production. Do not upload another artifact using code `42`.

1. Open [Play Console](https://play.google.com/console), select Santoro and open **Test and release → App bundle explorer**. Navigation wording can vary; find the list of previously uploaded app bundles.
2. Record the highest used version code, including test uploads. Production code `42` does not establish that `42` is the highest code across all uploads.
3. Choose a new code above that maximum. `43` is valid only if the maximum is still `42`.
4. Choose the user-facing version. `1.1.0` is a reasonable proposal for a release adding person profiles and filmography; `1.0.35` is an alternative if you deliberately keep the patch sequence. Confirm the label before implementation.
5. During release preparation, update `versionName` and `versionCode` in `gradle/libs.versions.toml` in a normal source commit on `main` (or an optional PR). This guide does not edit them.

For the first manual pipeline, keep version codes explicit in that catalog. One new candidate gets one new code. We do not need a run-number formula or a Gradle override to begin safely. The workflow must read the catalog and report the resulting bundle metadata; Play rejection of a reused code is a failure, never a reason to silently change numbers and retry.

### Your setup worksheet

Keep public identifiers in your notes. Keep passwords and private key material in a password manager or GitHub Secrets, never in this worksheet or chat.

| Value | Where you obtain it | Where it will be used |
|---|---|---|
| Highest Play version code | App bundle explorer | Select the next unused code |
| Next version name and code | Your release decision | Version catalog and release notes |
| Google Cloud project ID and number | Cloud project settings | Workload Identity resources |
| GitHub repository ID and owner ID | Read-only GitHub API command in stage 5 | Identity trust restrictions |
| Publishing service-account email | Cloud Service Accounts | Play invitation and GitHub variable |
| Workload Identity provider resource name | Cloud Federation provider | GitHub variable |
| Existing upload keystore, alias and passwords | Existing release setup or password manager | GitHub environment secrets |
| Release Firebase configuration | Existing Production Firebase Android app | GitHub environment secret |

You need owner/admin access to the GitHub settings, permission to configure the chosen Cloud project, and permission to manage Santoro users in Play Console. Do not create another Play application or Firebase release app: this is an update of the existing application.

## 3. Understand and protect main

### Main and direct pushes

`main` is the repository branch from which we prepare releases. A branch is a named line of source history; it is not a running application. An uncommitted local change is not automatically present on GitHub.

The maintainer prefers working directly on `main`. This guide supports ordinary commits and pushes without requiring PRs. A pull request is an optional way to review and merge another branch, useful for larger changes or future collaboration; it is not required to publish safely through this manual pipeline.

There are two different controls:

| Control | What it protects | Santoro's choice |
|---|---|---|
| Branch rules | Changes to Git history and optional pre-merge requirements | Block force pushes and deletion; allow ordinary pushes |
| Release checks | Whether a particular commit can be built and uploaded | Require all checks to pass before an Internal upload |

A normal push appends commits. A force push can replace shared history and make an already tested commit harder to trace. Blocking deletion/force pushes protects the history while letting you continue your normal workflow.

Direct push means a broken commit can reach `main` before CI reports a failure. We accept that development trade-off. It must not reach Internal: the manual release workflow independently validates the selected SHA and refuses to upload if checks fail. Branch protection is useful here, but it is not the release gate.

### Configure only the rules you actually want

1. Open [Santoro repository settings](https://github.com/asensiodev/Santoro/settings).
2. Go to **Rules → Rulesets → New ruleset → New branch ruleset**. If unavailable, use **Settings → Branches → Add branch protection rule** instead; avoid accidentally creating overlapping policies.
3. Name the rule `protect-main-history` and target exactly `main`.
4. Block deletion and force pushes. In a ruleset these are **Restrict deletions** and **Block force pushes**; in classic branch protection, leave force pushes/deletions disallowed.
5. Do not require a pull request, approving reviews, restricted updates or successful status checks for ordinary pushes. Those extra rules can prevent the direct workflow you requested.
6. Review bypass permissions. If you want the history rules to apply to your administrator account, avoid giving yourself a routine bypass (or apply classic protection to administrators). Keep the rule narrow so normal pushes remain possible.
7. Activate/save the rule. Read its final summary and confirm that only history rewrites/deletion are restricted.

**Expected result:** ordinary `git push` to `main` is allowed, and CI runs after relevant pushes. Forced rewrites and deletion are disallowed when the rule applies to the actor. Inspect the rule; do not test it by actually deleting or rewriting `main`.

Do not enable required PRs/checks merely because another tutorial recommends them. If you later choose that workflow, first resolve Santoro's documentation-only path filters: a skipped required workflow can leave a documentation PR waiting indefinitely for its check. This is a future setup consideration, not a blocker for your current direct pushes.

GitHub documents [branch protection](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches), [ruleset creation](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/creating-rulesets-for-a-repository), and [workflow path filters](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/trigger-a-workflow).

## 4. Create the internal GitHub environment

### What an environment means here

A GitHub environment is a named deployment configuration containing secrets, variables and deployment rules. It is not an Android emulator, another Git branch, a Firebase project or the Google Play test track.

We use the same label, `internal`, for two separate things:

| System | Meaning of internal |
|---|---|
| GitHub | Rules and credentials for a deployment job |
| Google Play | The audience that receives a test release |

The workflow connects them: its job selects the GitHub environment and its publisher targets the Play track. Creating one does not create the other.

### Create it

1. In [Santoro settings](https://github.com/asensiodev/Santoro/settings), open **Environments**.
2. Choose **New environment**, enter `internal`, and configure it.
3. Under **Deployment branches and tags**, select **Selected branches and tags**.
4. Add a **Branch** rule for exactly `main`. Do not add all branches or a wildcard tag rule.
5. Initially leave the secrets empty; we add them after configuring identity and signing.
6. Required reviewers are optional for this manual Internal flow. A second approval is useful when another maintainer reviews releases. Do not enable **Prevent self-review** with yourself as the only possible reviewer: that prevents you from approving your own run.

Only a job referencing this environment receives its environment secrets, after its rules are satisfied. This limits where signing credentials become available. A user with enough permission to change workflows or environment settings is still trusted; an environment is not protection against all repository administrators.

**Expected result:** the environment exists, allows `main`, and excludes feature branches and tags. No external service has been deployed yet. See [GitHub environment setup](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments).

### Add the initial publishing switch

Open **Settings → Secrets and variables → Actions → Variables** and create a repository variable:

```text
INTERNAL_DEPLOY_ENABLED=false
```

A variable is ordinary readable configuration, not encrypted secret storage. This switch lets us install and rehearse the publishing workflow while actual Play uploads remain disabled. It is a useful operational control; it does not replace identity restrictions.

## 5. Create the Google publishing identity

### Why GitHub needs an identity

Your browser session can access Play as you. A GitHub runner is a separate computer and needs its own identity. Give it a dedicated service account instead of your personal Google credentials.

We use Workload Identity Federation (WIF): GitHub supplies a signed OpenID Connect (OIDC) identity token; Google checks its claims and permits it to act as the publishing service account. Credentials are temporary. No permanent service-account private-key JSON needs to be stored in GitHub.

Google recommends numeric repository and owner IDs for trust conditions because names can be renamed or reused. See [Google deployment-pipeline federation](https://docs.cloud.google.com/iam/docs/workload-identity-federation-with-deployment-pipelines).

### Configure the Cloud side

1. Open [Google Cloud Console](https://console.cloud.google.com/) and select the controlled project you will use for release automation. Record its **project ID** and **project number**; they are different values.
2. In **APIs & Services → Library**, enable the Google Play Android Developer API. Ensure IAM, Resource Manager, Service Account Credentials and Security Token Service APIs required by the federation setup are enabled. Follow the linked Google guide for project/billing prerequisites and the permissions your administrator needs.
3. Open **IAM & Admin → Service Accounts → Create service account**. Name it `santoro-play-internal` and record its email.
4. Do not assign project-wide Owner or Editor to this publisher and do not create a private JSON key. Cloud project permissions and Play release permissions are configured separately.
5. Open **IAM & Admin → Workload Identity Federation**. Create a pool, for example `github-actions`, and an OIDC provider, for example `santoro-github`.
6. Use the issuer `https://token.actions.githubusercontent.com`.
7. Obtain GitHub IDs with this read-only command if the GitHub CLI is installed and authenticated:

   ```bash
   gh api repos/asensiodev/Santoro --jq '{repository_id: .id, owner_id: .owner.id}'
   ```

8. Add attribute mappings:

   ```text
   google.subject                = assertion.sub
   attribute.repository_id       = assertion.repository_id
   attribute.repository_owner_id = assertion.repository_owner_id
   attribute.ref                 = assertion.ref
   ```

9. Configure the provider condition below, substituting the numeric IDs. The workflow filename is the proposed filename from stage 8; change the condition with it if implementation uses another name.

   ```text
   assertion.repository_id == 'REPOSITORY_ID' && assertion.repository_owner_id == 'OWNER_ID' && assertion.ref == 'refs/heads/main' && assertion.event_name == 'workflow_dispatch' && assertion.sub == 'repo:asensiodev/Santoro:environment:internal' && assertion.workflow_ref == 'asensiodev/Santoro/.github/workflows/deploy-internal.yml@refs/heads/main'
   ```

10. Permit this repository principal to impersonate the publishing service account by granting `roles/iam.workloadIdentityUser` on that service account:

    ```text
    principalSet://iam.googleapis.com/projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL_ID/attribute.repository_id/REPOSITORY_ID
    ```

    Use the chosen project number and pool ID. In the service account's permissions/principal-access view, grant that principal the **Workload Identity User** role. If the UI does not offer the principal type, use Cloud Shell with your authorized administrator account and substitute the worksheet values:

    ```bash
    gcloud iam service-accounts add-iam-policy-binding PUBLISHER_EMAIL \
      --project=PROJECT_ID \
      --role=roles/iam.workloadIdentityUser \
      --member="principalSet://iam.googleapis.com/projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL_ID/attribute.repository_id/REPOSITORY_ID"
    ```

    This command changes IAM access; check every identifier before running it. It grants impersonation for this service account, not project administration. The role goes on the service account, and the principal identifies the repository; do not grant it indiscriminately to the entire pool.

11. Copy the provider's full resource name, such as `projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL_ID/providers/PROVIDER_ID`.

These conditions are a concrete proposal, not proof that federation works. In particular, environment jobs have an environment-based `sub` claim rather than the ordinary branch-based subject. Validate the configured claims in the dry run, and do not fix an error by removing repository/branch restrictions.

**Expected result:** only the intended manual Santoro workflow on `main`, using `internal`, can impersonate the publisher. Cloud authentication alone still cannot release Santoro: the next stage grants app-specific Play permissions.

## 6. Give that identity access to Santoro in Play

1. Open [Play Console](https://play.google.com/console) with an account allowed to manage users.
2. Go to **Users and permissions → Invite new users**.
3. Enter the publishing service-account email from stage 5.
4. Give access specifically to Santoro, not every app in the developer account.
5. Grant the app-information access needed by the publisher and permission to release apps to testing tracks. Read the current permission descriptions: Google may adjust labels.
6. Do not grant Production release, administrator, financial, order or subscription-management permissions.
7. Save/invite the account and verify its resulting app permissions.

The account is a machine identity, so it does not sign into Gmail to accept an invitation. Play authorizes its service-account email. Google no longer requires linking a developer account to a Cloud project for this API setup. See [Play API setup](https://developers.google.com/android-publisher/getting_started).

Permission for testing tracks may cover Internal, Closed and Open testing, rather than only Internal. We additionally fix the workflow destination to `internal`; do not describe the account as technically Internal-only unless Play actually provides that granularity. The key boundary is that it lacks Production permission.

**Expected result:** Santoro's testing release permission is present, Production permission is absent, and other applications are inaccessible. Inspect permissions rather than attempting a real Production upload to prove a denial.

## 7. Store the release configuration

### Signing: two different keys

The upload key signs the bundle that you send to Play. With Play App Signing, Google manages the app-signing key used for delivery to users. Reuse Santoro's registered upload key. Generating an unrelated new keystore would not automatically make it valid for the existing app.

Locate the existing keystore and its alias/passwords from your release setup. Do not inspect or paste your entire Gradle properties file into chat. Keep an encrypted offline backup. If the key is unavailable, use Play's upload-key recovery procedure before attempting this release.

### Add the values to GitHub

In **Settings → Environments → internal**, create these **environment secrets**:

| Secret | What it contains | Why the workflow needs it |
|---|---|---|
| `ANDROID_UPLOAD_KEYSTORE_BASE64` | Existing upload keystore encoded as text | Reconstruct the signing file on the runner |
| `ANDROID_UPLOAD_KEY_ALIAS` | Alias of the registered upload key | Select the right entry in the keystore |
| `ANDROID_UPLOAD_STORE_PASSWORD` | Keystore password | Open the keystore |
| `ANDROID_UPLOAD_KEY_PASSWORD` | Private-key password | Sign the bundle |
| `GOOGLE_SERVICES_RELEASE_JSON` | Existing release `google-services.json` contents | Build with the correct Firebase Android app |

Base64 is an encoding, not encryption. On the maintainer's Mac, replace the example path below with the actual upload-keystore path and copy its encoded contents directly to the clipboard:

```bash
base64 -i /absolute/path/to/santoro-upload.jks | pbcopy
```

Paste into the `ANDROID_UPLOAD_KEYSTORE_BASE64` secret value and save. This reads the existing file; it does not generate a key. Do not paste the clipboard into chat, screenshots or source files. After saving, clear it:

```bash
printf '' | pbcopy
```

Add the alias and passwords from your existing release setup with **Add secret**, using the exact names in the table. For Firebase, obtain the existing Production Android app's config from **Firebase Console → Project settings → General → Your apps**; verify its package is `com.asensiodev.santoro`, open the downloaded JSON locally, and paste its contents into `GOOGLE_SERVICES_RELEASE_JSON`. Do not substitute the `.debug` app configuration.

Do not print encoded credentials in shared logs or commit them. GitHub stores the secret encrypted. Alias names are not themselves powerful credentials, but keeping signing values together avoids unnecessary exposure and mismatched configuration.

`google-services.json` is Android client configuration, not a Firebase administrator key. Firebase data access still depends on authentication and security rules. We keep this file outside Git and require the real release configuration when building a publishable bundle.

Create these **environment variables** in the same environment:

| Variable | Value |
|---|---|
| `PLAY_PACKAGE_NAME` | `com.asensiodev.santoro` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Provider resource name from stage 5 |
| `GCP_PLAY_SERVICE_ACCOUNT` | Publishing service-account email |

The Play track is fixed to `internal` in the proposed workflow; it is not a user-selectable Production input. No version-code base is needed for the catalog-based first implementation. No `GOOGLE_APPLICATION_CREDENTIALS` private-key secret is required: federation supplies temporary credentials.

**Expected result:** the secrets and public identifiers exist in the intended environment. Their names are visible; their secret values are not printed or stored in the repo. See [GitHub secret handling](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).

## 8. Implement the manual release workflow

### Current behavior versus the workflow we still need

Today `.github/workflows/ci.yml` runs verification jobs and offers a manual CI trigger. Pressing that existing button does not upload to Play. `app/build.gradle.kts` already reads release signing properties, and the version catalog currently contains the published version.

The proposed `.github/workflows/deploy-internal.yml` does not exist yet. Repository implementation must be reviewed and tested before this guide's publishing commands work. This is the stage where an agent/developer edits workflow files, adds the release verification path while retaining ordinary direct pushes to `main`. PR-only required checks are not part of this setup.

### What the workflow must do

1. Start only from `workflow_dispatch` on `refs/heads/main`; accept an explicit mode, `dry-run` or `publish`, defaulting to `dry-run`.
2. Capture the triggering commit SHA and check out that exact commit for every job. Do not switch to the moving tip of `main` halfway through a run.
3. Run the existing static, unit/coverage, screenshot and instrumented checks for that same SHA. Implement reusable verification jobs where appropriate; do not treat a green run for another commit as release evidence.
4. Keep verification jobs away from upload secrets. After verification succeeds, a release job references `environment: internal` and reconstructs signing files under `$RUNNER_TEMP`.
5. Read and validate `versionName` and `versionCode` from the catalog. Produce a signed `:app:bundleRelease` and run release lint and the repository release gates.
6. Require real signing and release Firebase configuration. The CI currently uses Firebase placeholders for some verification jobs when secrets are absent; a publishable release must fail instead of falling back to placeholders or an unsigned AAB.
7. Verify bundle package/version metadata, the signature and the expected upload certificate fingerprint, and calculate SHA-256. A valid signature from the wrong key is not sufficient. Android upload certificates can be self-signed, so a strict signature check must handle expected trust warnings deliberately rather than equating every warning with a corrupt bundle.
8. Authenticate using the pinned Google authentication action and a Play-compatible publisher. For an OAuth access-token publisher, request the `https://www.googleapis.com/auth/androidpublisher` scope explicitly; the authentication action's default Cloud scope is not a substitute.
9. Request `contents: read` and `id-token: write` only on the job that needs Google identity. An OIDC token identifies the job; its write permission does not grant permission to edit repository files.
10. Upload only if mode is `publish`, `INTERNAL_DEPLOY_ENABLED` is `true`, and all checks passed. Target only `internal`.
11. Serialize the complete release operation with a dedicated concurrency group and `cancel-in-progress: false`. Do not let CI's current cancellation policy cancel a Play transaction during a new push or another button click.
12. Summarize commit, version, track, checksum and outcome, and clean temporary signing files and credentials even on failure. Do not expose tokens, keystore data or R8 mappings as public artifacts.

Federated credential files and service-account private-key JSON files are different formats. Select and test a publisher that supports the chosen WIF/ADC or scoped access-token method. An action input called `serviceAccountJson` is not proof that it accepts a WIF credential file. Do not introduce a permanent JSON key to work around an unverified uploader.

The Google authentication action may create its temporary credential file in the checked-out workspace; exclude it explicitly from artifacts and rely on its documented post-job cleanup as well as runner cleanup. Do not assume every temporary credential resides in `$RUNNER_TEMP`. See [Google's authentication action](https://github.com/google-github-actions/auth).

Every third-party action in the signing/publishing job must be reviewed and pinned to a full immutable commit SHA. Pinning fixes which code executes; updating those pins requires another review. See [GitHub Actions security](https://docs.github.com/en/actions/reference/security/secure-use).

### What the button will look like

After implementation is committed and pushed onto `main`:

1. Open [Santoro Actions](https://github.com/asensiodev/Santoro/actions).
2. Select **Deploy Internal**.
3. Choose **Run workflow**, select `main` and choose the mode.
4. Submit the run and inspect its summary.

The equivalent proposed commands are:

```bash
gh workflow run deploy-internal.yml --repo asensiodev/Santoro --ref main -f mode=dry-run
gh workflow run deploy-internal.yml --repo asensiodev/Santoro --ref main -f mode=publish
```

They are the same remote pipeline entry point, not a separate local build-and-upload path. The filename and `mode` input are the implementation contract; these commands are not available until the workflow exists. See [running workflows manually](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/manually-run-a-workflow).

## 9. Rehearse without uploading

Keep `INTERNAL_DEPLOY_ENABLED=false` and use mode `dry-run`. A rehearsal should exercise the real signing/build/metadata checks and restricted Google authentication, while never creating an upload or changing a track. Granting Google access is separate from committing a Play release.

The implementation should run the repository gates through the managed Gradle wrapper required by [the testing guide](GUIDE-testing-and-tdd.md): affected tests first, then `test detekt ktlintCheck koverVerify assembleDebug assembleRelease`, plus applicable screenshot/instrumented execution and `:app:bundleRelease :app:lintVitalRelease`.

Record:

- Exact commit SHA and expected version name/code.
- Passed checks for that SHA, including release shrinking and signing.
- Bundle package name, verified upload certificate and checksum.
- Successful Google authentication and confirmed app-specific testing permissions.
- Confirmation that dry-run made no Play upload and exposed no secret values.
- Non-main/pull-request exclusion and concurrency/cancellation behavior, verified without attempting Production publication.

Do not enable publishing merely because the debug build passed. Debug and release differ in signing, configuration and shrinking.

Install the signed, minified Release APK on a test emulator and run `python3 tools/check-release-startup.py --serial <device-serial>` from the repository root before distributing the bundle. Use `--adb <path>` when needed. The check performs a cold launch and rejects startup crashes; the separate Play upgrade and fresh-install checks remain required.

**Expected result:** a rehearsal summary you can compare with the setup worksheet, with publishing still disabled. Keep failed checks visibly open in this guide's preparation checklist and release-evidence table.

## 10. Run the first Internal deployment

1. Confirm the next version code is unused and the intended release changes are committed on GitHub `main`.
2. Change the repository variable `INTERNAL_DEPLOY_ENABLED` to `true` only after the rehearsal and permission review pass.
3. Start **Deploy Internal** on `main` with mode `publish`.
4. Let its checks finish. Avoid editing the same release simultaneously in Play Console.
5. Inspect the sanitized summary. Record the SHA, version name/code and bundle checksum.
6. Open **Play Console → Santoro → Test and release → Testing → Internal testing** and confirm the expected version is available to Internal testers. Investigate unexpected numbers instead of accepting the release as correct.

A successful workflow means the automated steps passed; it does not mean a person has tested the app. Availability is not guaranteed to be instantaneous. Play may show processing or review/declaration requirements; follow the displayed status. See [Play testing setup](https://support.google.com/googleplay/android-developer/answer/9845334?hl=en).

### Add yourself as a tester

On the Internal track, open **Testers**, select/create an email list or supported group, include the Google account used on your test device, and save it. Copy the opt-in link and open it while signed into that account. Accept the invitation, then use the Play Store installation/update link. Share the opt-in link yourself with other approved testers; do not assume creating a list sends an invitation automatically.

**Expected result:** Play shows the intended Internal version and your account can install it. Record the installed version, not just the version shown in the upload summary.

## 11. Test the actual Play update

The app is already in Production, so protect the upgrade experience as well as fresh installs. Use dedicated test accounts and preferably a physical device; emulator journey tests are useful but do not replace this Play-delivered smoke test.

### Upgrade from the current Production version

1. Before opting into Internal, install `1.0.34 (42)` from Production on a test device/account where that version is available.
2. Sign in and create known Watched/Watchlist entries. Record what you expect to remain.
3. Opt that same Play account into Internal and update through Play **without uninstalling or clearing application data**.
4. Verify the new installed version and check that the expected session, preferences and movie lists remain.
5. Exercise synchronization using the same Firebase account. Do not mistake testing a different account for the supported same-account upgrade path.

If you are already enrolled in Internal or have a higher version installed, use a separate test device/account with the Production baseline. Do not clear data and label that a successful upgrade test.

### Fresh install and feature checks

- [ ] Install cleanly with a dedicated test setup, then try Google sign-in and guest entry.
- [ ] Search, browse, open movie details, mark Watched and add/remove Watchlist entries.
- [ ] Confirm statistics and synchronized lists agree with the known test data.
- [ ] Open an actor/director, read the biography, browse the filmography and open a movie.
- [ ] Verify year headings in the full filmography, preview card years and Back/scroll restoration.
- [ ] Switch English/Spanish and light/dark themes.
- [ ] Rotate, background/foreground, and test offline/error/retry behavior.
- [ ] Sign out and back in; confirm the expected account's data appears.
- [ ] Exercise account deletion only with a disposable account, never real personal data.
- [ ] Review TalkBack readability, touch targets, startup and scrolling on the device.
- [ ] Review Crashlytics for new fatal errors and relevant non-fatal failures.

Record pass/fail observations beside the release SHA/version. Check the separate FIP-023 and person-exploration manual gates; uploading to Internal does not complete them automatically. Offline first access to uncached person details may fail with retry; verify the accepted behavior rather than inventing an offline guarantee.

**Expected result:** an explicit smoke-test record for the exact Play-delivered candidate. Fix defects and publish a higher-code candidate before proceeding if necessary.

## 12. Promote the same artifact to Production

1. Confirm the Internal version, release SHA and smoke-test record match.
2. In Play Console, open the tested release and use the available promotion flow, or create a Production release and select the already uploaded bundle from the library. Do not rebuild or upload a different binary with the same label.
3. Add English and Spanish release notes describing the actual user-facing changes.
4. Review Play's warnings, declarations and review status before submitting.
5. For this update, choose a staged rollout appropriate to the audience where available; for example, begin with a small percentage and increase after reviewing crashes and product behavior. Low traffic can provide little statistical evidence, so combine telemetry with your smoke tests.
6. Complete the Production action explicitly in Play Console. GitHub's publishing account remains unable to perform it.
7. Monitor the rollout and record the final outcome.

Internal testing is recommended preparation for this update, not a new first-publication closed-testing requirement. Play's displayed policy or release blockers still apply. Staged rollout can limit exposure and can be halted; users who already received an update are not automatically downgraded. See [staged rollouts](https://support.google.com/googleplay/android-developer/answer/6346149?hl=en).

**Expected result:** Production references the same tested version code. Any later fix uses a new higher code.

## 13. Repeat releases and recover from failures

For subsequent releases: prepare a new version in a source commit, push to `main`, inspect CI, run the manual Internal workflow, test the Play candidate, then promote. Keep the same credentials and rules unless a rotation or configuration change is required.

| Situation | Meaning and next action |
|---|---|
| There is no Deploy Internal button | The new workflow is not implemented/on the default branch, or lacks `workflow_dispatch`; the existing CI button is not a publisher. |
| A PR waits forever for CI | Check path filters and required check names; fix the required-gate setup rather than bypassing it. |
| Environment rejects a branch | Use the intended `main` run; do not broaden the environment to all branches. |
| A solo run waits for an impossible approval | Review required reviewer/self-review settings and choose a feasible approval policy. |
| WIF authentication fails | Compare numeric IDs, provider path, subject, workflow filename/ref, event, impersonation binding and propagation delay. Preserve the trust restrictions. |
| Play denies the upload | Check app-specific test permissions, package name and the OAuth scope. Do not grant Production or project Owner as a shortcut. |
| Version code was already used | Check all Play uploads, prepare a higher code and a new commit; never reuse the Production code. |
| A publish run fails after upload | First inspect Play to learn whether the code was consumed or the track changed. Do not blindly rerun the same artifact; document and reconcile the partial outcome. |
| Release Firebase config or signing secret is missing | Fail the release. CI placeholders or an unsigned build are not acceptable fallbacks. |
| Signing key is rejected | Compare the registered upload certificate with the existing key; use Play's key-reset procedure if needed. |
| A Play edit conflicts with another | Avoid simultaneous edits; inspect the track and retry only after resolving the conflict and code status. |
| Internal testers do not see an update | Check opt-in account, tester list, processing status and installed version code; allow propagation without promising a fixed time. |
| Internal testing finds a defect | Fix it in a source commit, choose a new higher code, build a new candidate and retest. |
| Production finds a serious defect | Halt a staged rollout where possible, prepare and test a higher-code fix; do not assume an old bundle can instantly downgrade users. |
| A credential is exposed | Disable publishing, revoke/rotate the affected credential, audit access and logs, and resume only after correcting the cause. Deleting a log alone is not revocation. |

The `INTERNAL_DEPLOY_ENABLED` switch blocks uploads, not credential exposure by itself. If identity is compromised, also disable the provider/remove the service-account access. For an exposed upload key, follow Play's upload-key reset procedure. Preserve the Play-managed app-signing key.

## Preparation and release record

Use this checklist as the release preparation record. Record non-secret evidence in the table below or copy the worksheet to private release notes. Blank boxes are pending, not proof that setup occurred.

- [ ] Highest Play code recorded; next name/code approved.
- [ ] `main` rules block force pushes/deletion while allowing ordinary direct pushes.
- [ ] `internal` environment restricted to `main`.
- [ ] Publishing switch starts disabled.
- [ ] WIF trust restricted to the intended repository, owner, branch, environment and workflow.
- [ ] Play account limited to Santoro/testing, without Production permission.
- [ ] Existing upload key and real release Firebase configuration stored securely.
- [ ] Manual workflow implemented, reviewed and pushed to `main`; actions pinned.
- [ ] Same-commit verification and signed-bundle dry run passed.
- [ ] First Internal upload confirmed in Play.
- [ ] Upgrade and fresh-install smoke tests recorded; Crashlytics checked.
- [ ] Same-artifact Production promotion reviewed and performed manually.

| Release evidence | Value |
|---|---|
| Source SHA | Pending |
| Version name/code | Pending |
| AAB SHA-256 | Pending |
| Upload certificate fingerprint matched | Pending |
| CI/release verification run | Pending |
| Internal upload result | Pending |
| Upgrade/fresh-install checks | Pending |
| Production rollout decision and result | Pending |

## Historical context

The former Internal testing guide recorded a completed test on 2026-04-18 for `1.0.14 (19)`. That historical result does not validate a current candidate. The maintainer confirmed `1.0.34 (42)` is in Production on 2026-10-07; check all Play uploads before selecting a new code.

The former Closed testing guide described initial Production-access preparation for `1.0.15 (20)`. Santoro is already in Production, so that onboarding guide has been removed. Its historical setup notes and store-listing drafts remain recoverable in Git.

## Official references

- [GitHub branch protection](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [GitHub ruleset creation](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/creating-rulesets-for-a-repository)
- [GitHub deployment environments](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments)
- [GitHub workflow triggers and path filters](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/trigger-a-workflow)
- [GitHub manual workflow execution](https://docs.github.com/en/actions/how-tos/manage-workflow-runs/manually-run-a-workflow)
- [GitHub secrets](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets)
- [GitHub secure use of Actions](https://docs.github.com/en/actions/reference/security/secure-use)
- [Google Workload Identity Federation](https://docs.cloud.google.com/iam/docs/workload-identity-federation-with-deployment-pipelines)
- [Google GitHub authentication action](https://github.com/google-github-actions/auth)
- [Google Play Developer API setup](https://developers.google.com/android-publisher/getting_started)
- [Android versioning](https://developer.android.com/studio/publish/versioning)
- [Android app signing](https://developer.android.com/studio/publish/app-signing)
- [Play test setup](https://support.google.com/googleplay/android-developer/answer/9845334?hl=en)
- [Play release preparation](https://support.google.com/googleplay/android-developer/answer/9859348?hl=en)
- [Play staged rollouts](https://support.google.com/googleplay/android-developer/answer/6346149?hl=en)

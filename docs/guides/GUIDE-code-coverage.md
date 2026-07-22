# Code Coverage

This guide explains Santoro's aggregate JVM coverage reporting with Kover and its CI integration with GitHub Actions and Codecov.

## Coverage Contract

Kover measures JVM test execution across the production modules aggregated in the root `build.gradle.kts`.

The measured baseline established on 2026-07-22 is:

| Metric | Covered | Total | Baseline | CI floor |
|---|---:|---:|---:|---:|
| Lines | 2,484 | 3,276 | 75.8242% | 75% |
| Branches | 722 | 1,014 | 71.2032% | 71% |

The floors are intentionally rounded down. They prevent meaningful aggregate regression without claiming that every class already meets the stricter ViewModel, use-case, repository, and mapper targets.

## Included Code

The aggregate report includes executable production logic from:

- The application module.
- Core auth, build-config, data, database, design-system, domain, network, sync, and UI modules.
- Every feature API and implementation module.
- Observability, remote-config, and secure-storage API and implementation modules.

`architecture-tests`, `core:testing`, and the resource-only `core:string-resources` module are not coverage subjects.

## Exclusions

The report excludes code that does not provide a meaningful unit-coverage signal:

- Android-generated `R`, `Manifest`, and `BuildConfig` classes.
- Dagger and Hilt generated components, factories, and members injectors.
- Room-generated database and DAO implementations.
- Hand-written DI wiring under `.di.` packages.
- Compose-generated singleton classes.
- Declarations annotated with `@Composable`, including previews and rendering functions.
- Android `Application` and `ComponentActivity` entry points.

Compose rendering is validated with Paparazzi and Android tests instead of being used to inflate or depress the JVM line metric.

## Local Commands

Generate the aggregate XML and HTML reports:

```sh
./gradlew :koverXmlReport :koverHtmlReport
```

Print the aggregate line percentage:

```sh
./gradlew :koverLog
```

Enforce the measured line and branch floors:

```sh
./gradlew :koverVerify
```

Run the same coverage workflow as CI:

```sh
./gradlew :koverXmlReport :koverHtmlReport :koverVerify :architecture-tests:test
```

Reports are written to:

- `build/reports/kover/report.xml`
- `build/reports/kover/html/index.html`

Use root-qualified task names beginning with `:`. Unqualified Kover task names can execute the corresponding task independently in every subproject.

## CI Reporting

The `Unit tests and coverage` CI matrix entry:

1. Executes all JVM tests needed by the aggregate Kover report.
2. Enforces the line and branch floors.
3. Adds the aggregate line percentage to the GitHub Actions job summary.
4. Uploads the HTML and XML reports as the `coverage-reports` artifact for 14 days.
5. Uploads the XML report to Codecov when the repository is configured there.

The GitHub artifact remains available independently of Codecov.

Kover does not instrument tests running on an Android device or emulator. Instrumented-test results remain a separate CI signal and are not included in the aggregate percentage.

## Codecov Setup

Codecov requires one-time repository-owner configuration:

1. Sign in at [Codecov](https://app.codecov.io/) with GitHub.
2. Install the [Codecov GitHub App](https://github.com/apps/codecov) for `asensiodev/Santoro` only.
3. Enable the Santoro repository in Codecov.
4. Copy the repository upload token from Codecov's repository settings.
5. Open GitHub repository settings.
6. Navigate to `Secrets and variables` > `Actions`.
7. Add a repository secret named `CODECOV_TOKEN` containing only the token value.
8. Run the CI workflow manually or open a pull request.
9. Confirm Codecov receives `build/reports/kover/report.xml` with the `unit` flag.

Never commit the Codecov token or print it in workflow output.

The Codecov statuses in `codecov.yml` start as informational. They report project and changed-line coverage without blocking pull requests while the first history is established. The local Kover floors remain the authoritative blocking coverage gate.

After the first successful default-branch upload, add the Codecov badge to the root README using the badge URL provided by the Codecov repository settings.

## Raising Coverage Floors

Raise a floor only after:

1. Generating a fresh aggregate report from a clean, successful test run.
2. Confirming the increase comes from meaningful behavior tests rather than broader exclusions.
3. Updating the measured baseline in this guide.
4. Keeping the floor at or below the reproducible measured percentage.

Do not lower a floor solely to make CI pass. Investigate the regression, add meaningful tests, or document an intentional production-code expansion before adjusting policy.

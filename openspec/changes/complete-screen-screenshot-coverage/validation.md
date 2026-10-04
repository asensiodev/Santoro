# Screen screenshot validation

## Parent review

This is regression coverage of existing behavior, not retrospective TDD. No production code changes were made. Initial setup/style failures and fixture corrections are recorded separately from behavior failures.

All 76 new captures were reviewed visually after the Surface fixture correction, in ten contact sheets generated from the final PNGs. Every light/dark variant was inspected. All eight legacy expected/actual delta images were reviewed before approving their targeted toolchain refresh. No tolerance changed. Only those eight tracked component goldens remain changed; the accidentally recorded unchanged HeroMovieCard golden was restored from the exact HEAD blob.

Independent root `verifyPaparazziDebug` passed 112 active screenshot cases across eight modules: 88 screen cases (76 new and 12 existing person cases) and 24 component cases. Orphan PNGs without executing tests are not included in this count.

YAML parsing and matrix assertions passed: root screenshot verification command, distinct unit/screenshot artifact names. `openspec validate complete-screen-screenshot-coverage --strict` and `git diff --check` passed. Hosted Ubuntu execution remains external and unverified locally. The final scope audit confirms exactly eight approved tracked golden changes and exactly 76 new screen PNGs; HeroMovieCard remains identical to HEAD.

Graph Tier 2 discovery covered ten plain screens and existing screenshot/state classes. Exact coverage checks recorded no source parse gaps in relied-on screen/state/test/model paths; changed person fixture source was read directly while watch metadata caught up. PNGs and build outputs are intentionally outside the graph and were inspected directly.

## Managed Gradle evidence

# Screen screenshot validation

Workflow: `aaf28c3e896c59524e8151126b79e04e`. Commands use the managed `gradle_run.py` wrapper, JDK 21, in-process Kotlin compiler, and Firebase disabled.

## Existing baseline audit

Question: **Do all existing screenshot modules verify before adding missing screen coverage?**

Nested command: `./gradlew verifyPaparazziDebug --continue -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false`. Wrapper run 0001, exit 1.

Answer: No. Nine existing snapshots differ: Banner 0.367078%, BottomNavigationBar 0.163803%, NoResultsContent 0.013066%, QueryTextField 0.284469%, watched MovieCard 0.071069%, WatchedStatsDashboard 1.736257%, watchlist item 0.156176%, GenreChip 2.386627%. (Eight named classes total; XML inspection establishes eight failures, correcting initial message count of nine.) Login/settings tasks also failed Gradle input validation because their `src/test/snapshots` directories do not exist; their new baselines will create the inputs.

Person and search snapshot modules passed. Existing sources, GenreChip test, version catalog and baseline have no current working-tree diff. GenreChip, Banner, and WatchedStats baseline history points to commit `37dc373` (2026-03-08), whose catalog pins Paparazzi 1.3.5 and compileSdk 35; current pins Paparazzi 2.0.0-alpha02 and compileSdk 36. Visual inspection of GenreChip/Banner deltas shows matching text/content and geometry with raster-edge changes. This supports stale-toolchain baselines; it does not alone establish every other delta is acceptable. Parent must review before refreshing.

No full Gradle log was read or exposed. Diagnosis used bounded wrapper JSON, test XML failure messages, generated Gradle problem-report JSON, exact source, Git history, and failure images.

## New screen recording

Question: **Do the eight new screenshot classes conform to Kotlin formatting before recording?** Run 0002 scoped `ktlintFormat` across the six changed feature implementation modules. Answer: No; ProfileScreenScreenshotTest required parent edits for multiline conditional braces. This is a style/setup failure, not behavioral TDD red evidence.

Question: **Can the new full-screen screenshot cases compile and record deterministic baselines?** Run 0003 scoped `recordPaparazziDebug` across login/search/settings/movie-detail/watched/watchlist, with `--tests '*ScreenScreenshotTest*'` after the final task. Answer: Yes, 64 new screen cases recorded: login 6, search 12, see-all 10, settings 4, profile 6, movie detail 6, watched 10, watchlist 10.

The filter applied only to the final task; earlier modules also recorded existing components. This unexpectedly refreshed already reviewed GenreChip, watched MovieCard, WatchedStatsDashboard, and unchanged HeroMovieCard. Parent was notified immediately to restore HeroMovieCard and explicitly decide on only reviewed stale baselines. No snapshot verification claim follows merely from recording; independent verify tasks remain required.

## Fixture correction and approved baseline refresh

Parent visually reviewed all eight legacy deltas and approved their refresh at the current pinned renderer; HeroMovieCard was restored from HEAD and excluded from subsequent recording. New fixtures were corrected to render inside a full-size Material3 Surface, supplying background and content color. This was a test-harness correction, not a production defect. A person Compose test name was shortened solely for max-line-length compliance.

### Run 0004

Question: **Does formatting complete after the profile conditional braces are corrected?**

Command: `./gradlew :feature:settings:impl:ktlintFormat :feature:movie-detail:impl:ktlintFormat :feature:watched-movies:impl:ktlintFormat :feature:watchlist:impl:ktlintFormat :feature:person-detail:impl:ktlintFormat -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false --console=plain --no-scan`

Answer: Failed (exit 1).

### Run 0005

Question: **Do new person loading, error, and empty screen cases record in both themes?**

Command: `./gradlew :feature:person-detail:impl:recordPaparazziDebug --tests *PersonScreenStatesScreenshotTest* -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false --console=plain --no-scan`

Answer: Passed (exit 0).

### Run 0006

Question: **Do the four reviewed design-system baselines record with the current pinned renderer?**

Command: `./gradlew :core:design-system:recordPaparazziDebug --tests *BannerScreenshotTest* --tests *BottomNavigationBarScreenshotTest* --tests *NoResultsContentScreenshotTest* --tests *QueryTextFieldScreenshotTest* -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false --console=plain --no-scan`

Answer: Passed (exit 0).

### Run 0007

Question: **Does the reviewed watchlist-item baseline record with the current pinned renderer?**

Command: `./gradlew :feature:watchlist:impl:recordPaparazziDebug --tests *WatchlistMovieItemScreenshotTest* -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false --console=plain --no-scan`

Answer: Passed (exit 0).

### Run 0008

Question: **Do all new Surface-backed screenshot fixtures format successfully?**

Command: `./gradlew :feature:login:impl:ktlintFormat :feature:search:impl:ktlintFormat :feature:settings:impl:ktlintFormat :feature:movie-detail:impl:ktlintFormat :feature:watched-movies:impl:ktlintFormat :feature:watchlist:impl:ktlintFormat :feature:person-detail:impl:ktlintFormat -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false --console=plain --no-scan`

Answer: Failed (exit 1).

### Run 0009

Question: **Do the corrected Surface-backed new screen fixtures record in both themes?**

Command: `./gradlew :feature:login:impl:recordPaparazziDebug --tests *LoginScreenScreenshotTest* :feature:search:impl:recordPaparazziDebug --tests *ScreenScreenshotTest* :feature:settings:impl:recordPaparazziDebug --tests *ScreenScreenshotTest* :feature:movie-detail:impl:recordPaparazziDebug --tests *MovieDetailScreenScreenshotTest* :feature:watched-movies:impl:recordPaparazziDebug --tests *WatchedMoviesScreenScreenshotTest* :feature:watchlist:impl:recordPaparazziDebug --tests *WatchlistMoviesScreenScreenshotTest* :feature:person-detail:impl:recordPaparazziDebug --tests *PersonScreenStatesScreenshotTest* --continue -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false --console=plain --no-scan`

Answer: Passed (exit 0).


### Run 0010

Question: **Do all screenshot modules independently verify after adding screen coverage and reviewing stale baselines?**

Command: `./gradlew verifyPaparazziDebug --continue -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false --console=plain --no-scan`

Answer: Passed (exit 0). Test XML proves 112 executed screenshot cases in 23 classes across eight modules, zero failures: 76 new cases plus 36 existing. Counts exclude orphan PNGs.

Parent visually reviewed all 76 final corrected captures in ten contact sheets and approved them as current-UI regression baselines. Known limits: existing light-theme white movie navigation icons in loading/error, and initial loader-frame dots are documented; production code was not changed.

## Complete replayable wrapper evidence

Create command: `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py create` returned workflow `aaf28c3e896c59524e8151126b79e04e`.

### Run 0001

Question: **Do all existing screenshot modules verify before adding missing screen coverage?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope broad --question 'Do all existing screenshot modules verify before adding missing screen coverage?' -- ./gradlew verifyPaparazziDebug --continue -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Failed: eight legacy snapshot mismatches and missing login/settings snapshot directories. Person and search modules passed.

### Run 0002

Question: **Do the eight new screenshot classes conform to Kotlin formatting before recording?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope targeted --question 'Do the eight new screenshot classes conform to Kotlin formatting before recording?' -- ./gradlew :feature:login:impl:ktlintFormat :feature:search:impl:ktlintFormat :feature:settings:impl:ktlintFormat :feature:movie-detail:impl:ktlintFormat :feature:watched-movies:impl:ktlintFormat :feature:watchlist:impl:ktlintFormat -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Failed: new ProfileScreenScreenshotTest multiline conditional braces required parent correction. Style/setup failure, not behavioral TDD red.

### Run 0003

Question: **Can the new full-screen screenshot cases compile and record deterministic baselines?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope targeted --question 'Can the new full-screen screenshot cases compile and record deterministic baselines?' -- ./gradlew :feature:login:impl:recordPaparazziDebug :feature:search:impl:recordPaparazziDebug :feature:settings:impl:recordPaparazziDebug :feature:movie-detail:impl:recordPaparazziDebug :feature:watched-movies:impl:recordPaparazziDebug :feature:watchlist:impl:recordPaparazziDebug --tests '*ScreenScreenshotTest*' --continue -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Passed: 64 new screen cases recorded. Filter placement applied only to final task, so earlier tasks also recorded old components; see correction below.

### Run 0004

Question: **Does formatting complete after the profile conditional braces are corrected?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope targeted --question 'Does formatting complete after the profile conditional braces are corrected?' -- ./gradlew :feature:settings:impl:ktlintFormat :feature:movie-detail:impl:ktlintFormat :feature:watched-movies:impl:ktlintFormat :feature:watchlist:impl:ktlintFormat :feature:person-detail:impl:ktlintFormat -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Failed only on PersonScreenStatesScreenshotTest max-line-length; earlier scoped module formatting completed.

### Run 0005

Question: **Do new person loading, error, and empty screen cases record in both themes?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope targeted --question 'Do new person loading, error, and empty screen cases record in both themes?' -- ./gradlew :feature:person-detail:impl:recordPaparazziDebug --tests '*PersonScreenStatesScreenshotTest*' -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Passed: 12 new person loading/error/empty cases recorded.

### Run 0006

Question: **Do the four reviewed design-system baselines record with the current pinned renderer?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope targeted --question 'Do the four reviewed design-system baselines record with the current pinned renderer?' -- ./gradlew :core:design-system:recordPaparazziDebug --tests '*BannerScreenshotTest*' --tests '*BottomNavigationBarScreenshotTest*' --tests '*NoResultsContentScreenshotTest*' --tests '*QueryTextFieldScreenshotTest*' -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Passed: four explicitly reviewed design-system baselines recorded.

### Run 0007

Question: **Does the reviewed watchlist-item baseline record with the current pinned renderer?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope targeted --question 'Does the reviewed watchlist-item baseline record with the current pinned renderer?' -- ./gradlew :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMovieItemScreenshotTest*' -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Passed: explicitly reviewed watchlist-item baseline recorded.

### Run 0008

Question: **Do all new Surface-backed screenshot fixtures format successfully?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope targeted --question 'Do all new Surface-backed screenshot fixtures format successfully?' -- ./gradlew :feature:login:impl:ktlintFormat :feature:search:impl:ktlintFormat :feature:settings:impl:ktlintFormat :feature:movie-detail:impl:ktlintFormat :feature:watched-movies:impl:ktlintFormat :feature:watchlist:impl:ktlintFormat :feature:person-detail:impl:ktlintFormat -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Failed only on overlong existing person Compose test name; parent shortened the name without assertions changes.

### Run 0009

Question: **Do the corrected Surface-backed new screen fixtures record in both themes?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope targeted --question 'Do the corrected Surface-backed new screen fixtures record in both themes?' -- ./gradlew :feature:login:impl:recordPaparazziDebug --tests '*LoginScreenScreenshotTest*' :feature:search:impl:recordPaparazziDebug --tests '*ScreenScreenshotTest*' :feature:settings:impl:recordPaparazziDebug --tests '*ScreenScreenshotTest*' :feature:movie-detail:impl:recordPaparazziDebug --tests '*MovieDetailScreenScreenshotTest*' :feature:watched-movies:impl:recordPaparazziDebug --tests '*WatchedMoviesScreenScreenshotTest*' :feature:watchlist:impl:recordPaparazziDebug --tests '*WatchlistMoviesScreenScreenshotTest*' :feature:person-detail:impl:recordPaparazziDebug --tests '*PersonScreenStatesScreenshotTest*' --continue -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Passed: final corrected Surface-backed nine classes recorded, 76 new cases.

### Run 0010

Question: **Do all screenshot modules independently verify after adding screen coverage and reviewing stale baselines?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope broad --question 'Do all screenshot modules independently verify after adding screen coverage and reviewing stale baselines?' -- ./gradlew verifyPaparazziDebug --continue -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Passed: independent root snapshot verification, 112 executed cases in 23 classes across eight modules, zero failures.

### Run 0011

Question: **Do the aggregate unit, static-analysis, coverage, and debug/release build gates pass after adding screenshot coverage?**

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow aaf28c3e896c59524e8151126b79e04e --scope broad --question 'Do the aggregate unit, static-analysis, coverage, and debug/release build gates pass after adding screenshot coverage?' -- ./gradlew test detekt ktlintCheck koverVerify assembleDebug assembleRelease --continue -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Bounded answer: Passed: test, detekt, ktlintCheck, koverVerify, assembleDebug and assembleRelease, exit 0, 193.059 seconds.

The initial multi-task record filter placement unintentionally updated GenreChip, watched MovieCard/Stats, and HeroMovieCard. Parent restored Hero from HEAD, reviewed all eight legacy deltas, and explicitly approved exactly those eight stale baseline refreshes. Final Git audit confirms only the manifest eight existing tracked PNGs changed. Every baseline was last recorded at commit37dc373 with Paparazzi1.3.5/compileSdk35; current pinned renderer is Paparazzi2.0.0-alpha02/compileSdk36. No comparison tolerance was widened. New Surface fixtures were visually reviewed by parent after recording.

Current warnings are non-failing: JDK native-access/CDS, existing AppIcons Help deprecation, existing Locale constructor deprecation, dependency R8 rule warning. Remote GitHub CI execution and manual live-data/process-death checks were not run by this workflow.

## Finish

Command: `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py finish --workflow aaf28c3e896c59524e8151126b79e04e`. Result: `{"finished": "aaf28c3e896c59524e8151126b79e04e"}`. Finish removed only wrapper-owned logs; source, baselines, reports and this evidence file remain.


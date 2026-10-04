# Chronological filmography year headings

Date: 2026-10-04. Scope: the user approved grouping the complete filmography by release year with subtle headings, omitting repeated card years, and keeping the same layout for acting and crew. The existing chronological order remains; no sorting controls are added. Horizontal profile previews keep their year.

## Changed production and test classes

- `PersonDetailUiState`: `moviesByYear` groups the selected section by release year, retaining the already established date order and the final null-date group. This keeps grouping outside Compose.
- `PersonFilmographyScreen`: renders one accessible, subtly styled heading per year using existing typography and spacing tokens. Stable year and movie keys preserve lazy list behavior. Undated movies use a localized heading.
- `FilmographyMovie` in `PersonDetailScreen`: removes the duplicate release-year line from full-list cards; poster, title, roles and movie click behavior remain. Horizontal `FilmographyPoster` cards retain their year.
- `PersonDetailScreenTest`: adds screen-level checks for multiple movies under a single year heading, year/card order, singleton crew years, undated heading and movie identity. An existing profile interaction test also checks that the horizontal preview still displays 2024. Existing screen state and callback tests remain.
- `PersonDetailScreenScreenshotTest`: updates only the complete-list fixture to include two movies sharing a year, a previous year and an undated movie, in both themes.

English and Spanish resources add No release date / Sin fecha. The existing OpenSpec design, specification and task list describe this approved extension and reuse its recorded screen/screenshot boundary agreement. PRD F-29 describes the grouped complete list. Two intended full-list screenshot baselines are updated after inspection; unrelated user changes and baselines remain outside this extension.

## Test-first and validation evidence

Every Gradle invocation uses `/Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py`, workflow `44013214cf1eebd8d707409ee5ece650`. Full managed build logs were not opened. The common Gradle arguments are:

```text
-Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process -PenableFirebase=false
```

Run 0001 was blocked by sandbox access to the Gradle cache, and run 0002 used the incompatible default Java 25 launcher configuration. Both are setup failures, not behavioral red. Authorized escalated runs use the existing Java 21 configuration and Pixel_9a API 37 emulator.

Runs 0003 and 0004 ask: "Does the complete filmography show one accessible year heading before movies sharing a year?"

```text
./gradlew :feature:person-detail:impl:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.asensiodev.feature.persondetail.impl.presentation.PersonDetailScreenTest#givenMoviesSharingAYear_whenCompleteListRendered_thenOneYearHeadingPrecedesTheirCards
```

Red: the accessible 2024 heading assertion failed at the screen boundary before production edits. Green: the same focused test passed after grouping and removing duplicate card years. Singleton crew/undated assertions protect the implemented grouping as regression coverage; no separate red claim is made for them.

Run 0005 asks: "Do all person screen behaviors pass and can the two updated year-grouped screenshots be recorded?"

```text
./gradlew :feature:person-detail:impl:connectedDebugAndroidTest :feature:person-detail:impl:recordPaparazziDebug --tests '*PersonDetailScreenScreenshotTest.completeFilmography*'
```

Result: all 23 person Compose cases passed; the two intended complete-list screenshots were recorded. Both PNGs were inspected individually: distinct year groups, readable titles/roles, no duplicate card years, and an undated group visible in both light and dark themes.

Runs 0006 and 0007 ask: "Do person JVM tests, all 12 screenshot baselines, screen behaviors, navigation restoration journeys and affected static checks pass?"

Run 0006 failed during task selection because the app uses the `journeyTest` instrumentation variant, not `debug`; no tests ran. Run 0007 corrects that invocation:

```text
./gradlew :feature:person-detail:impl:testDebugUnitTest :feature:person-detail:impl:verifyPaparazziDebug :feature:person-detail:impl:connectedDebugAndroidTest :app:connectedJourneyTestAndroidTest :feature:person-detail:impl:detekt :feature:person-detail:impl:ktlintCheck :core:string-resources:lintDebug
```

Result: passed. Current reports contain 53 person JVM cases (including 24 screenshot cases), all 23 person Compose cases and all 16 app journeys, with zero failures, errors or skips. Paparazzi independently verified all 24 current person baselines, including the 12 profile/complete-list baselines named in the question. Person detekt and ktlintCheck, string-resource lintDebug, OpenSpec strict validation and git diff whitespace checks passed.

The managed invocation for each run is `python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow 44013214cf1eebd8d707409ee5ece650 --scope targeted --question "<question above>" -- <nested command above> <common arguments above>`. Runs 0001 and 0002 precede the common Java configuration described above.

Workflow finish succeeded and removed only wrapper-owned logs:

```text
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py finish --workflow 44013214cf1eebd8d707409ee5ece650
```

## Remaining scope

This extension does not complete the existing manual TalkBack, live TMDB, process-death, unrelated movie snapshot or archive gates. The repository aggregate release gate is not rerun for this focused presentation extension; no new release-ready claim is made.

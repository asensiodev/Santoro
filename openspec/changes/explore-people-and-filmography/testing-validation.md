# Testing follow-up Gradle evidence

Date: 2026-10-04. Workflow: `f1025bcd67391c5fe3c75aebcd955627`.

This follow-up adds regression coverage to existing behavior. It does not retroactively claim TDD. No production behavior changed.

All Gradle commands ran through the compact-output wrapper with the existing JDK 21 and in-process Kotlin compiler. Wrapper-added flags: `--console=plain --no-scan`. Full raw Gradle logs were not reopened or included.

## Run 0001

Question: "Do the expanded person-detail regression tests and Android test sources compile after scoped formatting?"

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow f1025bcd67391c5fe3c75aebcd955627 --scope targeted --question 'Do the expanded person-detail regression tests and Android test sources compile after scoped formatting?' -- ./gradlew :feature:person-detail:impl:ktlintFormat :feature:person-detail:impl:testDebugUnitTest :feature:person-detail:impl:compileDebugAndroidTestKotlin -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process
```

Answer: Failed in scoped formatting: nonautocorrectable line lengths. Parent inspected and shortened the test names and JSON fixture lines before the next run. This was a tooling failure, not behavioral TDD red evidence.

## Run 0002

Question: "Do the expanded person-detail regression tests and Android test sources compile after fixing test line lengths?"

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow f1025bcd67391c5fe3c75aebcd955627 --scope targeted --question 'Do the expanded person-detail regression tests and Android test sources compile after fixing test line lengths?' -- ./gradlew :feature:person-detail:impl:ktlintFormat :feature:person-detail:impl:testDebugUnitTest :feature:person-detail:impl:compileDebugAndroidTestKotlin -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process
```

Answer: Formatting passed; Android compilation failed on invalid top-level androidx.compose.ui.test.onNode import. Parent removed the import; the receiver method remained. This was a compiler failure, not behavioral TDD red evidence.

## Run 0003

Question: "Do person-detail JVM tests and Android test compilation pass after removing the invalid Compose import?"

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow f1025bcd67391c5fe3c75aebcd955627 --scope targeted --question 'Do person-detail JVM tests and Android test compilation pass after removing the invalid Compose import?' -- ./gradlew :feature:person-detail:impl:testDebugUnitTest :feature:person-detail:impl:compileDebugAndroidTestKotlin :feature:person-detail:impl:ktlintCheck -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process
```

Answer: Passed. XML verifies 34 JVM tests including six screenshots, zero failures/errors/skips. Android test sources compile and scoped ktlintCheck passes.

## Run 0004

Question: "Do the expanded person Compose interactions, existing app journeys, independent person snapshots, and person static analysis pass?"

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py run --workflow f1025bcd67391c5fe3c75aebcd955627 --scope targeted --question 'Do the expanded person Compose interactions, existing app journeys, independent person snapshots, and person static analysis pass?' -- ./gradlew :feature:person-detail:impl:connectedDebugAndroidTest :feature:person-detail:impl:verifyPaparazziDebug :app:connectedJourneyTestAndroidTest :feature:person-detail:impl:detekt -Dorg.gradle.java.home=/Users/angelasensio/.gradle/jdks/azul_systems__inc_-21-aarch64-os_x.2/zulu21.50.19-ca-jdk21.0.11-macosx_aarch64/Contents/Home -Pkotlin.compiler.execution.strategy=in-process
```

Answer: Passed. XML verifies 14 Compose interaction tests, 15 app journeys, and six independent screenshot verification cases, zero failures/errors/skips. Person detekt passes.

## Verified counts

| Test class or suite | Cases | Result |
| --- | ---: | --- |
| PersonApiMapperTest | 5 | Pass |
| DefaultPersonRepositoryTest | 5 | Pass |
| PersonApiServiceTest | 1 | Pass |
| PersonRepositoryHttpTest | 6 | Pass |
| PersonDetailViewModelTest | 11 | Pass |
| PersonDetailScreenScreenshotTest | 6 | Pass, including independent verifyPaparazziDebug |
| PersonDetailScreenTest | 14 | Pass on emulator-5554 / Pixel_9a API 37 |
| App journey suite | 15 | Pass on the same emulator |

The JVM total is 34 including the six screenshots; independent screenshot verification does not add six unique tests. JVM and connected XML results report zero failures, errors, or skipped cases.

## Scope and remaining limitations

Scoped formatting, ktlintCheck, detekt, JVM tests, Android test compilation, Compose interactions, app journeys, and person snapshot verification passed. CI configuration was changed by the parent; hosted CI was not executed by this local workflow. The prior aggregate result is historical evidence: no aggregate build was rerun for this test/documentation/CI-only follow-up, and this report makes no new release-ready claim. The previously reported existing GenreChip screenshot mismatch and live-TMDB/process-death manual checks are not resolved by these results. No old baseline was regenerated.

Bounded warnings remained: Gradle native-access and future Gradle 9 deprecation notices; JVM bootstrap classpath sharing; existing app journey fixture Kotlin internal visibility suppression warnings.

## Wrapper lifecycle

```sh
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py create
python3 /Users/angelasensio/.agents/skills/gradle-run/scripts/gradle_run.py finish --workflow f1025bcd67391c5fe3c75aebcd955627
```

Both lifecycle operations passed. Finish removed only wrapper-owned logs.

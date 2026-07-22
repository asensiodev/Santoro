import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.paparazzi) apply false
    alias(libs.plugins.room) apply false
}

subprojects {
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jetbrains.kotlinx.kover")
    }

    ktlint {
        android.set(true)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.HTML)
        }
        filter {
            exclude("**/generated/**")
        }
    }
}

dependencies {
    kover(project(":app"))
    kover(project(":core:auth"))
    kover(project(":core:build-config"))
    kover(project(":core:data"))
    kover(project(":core:database"))
    kover(project(":core:design-system"))
    kover(project(":core:domain"))
    kover(project(":core:network"))
    kover(project(":core:sync"))
    kover(project(":core:ui"))
    kover(project(":feature:login:api"))
    kover(project(":feature:login:impl"))
    kover(project(":feature:movie-detail:api"))
    kover(project(":feature:movie-detail:impl"))
    kover(project(":feature:search-movies:api"))
    kover(project(":feature:search-movies:impl"))
    kover(project(":feature:settings:api"))
    kover(project(":feature:settings:impl"))
    kover(project(":feature:watched-movies:api"))
    kover(project(":feature:watched-movies:impl"))
    kover(project(":feature:watchlist:api"))
    kover(project(":feature:watchlist:impl"))
    kover(project(":library:observability:api"))
    kover(project(":library:observability:impl"))
    kover(project(":library:remote-config:api"))
    kover(project(":library:remote-config:impl"))
    kover(project(":library:secure-storage:api"))
    kover(project(":library:secure-storage:impl"))
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.R",
                    "*.R\$*",
                    "*.Manifest",
                    "*.Manifest\$*",
                    "*ComposableSingletons*",
                    "*Dagger*",
                    "*Hilt_*",
                    "*_Factory",
                    "*_Factory\$*",
                    "*_MembersInjector",
                    "*Database_Impl*",
                    "*Dao_Impl*",
                    "*.di.*",
                )
                annotatedBy(
                    "androidx.compose.runtime.Composable",
                    "dagger.internal.DaggerGenerated",
                )
                inheritedFrom(
                    "android.app.Application",
                    "androidx.activity.ComponentActivity",
                )
            }
        }
        total {
            xml {
                xmlFile =
                    layout.buildDirectory
                        .file("reports/kover/report.xml")
                        .get()
                        .asFile
            }
            html {
                htmlDir =
                    layout.buildDirectory
                        .dir("reports/kover/html")
                        .get()
                        .asFile
            }
            log {
                header = "Santoro aggregate coverage"
                format = "<entity>: <value>%"
            }
            verify {
                rule("Aggregate line coverage") {
                    minBound(75)
                }
                rule("Aggregate branch coverage") {
                    minBound(
                        71,
                        CoverageUnit.BRANCH,
                        AggregationType.COVERED_PERCENTAGE,
                    )
                }
            }
        }
    }
}

detekt {
    val filesProp = project.findProperty("detektFiles") as String?
    if (!filesProp.isNullOrBlank()) {
        val fileList =
            filesProp
                .split(",")
                .filter { it.isNotBlank() }
                .map { file(it) }
        source.setFrom(fileList)
    } else {
        source.setFrom(files("src/main/kotlin"))
    }
}

tasks.register("copyGitHooks") {
    doLast {
        val sourceDir = file("hooks")
        val targetDir = file(".git/hooks")
        sourceDir
            .listFiles()
            ?.forEach { sourceFile ->
                val targetFile = File(targetDir, sourceFile.name)
                if (!targetFile.exists() ||
                    Files.mismatch(sourceFile.toPath(), targetFile.toPath()) != -1L
                ) {
                    Files.copy(
                        sourceFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                    print("> Copied hook: ${sourceFile.name}")
                }
            }
    }
    doLast {
        file(".git/hooks/")
            .walk()
            .forEach { file ->
                if (file.isFile) {
                    try {
                        Files.setPosixFilePermissions(
                            file.toPath(),
                            PosixFilePermissions.fromString("rwxr-xr-x"),
                        )
                    } catch (_: UnsupportedOperationException) {
                        logger.warn("Unable to set POSIX permissions on ${file.name}.")
                    }
                }
            }
    }
}

tasks.register("konsistCheck") {
    group = "verification"
    description = "Runs Konsist architecture tests"

    dependsOn(":architecture-tests:test")
}

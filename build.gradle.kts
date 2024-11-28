import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

subprojects {
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
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

detekt {
    val filesProp = project.findProperty("detektFiles") as String?
    if (!filesProp.isNullOrBlank()) {
        val fileList =
            filesProp.split(",")
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
        sourceDir.listFiles()
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
        file(".git/hooks/").walk()
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

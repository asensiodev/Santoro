package versions

import dependency_handler.implementation
import org.gradle.api.artifacts.dsl.DependencyHandler

object Coroutines {
    private const val version = "1.6.0"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
}

fun DependencyHandler.coroutines() {
    implementation(Coroutines.coroutines)
}
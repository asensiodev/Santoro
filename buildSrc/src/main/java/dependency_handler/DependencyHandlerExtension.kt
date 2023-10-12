package dependency_handler

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.implementation(dependency: String) {
    add("implementation", dependency)
}

fun DependencyHandler.implementation(dependency: Dependency) {
    add("implementation", dependency)
}

fun DependencyHandler.kapt(dependency: String) {
    add("kapt", dependency)
}

fun DependencyHandler.test(dependency: Dependency) {
    add("androidTest", dependency)
}

fun DependencyHandler.test(dependency: String) {
    add("androidTest", dependency)
}

fun DependencyHandler.debugImplementation(dependency: String) {
    add("debugImplementation", dependency)
}


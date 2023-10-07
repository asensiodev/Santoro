plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/base-module.gradle")

android {
    namespace = "com.noirsonora.movie_list.domain"
}

// Specific :movie_list:domain module dependencies
dependencies {
    implementation(project(Modules.core))
}
plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.noirsonora.movie_list.presentation"
}

// Specific :news_feed:presentation module dependencies
dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.movieListDomain))

    implementation(Coil.coilCompose)
}
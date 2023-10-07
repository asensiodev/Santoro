plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/base-module.gradle")

android {
    namespace = "com.noirsonora.movie_lis.domain"
}

// Specific :news_feed:domain module dependencies
dependencies {
    implementation(project(Modules.core))
}
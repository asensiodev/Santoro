plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/base-module.gradle")

android {
    namespace = "com.noirsonora.news_feed.domain"
}

// Specific :news_feed:domain module dependencies
dependencies {
    implementation(project(Modules.core))
}
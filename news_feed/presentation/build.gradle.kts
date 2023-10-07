plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.noirsonora.news_feed.presenation"
}

// Specific :news_feed:presentation module dependencies
dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.newsFeedDomain))
}
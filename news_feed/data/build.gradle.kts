plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/base-module.gradle")

android {
    namespace = "com.noirsonora.news_feed.data"
}

// Specific :news_feed:data module dependencies
dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.newsFeedDomain))

    // Here we need to use "kapt", there is no kapt function

    // Retrofit

    // Room

    // OkHttpLoggingInterceptor

    // MoshiConverter
}
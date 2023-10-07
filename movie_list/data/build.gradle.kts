plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/base-module.gradle")

android {
    namespace = "com.noirsonora.movie_list.data"
}

// Specific :movie_list:data module dependencies
dependencies {
    implementation(project(Modules.core))
    implementation(project(Modules.movieListDomain))

    // Here we need to use "kapt", there is no kapt function

    // Retrofit

    // Room

    // OkHttpLoggingInterceptor

    // MoshiConverter
}
plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.feature.searchmovies.impl"
}

dependencies {
    implementation(projects.feature.searchMovies.api)
    implementation(projects.core.network)
    implementation(projects.library.remoteConfig.api)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.gson)
    implementation(libs.coil.compose)
}

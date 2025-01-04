plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.feature.watchedmovies.impl"
}

dependencies {
    implementation(projects.feature.watchedMovies.api)
    implementation(projects.core.database)

    implementation(libs.bundles.coil)
}

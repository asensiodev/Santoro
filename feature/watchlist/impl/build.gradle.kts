plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.feature.watchlist.impl"
}

dependencies {
    implementation(projects.feature.watchlist.api)
    implementation(projects.core.database)

    implementation(libs.bundles.coil)
}

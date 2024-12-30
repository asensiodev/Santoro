plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.feature.moviedetail.impl"
}

dependencies {
    implementation(projects.feature.movieDetail.api)
    implementation(projects.core.data)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    implementation(libs.gson)
    implementation(libs.bundles.coil)
}

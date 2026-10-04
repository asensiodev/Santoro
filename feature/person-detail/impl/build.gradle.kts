plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.asensiodev.feature.persondetail.impl"
}

dependencies {
    implementation(projects.feature.personDetail.api)
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.bundles.coil)

    testImplementation(projects.core.network)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.retrofit.gson.converter)
}

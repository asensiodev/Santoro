plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.library.observability.impl"
}

dependencies {
    implementation(projects.library.observability.api)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    testImplementation(libs.mockk)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testRuntimeOnly(libs.junit.jupiter.vintage.engine)
}

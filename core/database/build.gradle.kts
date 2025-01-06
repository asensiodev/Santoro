plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.convention.android.room)
}

android {
    namespace = "com.asensiodev.santoro.core.database"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.gson)

    testImplementation(libs.turbine)
}

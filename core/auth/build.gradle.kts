plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.auth"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.stringResources)

    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
}

plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "com.asensiodev.santoro.core.sync"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.auth)

    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.workmanager)
    implementation(libs.workmanager.hilt)
    ksp(libs.hilt.compiler)

    testImplementation(libs.mockk)
    testImplementation(libs.kluent)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.google.play.services.tasks)
    testImplementation(libs.workmanager.testing)
}

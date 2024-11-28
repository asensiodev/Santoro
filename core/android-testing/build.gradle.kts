plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "com.asensiodev.santoro.core.androidtesting"
}

dependencies {
    implementation(libs.junit.jupiter.api)
    implementation(libs.kotest)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.mockk)
}

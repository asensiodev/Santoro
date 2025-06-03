plugins {
    alias(libs.plugins.convention.jvm.library)
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.junit.jupiter.api)
    implementation(libs.kotest)
    implementation(libs.mockk)
    implementation(libs.kotlinx.coroutines.test)
}

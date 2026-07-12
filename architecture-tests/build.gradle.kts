plugins {
    alias(libs.plugins.convention.jvm.library)
}

dependencies {
    testImplementation(libs.konsist)
}

tasks.withType<Test> {
    outputs.upToDateWhen { false }
}

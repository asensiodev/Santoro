plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/base-module.gradle")

android {
    namespace = "com.noirsonora.core"
}

// Specific :core module dependencies
dependencies {

}
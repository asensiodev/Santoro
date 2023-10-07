plugins {
    `android-library`
    `kotlin-android`
}

apply (from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.noirsonora.core_ui"
}

// Specific :core_ui module dependencies

dependencies {

}

package versions

import dependency_handler.implementation
import org.gradle.api.artifacts.dsl.DependencyHandler

object AndroidX {
    private const val coreKtxVersion = "1.9.0"
    const val coreKtx = "androidx.core:core-ktx:$coreKtxVersion"

    private const val appCompatVersion = "1.4.0"
    const val appCompat = "androidx.appcompat:appcompat:$appCompatVersion"

    private const val lifecycleRuntimeVersion = "2.6.2"
    const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleRuntimeVersion"

    private const val lifecycleComposeVersion = "2.6.2"
    const val lifecycleCompose = "androidx.lifecycle:lifecycle-runtime-compose:$lifecycleComposeVersion"


    private const val splashScreenVersion = "1.0.0-beta02"
    const val splashScreen = "androidx.core:core-splashscreen:$splashScreenVersion"

    private const val dataStoreVersion = "1.0.0"
    const val dataStore = "androidx.datastore:datastore-preferences:$dataStoreVersion"
}

fun DependencyHandler.androidX() {
    implementation(AndroidX.coreKtx)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.lifecycleRuntime)
    implementation(AndroidX.lifecycleCompose)
    implementation(AndroidX.splashScreen)
    implementation(AndroidX.dataStore)
}
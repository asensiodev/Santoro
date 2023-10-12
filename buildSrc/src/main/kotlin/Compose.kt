import dependency_handler.debugImplementation
import dependency_handler.implementation
import org.gradle.api.artifacts.dsl.DependencyHandler

object Compose {
    private const val composeBomVersion = "2023.03.00"
    const val composeCompilerVersion = "1.4.3"
    const val composeBom = "androidx.compose:compose-bom:$composeBomVersion"
    const val activityComposeVersion = "1.4.0"

    const val ui = "androidx.compose.ui:ui"
    const val uiGrapichs = "androidx.compose.ui:ui-graphics"
    const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
    const val material3 = "androidx.compose.material3:material3"
    const val compiler = "androidx.compose.compiler:compiler:${composeCompilerVersion}"
    //const val navigation = "androidx.navigation:navigation-compose"
    //const val hiltNavigationCompose = "androidx.hilt:hilt-navigation-compose"
    const val activityCompose = "androidx.activity:activity-compose:${activityComposeVersion}"
    const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose"
    const val runtime = "androidx.compose.runtime:runtime"
    const val composeUiTooling = "androidx.compose.ui:ui-tooling"
}

fun DependencyHandler.compose() {
    implementation(platform(Compose.composeBom))
    implementation(Compose.ui)
    implementation(Compose.uiGrapichs)
    debugImplementation(Compose.uiToolingPreview)
    implementation(Compose.material3)
    implementation(Compose.compiler)
    //implementation(Compose.navigation)
    //implementation(Compose.hiltNavigationCompose)
    implementation(Compose.activityCompose)
    implementation(Compose.viewModelCompose)
    implementation(Compose.runtime)
    implementation(Compose.composeUiTooling)
}

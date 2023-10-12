import dependency_handler.implementation
import dependency_handler.kapt
import org.gradle.api.artifacts.dsl.DependencyHandler

object DaggerHilt {
    private const val version = "2.40"
    const val hiltAndroid = "com.google.dagger:hilt-android:$version"
    const val hiltCompiler = "com.google.dagger:hilt-android-compiler:$version"
}

fun DependencyHandler.daggerHilt() {
    implementation(DaggerHilt.hiltAndroid)
    kapt(DaggerHilt.hiltCompiler)
}
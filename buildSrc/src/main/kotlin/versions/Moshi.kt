package versions

import dependency_handler.kapt
import org.gradle.api.artifacts.dsl.DependencyHandler

object Moshi {
    private const val version = "1.13.0"
    const val moshiCodegen = "kapt 'com.squareup.moshi:moshi-kotlin-codegen:$version'"
}

fun DependencyHandler.moshi() {
    kapt(Moshi.moshiCodegen)
}
package gradle_plugins

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import versions.ProjectConfig

open class BaseGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        applyPlugins(project)
        setProjectConfiguration(project)
    }

    private fun applyPlugins(project: Project) {
        project.apply {
            plugin(ANDROID_LIBRARY)
            plugin(KOTLIN_ANDROID)
            plugin(HILT_ANDROID_PLUGIN)
            plugin(KOTLIN_KAPT)
        }
    }

    private fun setProjectConfiguration(project: Project) {
        project.extensions.configure<LibraryExtension>(ANDROID_EXTENSION_CONFIG) {
            compileSdk = ProjectConfig.compileSdk
            defaultConfig {
                minSdk = ProjectConfig.minSdk
                testInstrumentationRunner = ANDROID_TEST_RUNNER
            }
            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_18
                targetCompatibility = JavaVersion.VERSION_18
            }
        }
    }

    companion object {
        const val ANDROID_LIBRARY = "android-library"
        const val KOTLIN_ANDROID = "kotlin-android"
        const val HILT_ANDROID_PLUGIN = "dagger.hilt.android.plugin"
        const val KOTLIN_KAPT = "kotlin-kapt"
        const val ANDROID_EXTENSION_CONFIG = "android"
        const val ANDROID_TEST_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
    }

}
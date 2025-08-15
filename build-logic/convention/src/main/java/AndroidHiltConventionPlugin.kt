import com.asensiodev.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.withPlugin("com.android.base") {
                pluginManager.apply("com.google.dagger.hilt.android")
            }
            dependencies {
                "ksp"(libs.findLibrary("dagger.hilt.compiler").get())
                "implementation"(libs.findLibrary("dagger.hilt").get())
                "implementation"(libs.findLibrary("hilt-navigation-compose").get())
            }
        }
    }
}

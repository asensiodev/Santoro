import com.asensiodev.buildlogic.convention.logic.configureKotlinJvm
import com.asensiodev.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("com.asensiodev.santoro.buildlogic.convention.jvm-test")
            }
            configureKotlinJvm()
            dependencies {
                "implementation"(libs.findLibrary("kotlinx-coroutines-core").get())
            }
        }
    }
}

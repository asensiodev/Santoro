import org.gradle.api.Plugin
import org.gradle.api.Project
import com.asensiodev.buildlogic.convention.logic.configureKotlinJvm

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("com.asensiodev.santoro.buildlogic.convention.jvm-test")
            }
            configureKotlinJvm()
        }
    }
}

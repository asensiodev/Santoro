import com.asensiodev.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("com.asensiodev.santoro.buildlogic.convention.android-library-compose")
                apply("com.asensiodev.santoro.buildlogic.convention.android-hilt")
                apply("com.asensiodev.santoro.buildlogic.convention.android-test")
            }

            dependencies {
                add("implementation", project(":core:design-system"))
                add("implementation", project(":core:string-resources"))
                add("implementation", project(":core:domain"))

                "ksp"(libs.findLibrary("hilt-compiler").get())
                "implementation"(libs.findLibrary("androidx-navigation-compose").get())
            }
        }
    }
}

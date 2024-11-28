import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import com.asensiodev.buildlogic.convention.logic.libs

class JvmTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            tasks.withType<Test> {
                useJUnitPlatform()
            }

            dependencies {
                "testImplementation"(project(":core:testing"))
                "testImplementation"(libs.findLibrary("junit.jupiter.api").get())
                "testImplementation"(libs.findLibrary("junit.jupiter.params").get())
                "testRuntimeOnly"(libs.findLibrary("junit.jupiter.engine").get())
                "testImplementation"(libs.findLibrary("mockk").get())
                "testImplementation"(libs.findLibrary("kluent").get())
                "testImplementation"(libs.findLibrary("kotest").get())
                "testImplementation"(libs.findLibrary("turbine").get())
                "testImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
            }
        }
    }
}

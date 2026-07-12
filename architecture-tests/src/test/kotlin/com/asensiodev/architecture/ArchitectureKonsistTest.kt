package com.asensiodev.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

class ArchitectureKonsistTest {
    @Test
    fun `GIVEN domain production code WHEN inspected THEN it has no Android dependencies`() {
        productionFiles()
            .filter { file -> file.projectPath.contains("core/domain/src/main") }
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("android.") || import.name.startsWith("androidx.")
                }
            }
    }

    @Test
    fun `GIVEN production ViewModels WHEN inspected THEN they avoid forbidden state and dispatcher APIs`() {
        productionFiles()
            .filter { file -> file.projectPath.endsWith("ViewModel.kt") }
            .assertFalse { file ->
                file.hasTextContaining("GlobalScope") ||
                    file.hasTextContaining("Dispatchers.IO") ||
                    file.hasTextContaining("mutableStateOf")
            }
    }

    @Test
    fun `GIVEN production Compose code WHEN inspected THEN it uses lifecycle aware state collection`() {
        productionFiles()
            .assertFalse { file -> file.hasTextContaining("collectAsState(") }
    }

    @Test
    fun `GIVEN production code WHEN inspected THEN it does not use GlobalScope`() {
        productionFiles()
            .assertFalse { file -> file.hasTextContaining("GlobalScope") }
    }

    private fun productionFiles() =
        Konsist
            .scopeFromProject()
            .files
            .filter { file -> file.projectPath.contains("src/main/") }
}

package com.asensiodev.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

class ArchitectureKonsistTest {
    @Test
    fun `GIVEN domain production code WHEN inspected THEN it has only pure dependencies`() {
        productionFiles()
            .filter { file -> file.projectPath.contains("core/domain/src/main") }
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("android.") ||
                        import.name.startsWith("androidx.") ||
                        import.name.startsWith("com.google.firebase.") ||
                        import.name.startsWith("com.asensiodev.santoro.core.database.")
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
    fun `GIVEN presentation production code WHEN inspected THEN it avoids concrete sync infrastructure`() {
        productionFiles()
            .filter { file -> file.projectPath.contains("feature/") }
            .assertFalse { file ->
                file.hasTextContaining("WorkManagerSyncScheduler") ||
                    file.hasTextContaining("com.asensiodev.santoro.core.sync")
            }
    }

    @Test
    fun `GIVEN database read contract WHEN inspected THEN it exposes reads only`() {
        productionFiles()
            .filter { file -> file.projectPath.endsWith("/domain/DatabaseRepository.kt") }
            .assertFalse { file ->
                file.hasTextContaining("updateMovieState(") ||
                    file.hasTextContaining("removeFromWatchlist(") ||
                    file.hasTextContaining("getMoviesForSync(") ||
                    file.hasTextContaining("upsertMovieFromSync(") ||
                    file.hasTextContaining("updateMovieSyncState(") ||
                    file.hasTextContaining("clearAllUserData(")
            }
    }

    @Test
    fun `GIVEN feature production code WHEN inspected THEN movie database internals are not imported`() {
        productionFiles()
            .filter { file -> file.projectPath.contains("feature/") }
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name == "com.asensiodev.santoro.core.database.data.dao.MovieDao" ||
                        import.name == "com.asensiodev.santoro.core.database.data.SantoroRoomDatabase" ||
                        import.name ==
                        "com.asensiodev.santoro.core.database.data.repository.RoomDatabaseRepository"
                }
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

package com.asensiodev.feature.searchmovies.impl.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreRecentSearchesDataSourceTest {
    private val tempDir: Path = Files.createTempDirectory("recent-searches-test")

    @AfterEach
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `GIVEN searches WHEN saved THEN newest search is first`() =
        runTest {
            withFixture("insertion") { fixture ->
                fixture.dataSource.saveSearch("first")
                fixture.dataSource.saveSearch("second")

                fixture.dataSource.getRecentSearches().first() shouldBeEqualTo listOf("second", "first")
            }
        }

    @Test
    fun `GIVEN existing search WHEN saved again THEN it moves to first without duplication`() =
        runTest {
            withFixture("deduplication") { fixture ->
                fixture.dataSource.saveSearch("first")
                fixture.dataSource.saveSearch("second")

                fixture.dataSource.saveSearch("first")

                fixture.dataSource.getRecentSearches().first() shouldBeEqualTo listOf("first", "second")
            }
        }

    @Test
    fun `GIVEN more than five searches WHEN saved THEN only five newest remain`() =
        runTest {
            withFixture("maximum-size") { fixture ->
                (1..6).forEach { query -> fixture.dataSource.saveSearch("query-$query") }

                fixture.dataSource.getRecentSearches().first() shouldBeEqualTo
                    listOf("query-6", "query-5", "query-4", "query-3", "query-2")
            }
        }

    @Test
    fun `GIVEN saved searches WHEN clearAll THEN history is empty`() =
        runTest {
            withFixture("clear") { fixture ->
                fixture.dataSource.saveSearch("first")
                fixture.dataSource.saveSearch("second")

                fixture.dataSource.clearAll()

                fixture.dataSource.getRecentSearches().first() shouldBeEqualTo emptyList()
            }
        }

    @Test
    fun `GIVEN persisted searches WHEN data source is recreated THEN history remains available`() =
        runTest {
            val firstFixture = createFixture("recreation")
            firstFixture.dataSource.saveSearch("persisted")
            firstFixture.close()

            val recreatedFixture = createFixture("recreation")
            try {
                recreatedFixture.dataSource.getRecentSearches().first() shouldBeEqualTo listOf("persisted")
            } finally {
                recreatedFixture.close()
            }
        }

    @Test
    fun `GIVEN blank stored JSON WHEN read and saved THEN history recovers from empty`() =
        runTest {
            withFixture("blank-json") { fixture ->
                fixture.dataStore.edit { preferences -> preferences[recentSearchesKey] = "" }

                fixture.dataSource.getRecentSearches().first() shouldBeEqualTo emptyList()

                fixture.dataSource.saveSearch("new")

                fixture.dataSource.getRecentSearches().first() shouldBeEqualTo listOf("new")
            }
        }

    @Test
    fun `GIVEN malformed stored JSON WHEN read THEN flow fails and next save recovers`() =
        runTest {
            withFixture("malformed-json") { fixture ->
                fixture.dataStore.edit { preferences -> preferences[recentSearchesKey] = "not-json" }

                val failure =
                    runCatching { fixture.dataSource.getRecentSearches().first() }
                        .exceptionOrNull()

                failure.shouldBeInstanceOf<JsonSyntaxException>()

                fixture.dataSource.saveSearch("recovered")

                fixture.dataSource.getRecentSearches().first() shouldBeEqualTo listOf("recovered")
            }
        }

    @Test
    fun `GIVEN overlapping saves WHEN completed THEN no search is lost`() =
        runTest {
            withFixture("concurrency") { fixture ->
                val queries = (1..5).map { query -> "query-$query" }

                coroutineScope {
                    queries
                        .map { query -> async { fixture.dataSource.saveSearch(query) } }
                        .awaitAll()
                }

                val savedQueries = fixture.dataSource.getRecentSearches().first()
                savedQueries.size shouldBeEqualTo queries.size
                savedQueries.toSet() shouldBeEqualTo queries.toSet()
            }
        }

    private suspend fun TestScope.withFixture(
        name: String,
        block: suspend (Fixture) -> Unit,
    ) {
        val fixture = createFixture(name)
        try {
            block(fixture)
        } finally {
            fixture.close()
        }
    }

    private fun TestScope.createFixture(name: String): Fixture {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val dataStore =
            PreferenceDataStoreFactory.create(scope = scope) {
                tempDir.resolve("$name.preferences_pb").toFile()
            }
        return Fixture(
            dataStore = dataStore,
            dataSource = DataStoreRecentSearchesDataSource(dataStore, Gson()),
            scope = scope,
        )
    }

    private class Fixture(
        val dataStore: DataStore<Preferences>,
        val dataSource: DataStoreRecentSearchesDataSource,
        private val scope: CoroutineScope,
    ) {
        suspend fun close() {
            scope.coroutineContext.job.cancelAndJoin()
        }
    }

    private companion object {
        val recentSearchesKey = stringPreferencesKey("recent_searches")
    }
}

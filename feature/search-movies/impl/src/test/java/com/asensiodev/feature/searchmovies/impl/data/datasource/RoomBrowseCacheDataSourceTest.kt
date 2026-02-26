package com.asensiodev.feature.searchmovies.impl.data.datasource

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.repository.BrowseSectionKeys
import com.asensiodev.santoro.core.data.mapper.toApiModel
import com.asensiodev.santoro.core.data.model.MovieApiModel
import com.asensiodev.santoro.core.database.data.dao.BrowseCacheDao
import com.asensiodev.santoro.core.database.data.model.BrowseCacheEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomBrowseCacheDataSourceTest {
    private val dao: BrowseCacheDao = mockk(relaxed = true)
    private val gson = Gson()

    private lateinit var dataSource: RoomBrowseCacheDataSource

    private val sampleMovie =
        Movie(
            id = 1,
            title = "Inception",
            overview = "A mind-bending thriller.",
            posterPath = "/inception.jpg",
            backdropPath = null,
            releaseDate = "2010-07-16",
            popularity = 8.3,
            voteAverage = 8.8,
            voteCount = 32000,
            genres = emptyList(),
            productionCountries = emptyList(),
            isWatched = false,
            isInWatchlist = false,
        )

    @BeforeEach
    fun setUp() {
        dataSource = RoomBrowseCacheDataSource(dao, gson)
    }

    @Test
    fun `GIVEN dao returns null WHEN getCachedPage THEN returns null`() =
        runTest {
            coEvery { dao.getPage(any(), any()) } returns null

            val result = dataSource.getCachedPage(BrowseSectionKeys.POPULAR, 1)

            result.shouldBeNull()
        }

    @Test
    fun `GIVEN dao returns entity WHEN getCachedPage THEN deserializes and returns BrowseCacheEntry`() =
        runTest {
            val apiModel = sampleMovie.toApiModel()
            val json = gson.toJson(listOf(apiModel))
            val entity =
                BrowseCacheEntity(
                    section = BrowseSectionKeys.POPULAR,
                    page = 1,
                    moviesJson = json,
                    cachedAt = 12345L,
                )
            coEvery { dao.getPage(BrowseSectionKeys.POPULAR, 1) } returns entity

            val result = dataSource.getCachedPage(BrowseSectionKeys.POPULAR, 1)

            result.shouldNotBeNull()
            result.movies.size shouldBeEqualTo 1
            result.movies.first().id shouldBeEqualTo sampleMovie.id
            result.movies.first().title shouldBeEqualTo sampleMovie.title
            result.cachedAt shouldBeEqualTo 12345L
        }

    @Test
    fun `GIVEN movie list WHEN savePage THEN serializes and passes correct entity to DAO`() =
        runTest {
            val entitySlot = slot<BrowseCacheEntity>()
            coEvery { dao.upsertPage(capture(entitySlot)) } returns Unit

            dataSource.savePage(BrowseSectionKeys.POPULAR, 1, listOf(sampleMovie), 99999L)

            val captured = entitySlot.captured
            captured.section shouldBeEqualTo BrowseSectionKeys.POPULAR
            captured.page shouldBeEqualTo 1
            captured.cachedAt shouldBeEqualTo 99999L

            val type = object : TypeToken<List<MovieApiModel>>() {}.type
            val deserialized: List<MovieApiModel> = gson.fromJson(captured.moviesJson, type)
            deserialized.size shouldBeEqualTo 1
            deserialized.first().id shouldBeEqualTo sampleMovie.id
        }

    @Test
    fun `GIVEN cutoff WHEN clearStaleEntries THEN passes correct cutoff to DAO`() =
        runTest {
            val cutoff = 50000L
            dataSource.clearStaleEntries(cutoff)

            coVerify(exactly = 1) { dao.clearEntriesOlderThan(cutoff) }
        }
}

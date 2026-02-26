package com.asensiodev.feature.searchmovies.impl.data.repository

import app.cash.turbine.test
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.feature.searchmovies.impl.data.datasource.BrowseCacheLocalDataSource
import com.asensiodev.feature.searchmovies.impl.data.datasource.SearchMoviesDatasource
import com.asensiodev.feature.searchmovies.impl.data.model.BrowseCacheEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

class CachingSearchMoviesRepositoryTest {
    private val localDataSource: BrowseCacheLocalDataSource = mockk(relaxed = true)
    private val remoteDatasource: SearchMoviesDatasource = mockk()
    private val dispatchers = TestDispatcherProvider()

    private lateinit var repository: CachingSearchMoviesRepository

    private val sampleMovies =
        listOf(
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
            ),
        )

    @BeforeEach
    fun setUp() {
        repository = CachingSearchMoviesRepository(localDataSource, remoteDatasource, dispatchers)
    }

    @Test
    fun `GIVEN fresh cache WHEN getPopularMovies THEN emits cached movies without calling remote`() =
        runTest {
            val freshEntry =
                BrowseCacheEntry(
                    section = BrowseSectionKeys.POPULAR,
                    page = 1,
                    movies = sampleMovies,
                    cachedAt = System.currentTimeMillis(),
                )
            coEvery { localDataSource.getCachedPage(BrowseSectionKeys.POPULAR, 1) } returns freshEntry

            repository.getPopularMovies(1).test {
                awaitItem() shouldBeEqualTo Result.Success(sampleMovies)
                awaitComplete()
            }

            coVerify(exactly = 0) { remoteDatasource.getPopularMovies(any()) }
        }

    @Test
    fun `GIVEN expired cache WHEN getPopularMovies THEN calls remote saves result and emits fresh movies`() =
        runTest {
            val staleEntry =
                BrowseCacheEntry(
                    section = BrowseSectionKeys.POPULAR,
                    page = 1,
                    movies = sampleMovies,
                    cachedAt = System.currentTimeMillis() - BrowseCacheTtl.CURATED_MS - 1000L,
                )
            coEvery { localDataSource.getCachedPage(BrowseSectionKeys.POPULAR, 1) } returns staleEntry
            coEvery { remoteDatasource.getPopularMovies(1) } returns Result.Success(sampleMovies)

            repository.getPopularMovies(1).test {
                awaitItem() shouldBeEqualTo Result.Success(sampleMovies)
                awaitComplete()
            }

            coVerify(exactly = 1) { remoteDatasource.getPopularMovies(1) }
            coVerify(exactly = 1) { localDataSource.savePage(BrowseSectionKeys.POPULAR, 1, sampleMovies, any()) }
        }

    @Test
    fun `GIVEN no cache and network success WHEN getPopularMovies THEN saves and emits movies`() =
        runTest {
            coEvery { localDataSource.getCachedPage(BrowseSectionKeys.POPULAR, 1) } returns null
            coEvery { remoteDatasource.getPopularMovies(1) } returns Result.Success(sampleMovies)

            repository.getPopularMovies(1).test {
                awaitItem() shouldBeEqualTo Result.Success(sampleMovies)
                awaitComplete()
            }

            coVerify(exactly = 1) { localDataSource.savePage(BrowseSectionKeys.POPULAR, 1, sampleMovies, any()) }
        }

    @Test
    fun `GIVEN no cache and IOException WHEN getPopularMovies THEN emits error`() =
        runTest {
            coEvery { localDataSource.getCachedPage(BrowseSectionKeys.POPULAR, 1) } returns null
            coEvery { remoteDatasource.getPopularMovies(1) } throws IOException("Network error")

            repository.getPopularMovies(1).test {
                val error = awaitItem() as Result.Error
                error.exception shouldBeInstanceOf IOException::class
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN stale cache and IOException WHEN getPopularMovies THEN emits stale movies then StaleDataException`() =
        runTest {
            val staleEntry =
                BrowseCacheEntry(
                    section = BrowseSectionKeys.POPULAR,
                    page = 1,
                    movies = sampleMovies,
                    cachedAt = System.currentTimeMillis() - BrowseCacheTtl.CURATED_MS - 1000L,
                )
            coEvery { localDataSource.getCachedPage(BrowseSectionKeys.POPULAR, 1) } returns staleEntry
            coEvery { remoteDatasource.getPopularMovies(1) } throws IOException("Network error")

            repository.getPopularMovies(1).test {
                awaitItem() shouldBeEqualTo Result.Success(sampleMovies)
                val error = awaitItem() as Result.Error
                error.exception shouldBeInstanceOf StaleDataException::class
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN fresh cache for inception page 1 WHEN searchMovies THEN returns cached result without calling remote`() =
        runTest {
            val section = BrowseSectionKeys.searchKey("inception")
            val freshEntry =
                BrowseCacheEntry(
                    section = section,
                    page = 1,
                    movies = sampleMovies,
                    cachedAt = System.currentTimeMillis(),
                )
            coEvery { localDataSource.getCachedPage(section, 1) } returns freshEntry

            repository.searchMovies("inception", 1).test {
                awaitItem() shouldBeEqualTo Result.Success(sampleMovies)
                awaitComplete()
            }

            coVerify(exactly = 0) { remoteDatasource.searchMovies(any(), any()) }
        }

    @Test
    fun `GIVEN no cache for inception WHEN searchMovies THEN calls remote and caches result`() =
        runTest {
            val section = BrowseSectionKeys.searchKey("inception")
            coEvery { localDataSource.getCachedPage(section, 1) } returns null
            coEvery { remoteDatasource.searchMovies("inception", 1) } returns Result.Success(sampleMovies)

            repository.searchMovies("inception", 1).test {
                awaitItem() shouldBeEqualTo Result.Success(sampleMovies)
                awaitComplete()
            }

            coVerify(exactly = 1) { localDataSource.savePage(section, 1, sampleMovies, any()) }
        }

    @Test
    fun `GIVEN different queries WHEN searchMovies THEN treated as different cache keys`() =
        runTest {
            val inceptionSection = BrowseSectionKeys.searchKey("inception")
            val matrixSection = BrowseSectionKeys.searchKey("matrix")

            inceptionSection shouldBeEqualTo "search:inception"
            matrixSection shouldBeEqualTo "search:matrix"
            (inceptionSection == matrixSection) shouldBeEqualTo false
        }
}

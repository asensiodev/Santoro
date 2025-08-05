package com.asensiodev.feature.moviedetail.impl.data.datasource.repository

import app.cash.turbine.test
import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.moviedetail.impl.data.datasource.LocalMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.datasource.RemoteMovieDetailDataSource
import com.asensiodev.feature.moviedetail.impl.data.repository.DefaultMovieDetailRepository
import com.asensiodev.feature.moviedetail.impl.data.repository.MovieNotFoundException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultMovieDetailRepositoryTest {
    private val localDataSource: LocalMovieDetailDataSource = mockk()
    private val remoteDataSource: RemoteMovieDetailDataSource = mockk()

    private lateinit var repository: DefaultMovieDetailRepository

    @BeforeEach
    fun setUp() {
        repository = DefaultMovieDetailRepository(localDataSource, remoteDataSource)
    }

    @Test
    fun `GIVEN local and remote success WHEN getMovieDetail THEN returns merged movie`() =
        runTest {
            val movieId = 1
            val localMovie =
                Movie(
                    id = movieId,
                    title = "Local Movie",
                    overview = "Local Overview",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 10.0,
                    voteAverage = 8.5,
                    voteCount = 100,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = true,
                    isInWatchlist = false,
                )
            val remoteMovie =
                Movie(
                    id = movieId,
                    title = "Remote Movie",
                    overview = "Remote Overview",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 20.0,
                    voteAverage = 9.0,
                    voteCount = 200,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = false,
                    isInWatchlist = true,
                )
            val expectedMergedMovie =
                remoteMovie.copy(
                    isWatched = localMovie.isWatched,
                    isInWatchlist = localMovie.isInWatchlist,
                )

            every { localDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(localMovie))
            every { remoteDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(remoteMovie))

            repository.getMovieDetail(movieId).test {
                awaitItem() shouldBeEqualTo Result.Success(expectedMergedMovie)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN local success WHEN getMovieDetail THEN returns local movie`() =
        runTest {
            val movieId = 2
            val localMovie =
                Movie(
                    id = movieId,
                    title = "Local Movie",
                    overview = "Local Overview",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 5.0,
                    voteAverage = 7.0,
                    voteCount = 50,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = true,
                    isInWatchlist = true,
                )

            every { localDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(localMovie))
            every { remoteDataSource.getMovieDetail(movieId) } returns flowOf(Result.Error(Exception()))

            repository.getMovieDetail(movieId).test {
                awaitItem() shouldBeEqualTo Result.Success(localMovie)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN remote null data and local data null WHEN getMovieDetail THEN returns MovieNotFoundException`() =
        runTest {
            val movieId = 3

            every { localDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(null))
            every { remoteDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(null))

            repository.getMovieDetail(movieId).test {
                val result = awaitItem()
                result.shouldBeInstanceOf<Result.Error>()
                (result as Result.Error).exception.shouldBeInstanceOf<MovieNotFoundException>()
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN remote error and no local data WHEN getMovieDetail THEN returns remote error`() =
        runTest {
            val movieId = 4
            val remoteException = Exception("Remote fetch error")

            every { localDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(null))
            every { remoteDataSource.getMovieDetail(movieId) } returns flowOf(Result.Error(remoteException))

            repository.getMovieDetail(movieId).test {
                val result = awaitItem()
                result.shouldBeInstanceOf<Result.Error>()
                (result as Result.Error).exception shouldBeEqualTo remoteException
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN remote error and local data WHEN getMovieDetail THEN returns local data`() =
        runTest {
            val movieId = 5
            val localMovie =
                Movie(
                    id = movieId,
                    title = "Local Movie",
                    overview = "Local Overview",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 15.0,
                    voteAverage = 8.0,
                    voteCount = 150,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = false,
                    isInWatchlist = true,
                )
            val remoteException = Exception("Remote fetch error")

            every { localDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(localMovie))
            every { remoteDataSource.getMovieDetail(movieId) } returns flowOf(Result.Error(remoteException))

            repository.getMovieDetail(movieId).test {
                awaitItem() shouldBeEqualTo Result.Success(localMovie)
                awaitComplete()
            }
        }

    @Test
    fun `GIVEN local and remote data WHEN updateMovieState THEN updates state and returns success`() =
        runTest {
            val movie =
                Movie(
                    id = 8,
                    title = "Updated Movie",
                    overview = "Updated Overview",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 25.0,
                    voteAverage = 9.5,
                    voteCount = 250,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = true,
                    isInWatchlist = true,
                )

            coEvery { localDataSource.updateMovieState(movie) } returns Result.Success(true)

            val result = repository.updateMovieState(movie)

            result shouldBeEqualTo Result.Success(true)
            coVerify(exactly = 1) { localDataSource.updateMovieState(movie) }
        }

    @Test
    fun `GIVEN error updating movie state WHEN updateMovieState THEN returns error`() =
        runTest {
            val movie =
                Movie(
                    id = 9,
                    title = "Failed Update Movie",
                    overview = "Failed Overview",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 5.0,
                    voteAverage = 5.0,
                    voteCount = 50,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = false,
                    isInWatchlist = false,
                )
            val updateException = Exception("Update failed")

            coEvery { localDataSource.updateMovieState(movie) } returns Result.Error(updateException)

            val result = repository.updateMovieState(movie)

            result shouldBeEqualTo Result.Error(updateException)
            coVerify(exactly = 1) { localDataSource.updateMovieState(movie) }
        }

    @Test
    fun `GIVEN remote and local data WHEN getMovieDetail THEN returns only one merged result`() =
        runTest {
            val movieId = 10
            val localMovie =
                Movie(
                    id = movieId,
                    title = "Local Movie",
                    overview = "Local Overview",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 12.0,
                    voteAverage = 7.5,
                    voteCount = 120,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = true,
                    isInWatchlist = true,
                )
            val remoteMovie =
                Movie(
                    id = movieId,
                    title = "Remote Movie",
                    overview = "Remote Overview",
                    posterPath = null,
                    releaseDate = null,
                    popularity = 22.0,
                    voteAverage = 8.5,
                    voteCount = 220,
                    genres = listOf(),
                    productionCountries = listOf(),
                    isWatched = false,
                    isInWatchlist = false,
                )
            val expectedMergedMovie =
                remoteMovie.copy(
                    isWatched = localMovie.isWatched,
                    isInWatchlist = localMovie.isInWatchlist,
                )

            every { localDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(localMovie))
            every { remoteDataSource.getMovieDetail(movieId) } returns flowOf(Result.Success(remoteMovie))

            repository.getMovieDetail(movieId).test {
                awaitItem() shouldBeEqualTo Result.Success(expectedMergedMovie)
                awaitComplete()
            }
        }
}

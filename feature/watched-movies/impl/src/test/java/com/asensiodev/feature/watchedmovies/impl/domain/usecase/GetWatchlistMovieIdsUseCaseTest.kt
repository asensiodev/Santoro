package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.testing.dispatcher.TestDispatcherProvider
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.IOException

class GetWatchlistMovieIdsUseCaseTest {
    private val repository: DatabaseRepository = mockk()
    private val useCase = GetWatchlistMovieIdsUseCase(repository, TestDispatcherProvider())

    @Test
    fun `GIVEN watchlist movies WHEN invoked THEN emits their ids`() =
        runTest {
            every { repository.getWatchlistMovies() } returns
                flowOf(Result.success(listOf(movie(1), movie(2))))

            useCase().toList() shouldBeEqualTo listOf(setOf(1, 2))
        }

    @Test
    fun `GIVEN a read failure after ids were emitted WHEN invoked THEN the known ids are kept`() =
        runTest {
            every { repository.getWatchlistMovies() } returns
                flowOf(Result.success(listOf(movie(1))), Result.failure(IOException("disk")))

            useCase().toList() shouldBeEqualTo listOf(setOf(1))
        }

    private fun movie(id: Int) =
        Movie(
            id = id,
            title = "Movie $id",
            overview = "",
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            popularity = 0.0,
            voteAverage = 0.0,
            voteCount = 0,
            genres = emptyList(),
            productionCountries = emptyList(),
            isWatched = false,
            isInWatchlist = true,
        )
}

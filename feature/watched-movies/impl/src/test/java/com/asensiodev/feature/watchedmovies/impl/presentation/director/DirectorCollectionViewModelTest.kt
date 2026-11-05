package com.asensiodev.feature.watchedmovies.impl.presentation.director

import com.asensiodev.core.domain.model.Director
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.core.domain.repository.MovieRuntimeRepository
import com.asensiodev.core.domain.repository.PersonRepository
import com.asensiodev.core.domain.repository.UserPreferencesRepository
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import com.asensiodev.feature.watchedmovies.impl.domain.usecase.GetWatchedMoviesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DirectorCollectionViewModelTest {
    private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase = mockk()
    private val personRepository: PersonRepository = mockk()
    private val movieRuntimeRepository: MovieRuntimeRepository = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val watchedMovies = MutableStateFlow(Result.success(listOf(watched(1, "Memento", "2000-09-05"))))
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getWatchedMoviesUseCase() } returns watchedMovies
        every { userPreferencesRepository.directorShowPending } returns flowOf(true)
        coEvery { personRepository.getMovieCredits(NOLAN) } returns filmography()
        coEvery { personRepository.getPerson(NOLAN) } returns Result.success(person(PHOTO))
        coEvery { movieRuntimeRepository.getRuntimes(any()) } returns emptyMap()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN filmography still loading WHEN initialized THEN shows the name and local watched films`() =
        runTest {
            coEvery { personRepository.getMovieCredits(NOLAN) } coAnswers { CompletableDeferred<Nothing>().await() }
            watchedMovies.value =
                Result.success(listOf(watched(1, "Memento", "2000-09-05"), watched(7, "Other", "2001-01-01", 99)))
            val viewModel = initializedViewModel()

            viewModel.uiState.value shouldBeEqualTo
                DirectorCollectionUiState(
                    directorName = NAME,
                    profilePath = PHOTO,
                    films = listOf(watchedFilm(1, "Memento", "2000-09-05")),
                    watchedCount = 1,
                    totalCount = null,
                    filmographyState = FilmographyState.LOADING,
                )
        }

    @Test
    fun `GIVEN filmography loads WHEN initialized THEN merges watched marks with the feature films`() =
        runTest {
            val state = initializedViewModel().uiState.value

            state.films shouldBeEqualTo
                listOf(
                    watchedFilm(1, "Memento", "2000-09-05"),
                    DirectorFilm(2, "Inception", "/2.jpg", "2010-07-16", isWatched = false),
                    DirectorFilm(3, "Oppenheimer", "/3.jpg", "2023-07-19", isWatched = false),
                )
            state.watchedCount shouldBeEqualTo 1
            state.totalCount shouldBeEqualTo 3
            state.progressPercent shouldBeEqualTo 33
            state.filmographyState shouldBeEqualTo FilmographyState.CONTENT
        }

    @Test
    fun `GIVEN filmography fails WHEN retried THEN keeps watched films with an error and then loads the total`() =
        runTest {
            coEvery { personRepository.getMovieCredits(NOLAN) } returnsMany
                listOf(Result.failure(IOException("offline")), filmography())
            val viewModel = initializedViewModel()

            viewModel.uiState.value.filmographyState shouldBeEqualTo FilmographyState.ERROR
            viewModel.uiState.value.films shouldBeEqualTo listOf(watchedFilm(1, "Memento", "2000-09-05"))
            viewModel.uiState.value.totalCount shouldBeEqualTo null

            viewModel.process(DirectorCollectionIntent.RetryFilmography)
            advanceUntilIdle()

            viewModel.uiState.value.filmographyState shouldBeEqualTo FilmographyState.CONTENT
            viewModel.uiState.value.totalCount shouldBeEqualTo 3
        }

    @Test
    fun `GIVEN the request is cancelled WHEN loading THEN no error is shown`() =
        runTest {
            coEvery { personRepository.getMovieCredits(NOLAN) } returns Result.failure(CancellationException("left"))
            val viewModel = initializedViewModel()

            viewModel.uiState.value.filmographyState shouldBeEqualTo FilmographyState.LOADING
        }

    @Test
    fun `GIVEN a slow request WHEN retried THEN only the latest result is applied`() =
        runTest {
            val slow = CompletableDeferred<Result<PersonFilmography>>()
            coEvery { personRepository.getMovieCredits(NOLAN) } coAnswers { slow.await() } andThen filmography()
            val viewModel = initializedViewModel()

            viewModel.process(DirectorCollectionIntent.RetryFilmography)
            advanceUntilIdle()
            slow.complete(Result.failure(IOException("stale")))
            advanceUntilIdle()

            viewModel.uiState.value.filmographyState shouldBeEqualTo FilmographyState.CONTENT
            viewModel.uiState.value.totalCount shouldBeEqualTo 3
        }

    @Test
    fun `GIVEN loaded filmography WHEN a film is marked watched THEN marks update without a new request`() =
        runTest {
            val viewModel = initializedViewModel()
            viewModel.process(DirectorCollectionIntent.Initialize(NOLAN, NAME))

            watchedMovies.value =
                Result.success(listOf(watched(1, "Memento", "2000-09-05"), watched(2, "Inception", "2010-07-16")))
            advanceUntilIdle()

            viewModel.uiState.value.watchedCount shouldBeEqualTo 2
            viewModel.uiState.value.totalCount shouldBeEqualTo 3
            viewModel.uiState.value.films
                .single { it.movieId == 2 }
                .isWatched shouldBeEqualTo true
            coVerify(exactly = 1) { personRepository.getMovieCredits(NOLAN) }
        }

    @Test
    fun `GIVEN saved hidden pending WHEN loaded THEN only watched films are visible and the summary keeps the total`() =
        runTest {
            every { userPreferencesRepository.directorShowPending } returns flowOf(false)
            val state = initializedViewModel().uiState.value

            state.showPending shouldBeEqualTo false
            state.visibleFilms shouldBeEqualTo listOf(watchedFilm(1, "Memento", "2000-09-05"))
            state.watchedCount shouldBeEqualTo 1
            state.totalCount shouldBeEqualTo 3
        }

    @Test
    fun `GIVEN show pending is changed WHEN the write fails THEN keeps the choice for the session`() =
        runTest {
            coEvery { userPreferencesRepository.setDirectorShowPending(false) } throws IOException("disk")
            val viewModel = initializedViewModel()

            viewModel.process(DirectorCollectionIntent.ChangeShowPending(false))
            advanceUntilIdle()

            viewModel.uiState.value.showPending shouldBeEqualTo false
            viewModel.uiState.value.visibleFilms
                .map { it.movieId } shouldBeEqualTo listOf(1)
            coVerify { userPreferencesRepository.setDirectorShowPending(false) }
        }

    @Test
    fun `GIVEN the preference read fails WHEN loaded THEN pending films are shown`() =
        runTest {
            every { userPreferencesRepository.directorShowPending } returns flow { throw IOException("disk") }

            initializedViewModel().uiState.value.showPending shouldBeEqualTo true
        }

    @Test
    fun `GIVEN a director with a photo WHEN initialized THEN the header shows the photo`() =
        runTest {
            initializedViewModel().uiState.value.profilePath shouldBeEqualTo PHOTO
        }

    @Test
    fun `GIVEN the profile request fails WHEN initialized THEN shows no photo and still loads the filmography`() =
        runTest {
            coEvery { personRepository.getPerson(NOLAN) } returns Result.failure(IOException("offline"))
            val state = initializedViewModel().uiState.value

            state.profilePath shouldBeEqualTo null
            state.filmographyState shouldBeEqualTo FilmographyState.CONTENT
            state.totalCount shouldBeEqualTo 3
        }

    @Test
    fun `GIVEN the filmography fails WHEN retried THEN the profile is not requested again`() =
        runTest {
            coEvery { personRepository.getMovieCredits(NOLAN) } returns Result.failure(IOException("offline"))
            val viewModel = initializedViewModel()

            viewModel.process(DirectorCollectionIntent.RetryFilmography)
            advanceUntilIdle()

            viewModel.uiState.value.profilePath shouldBeEqualTo PHOTO
            coVerify(exactly = 1) { personRepository.getPerson(NOLAN) }
        }

    @Test
    fun `GIVEN shorts in the filmography WHEN loaded THEN only feature films count and shorts are apart`() =
        runTest {
            coEvery { personRepository.getMovieCredits(NOLAN) } returns
                filmography(credit(5, "Doodlebug", "1997-01-01"))
            coEvery { movieRuntimeRepository.getRuntimes(any()) } returns mapOf(5 to 3, 2 to 148, 3 to 180)
            val state = initializedViewModel().uiState.value

            state.films.map { it.movieId } shouldBeEqualTo listOf(1, 2, 3)
            state.shorts shouldBeEqualTo listOf(DirectorFilm(5, "Doodlebug", "/5.jpg", "1997-01-01", isWatched = false))
            state.watchedCount shouldBeEqualTo 1
            state.totalCount shouldBeEqualTo 3
            state.shortsWatchedCount shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN credits loaded WHEN runtimes are requested THEN only films without a stored runtime are looked up`() =
        runTest {
            watchedMovies.value = Result.success(listOf(watched(1, "Memento", "2000-09-05", runtime = 113)))

            initializedViewModel()

            coVerify(exactly = 1) { movieRuntimeRepository.getRuntimes(listOf(2, 3)) }
        }

    @Test
    fun `GIVEN runtimes still loading WHEN credits arrive THEN the filmography stays loading with local films only`() =
        runTest {
            coEvery { movieRuntimeRepository.getRuntimes(any()) } coAnswers { CompletableDeferred<Nothing>().await() }
            val state = initializedViewModel().uiState.value

            state.filmographyState shouldBeEqualTo FilmographyState.LOADING
            state.totalCount shouldBeEqualTo null
            state.films shouldBeEqualTo listOf(watchedFilm(1, "Memento", "2000-09-05"))
        }

    @Test
    fun `GIVEN a watched short stored locally WHEN the filmography is loading THEN it already shows among shorts`() =
        runTest {
            coEvery { personRepository.getMovieCredits(NOLAN) } coAnswers { CompletableDeferred<Nothing>().await() }
            watchedMovies.value =
                Result.success(
                    listOf(watched(1, "Memento", "2000-09-05"), watched(5, "Doodlebug", "1997-01-01", runtime = 3)),
                )
            val state = initializedViewModel().uiState.value

            state.films shouldBeEqualTo listOf(watchedFilm(1, "Memento", "2000-09-05"))
            state.shorts shouldBeEqualTo listOf(watchedFilm(5, "Doodlebug", "1997-01-01"))
            state.watchedCount shouldBeEqualTo 1
            state.shortsWatchedCount shouldBeEqualTo 1
        }

    private fun person(profilePath: String?) = Person(NOLAN, NAME, "", profilePath, null, null, null)

    private suspend fun kotlinx.coroutines.test.TestScope.initializedViewModel(): DirectorCollectionViewModel {
        val viewModel =
            DirectorCollectionViewModel(
                getWatchedMoviesUseCase,
                personRepository,
                movieRuntimeRepository,
                userPreferencesRepository,
                CLOCK,
            )
        viewModel.process(DirectorCollectionIntent.Initialize(NOLAN, NAME))
        advanceUntilIdle()
        return viewModel
    }

    private fun filmography(vararg extraCredits: PersonMovieCredit) =
        Result.success(
            PersonFilmography(
                acting = emptyList(),
                crew =
                    listOf(
                        credit(2, "Inception", "2010-07-16"),
                        credit(1, "Memento", "2000-09-05"),
                        credit(3, "Oppenheimer", "2023-07-19"),
                        credit(4, "Future", "2030-01-01"),
                    ) + extraCredits,
            ),
        )

    private fun credit(
        id: Int,
        title: String,
        releaseDate: String,
    ) = PersonMovieCredit(id, title, "/$id.jpg", releaseDate, listOf("Director"))

    private fun watchedFilm(
        id: Int,
        title: String,
        releaseDate: String,
    ) = DirectorFilm(id, title, "/$id.jpg", releaseDate, isWatched = true)

    private fun watched(
        id: Int,
        title: String,
        releaseDate: String,
        directorId: Int = NOLAN,
        runtime: Int? = null,
    ) = Movie(
        id = id,
        title = title,
        overview = "",
        posterPath = "/$id.jpg",
        backdropPath = null,
        releaseDate = releaseDate,
        popularity = 0.0,
        voteAverage = 0.0,
        voteCount = 0,
        genres = emptyList(),
        productionCountries = emptyList(),
        directors = listOf(Director(directorId, "Director $directorId")),
        runtime = runtime,
        isWatched = true,
        isInWatchlist = false,
    )

    private companion object {
        const val NOLAN = 525
        const val NAME = "Christopher Nolan"
        const val PHOTO = "/nolan.jpg"
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-04T10:00:00Z"), ZoneOffset.UTC)
    }
}

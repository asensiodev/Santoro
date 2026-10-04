package com.asensiodev.feature.persondetail.impl.presentation

import androidx.lifecycle.ViewModelStore
import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.repository.PersonRepository
import com.asensiodev.core.testing.extension.CoroutineTestExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class PersonDetailViewModelTest {
    @RegisterExtension
    val coroutines = CoroutineTestExtension()
    private val repository = mockk<PersonRepository>()
    private val person = Person(7, "Actor", "Biography", null, null, null, null)
    private val emptyCredits = PersonFilmography(emptyList(), emptyList())

    @Test
    fun `GIVEN available data WHEN initialized twice THEN profile loads once and empty credits remain content`() =
        runTest {
            coEvery { repository.getPerson(7) } returns Result.success(person)
            coEvery { repository.getMovieCredits(7) } returns Result.success(emptyCredits)
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            vm.uiState.value.person shouldBeEqualTo person
            vm.uiState.value.profileState shouldBeEqualTo PersonLoadState.Content
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Content
            coVerify(exactly = 1) { repository.getPerson(7) }
            coVerify(exactly = 1) { repository.getMovieCredits(7) }
        }

    @Test
    fun `GIVEN profile failure WHEN retried THEN details and filmography recover`() =
        runTest {
            coEvery { repository.getPerson(7) } returnsMany
                listOf(Result.failure(IllegalStateException()), Result.success(person))
            coEvery { repository.getMovieCredits(7) } returns Result.success(emptyCredits)
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            vm.uiState.value.profileState shouldBeEqualTo PersonLoadState.Error
            coVerify(exactly = 0) { repository.getMovieCredits(any()) }
            vm.process(PersonDetailIntent.RetryProfile)
            runCurrent()
            vm.uiState.value.person shouldBeEqualTo person
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Content
        }

    @Test
    fun `GIVEN credits fail WHEN retried THEN biography stays and profile loads once`() =
        runTest {
            coEvery { repository.getPerson(7) } returns Result.success(person)
            coEvery { repository.getMovieCredits(7) } returnsMany
                listOf(Result.failure(IllegalStateException()), Result.success(emptyCredits))
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Error
            vm.uiState.value.person shouldBeEqualTo person
            vm.process(PersonDetailIntent.RetryFilmography)
            runCurrent()
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Content
            coVerify(exactly = 1) { repository.getPerson(7) }
            coVerify(exactly = 2) { repository.getMovieCredits(7) }
        }

    @Test
    fun `GIVEN loading request WHEN duplicate retries arrive THEN one request owns the load`() =
        runTest {
            val pending = CompletableDeferred<Result<Person>>()
            coEvery { repository.getPerson(7) } coAnswers { pending.await() }
            coEvery { repository.getMovieCredits(7) } returns Result.success(emptyCredits)
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            vm.process(PersonDetailIntent.RetryProfile)
            vm.process(PersonDetailIntent.RetryFilmography)
            pending.complete(Result.success(person))
            runCurrent()
            coVerify(exactly = 1) { repository.getPerson(7) }
            coVerify(exactly = 1) { repository.getMovieCredits(7) }
        }

    @Test
    fun `GIVEN pending credits WHEN ViewModel clears THEN request cancels without an error`() =
        runTest {
            val cancelled = CompletableDeferred<Unit>()
            coEvery { repository.getPerson(7) } returns Result.success(person)
            coEvery { repository.getMovieCredits(7) } coAnswers {
                try {
                    awaitCancellation()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val vm = PersonDetailViewModel(repository)
            val store = ViewModelStore().apply { put("person", vm) }
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            store.clear()
            runCurrent()
            cancelled.isCompleted shouldBeEqualTo true
            vm.uiState.value.person shouldBeEqualTo person
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Loading
        }

    @Test
    fun `GIVEN cancellation wrapped in Result WHEN profile loads THEN no user error is emitted`() =
        runTest {
            coEvery { repository.getPerson(7) } returns Result.failure(CancellationException())
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            vm.uiState.value.profileState shouldBeEqualTo PersonLoadState.Loading
            coVerify(exactly = 0) { repository.getMovieCredits(any()) }
        }

    @Test
    fun `GIVEN pending details WHEN initialized THEN profile stays loading until details arrive`() =
        runTest {
            val pending = CompletableDeferred<Result<Person>>()
            coEvery { repository.getPerson(7) } coAnswers { pending.await() }
            coEvery { repository.getMovieCredits(7) } returns Result.success(emptyCredits)
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            vm.uiState.value.person shouldBeEqualTo null
            vm.uiState.value.profileState shouldBeEqualTo PersonLoadState.Loading
            coVerify(exactly = 0) { repository.getMovieCredits(any()) }
            pending.complete(Result.success(person))
            runCurrent()
            vm.uiState.value.person shouldBeEqualTo person
            vm.uiState.value.profileState shouldBeEqualTo PersonLoadState.Content
        }

    @Test
    fun `GIVEN pending credits WHEN details arrive THEN biography becomes available before filmography`() =
        runTest {
            val pending = CompletableDeferred<Result<PersonFilmography>>()
            coEvery { repository.getPerson(7) } returns Result.success(person)
            coEvery { repository.getMovieCredits(7) } coAnswers { pending.await() }
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            vm.uiState.value.person shouldBeEqualTo person
            vm.uiState.value.profileState shouldBeEqualTo PersonLoadState.Content
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Loading
            pending.complete(Result.success(emptyCredits))
            runCurrent()
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Content
        }

    @Test
    fun `GIVEN credits recovery WHEN retry repeats THEN biography stays and one recovery runs`() =
        runTest {
            val pending = CompletableDeferred<Result<PersonFilmography>>()
            coEvery { repository.getPerson(7) } returns Result.success(person)
            coEvery { repository.getMovieCredits(7) } returns Result.failure(IllegalStateException())
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            coEvery { repository.getMovieCredits(7) } coAnswers { pending.await() }
            vm.process(PersonDetailIntent.RetryFilmography)
            runCurrent()
            vm.process(PersonDetailIntent.RetryFilmography)
            vm.uiState.value.person shouldBeEqualTo person
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Loading
            pending.complete(Result.success(emptyCredits))
            runCurrent()
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Content
            coVerify(exactly = 2) { repository.getMovieCredits(7) }
        }

    @Test
    fun `GIVEN pending profile WHEN ViewModel clears THEN profile request cancels without user error`() =
        runTest {
            val cancelled = CompletableDeferred<Unit>()
            coEvery { repository.getPerson(7) } coAnswers {
                try {
                    awaitCancellation()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val vm = PersonDetailViewModel(repository)
            val store = ViewModelStore().apply { put("person", vm) }
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            store.clear()
            runCurrent()
            cancelled.isCompleted shouldBeEqualTo true
            vm.uiState.value.profileState shouldBeEqualTo PersonLoadState.Loading
            vm.uiState.value.person shouldBeEqualTo null
            coVerify(exactly = 0) { repository.getMovieCredits(any()) }
        }

    @Test
    fun `GIVEN cancellation wrapped in credits result WHEN loaded THEN biography remains without user error`() =
        runTest {
            coEvery { repository.getPerson(7) } returns Result.success(person)
            coEvery { repository.getMovieCredits(7) } returns Result.failure(CancellationException())
            val vm = PersonDetailViewModel(repository)
            vm.process(PersonDetailIntent.Initialize(7))
            runCurrent()
            vm.uiState.value.person shouldBeEqualTo person
            vm.uiState.value.profileState shouldBeEqualTo PersonLoadState.Content
            vm.uiState.value.filmographyState shouldBeEqualTo PersonLoadState.Loading
        }
}

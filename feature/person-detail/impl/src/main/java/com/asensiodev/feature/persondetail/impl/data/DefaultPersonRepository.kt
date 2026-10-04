package com.asensiodev.feature.persondetail.impl.data

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.repository.PersonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

internal class DefaultPersonRepository(
    private val service: PersonApiService,
    private val dispatchers: DispatcherProvider,
) : PersonRepository {
    override suspend fun getPerson(id: Int): Result<Person> =
        request {
            service.person(id).toDomain().also { require(it.id == id) }
        }

    override suspend fun getMovieCredits(id: Int): Result<PersonFilmography> =
        request { service.movieCredits(id).toDomain() }

    private suspend fun <T> request(block: suspend () -> T): Result<T> =
        withContext(dispatchers.io) {
            try {
                Result.success(block())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
}

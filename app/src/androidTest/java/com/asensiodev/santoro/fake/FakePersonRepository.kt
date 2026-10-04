package com.asensiodev.santoro.fake

import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.core.domain.repository.PersonRepository
import com.asensiodev.santoro.AppJourneyTestData
import java.util.concurrent.CopyOnWriteArrayList

class FakePersonRepository : PersonRepository {
    val requestedCreditIds = CopyOnWriteArrayList<Int>()
    var extraMovies: List<PersonMovieCredit> = emptyList()

    val requestedPersonIds = CopyOnWriteArrayList<Int>()

    override suspend fun getPerson(id: Int): Result<Person> {
        requestedPersonIds += id
        return Result.success(
            Person(
                id,
                if (id ==
                    AppJourneyTestData.ACTOR_ID
                ) {
                    AppJourneyTestData.ACTOR_NAME
                } else {
                    AppJourneyTestData.DIRECTOR_NAME
                },
                "Journey biography",
                null,
                null,
                null,
                null,
            ),
        )
    }

    override suspend fun getMovieCredits(id: Int): Result<PersonFilmography> {
        requestedCreditIds += id
        val movie = AppJourneyTestData.trendingMovie
        val credit =
            PersonMovieCredit(movie.id, movie.title, null, movie.releaseDate, listOf("Lead"))
        return Result.success(PersonFilmography(listOf(credit) + extraMovies, emptyList()))
    }

    fun reset() {
        requestedPersonIds.clear()
        requestedCreditIds.clear()
        extraMovies = emptyList()
    }
}

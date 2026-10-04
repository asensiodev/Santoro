package com.asensiodev.core.domain.repository

import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography

interface PersonRepository {
    suspend fun getPerson(id: Int): Result<Person>

    suspend fun getMovieCredits(id: Int): Result<PersonFilmography>
}

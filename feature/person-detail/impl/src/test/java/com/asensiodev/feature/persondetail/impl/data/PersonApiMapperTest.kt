package com.asensiodev.feature.persondetail.impl.data

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class PersonApiMapperTest {
    @Test
    fun `GIVEN nullable fields WHEN mapping profile THEN missing facts are normalized`() {
        val person = PersonApiModel(7, "Actor", null, "", "invalid", null, " ").toDomain()
        person.id shouldBeEqualTo 7
        person.biography shouldBeEqualTo ""
        person.profilePath shouldBeEqualTo null
        person.birthday shouldBeEqualTo null
        person.birthplace shouldBeEqualTo null
    }

    @Test
    fun `GIVEN full profile WHEN mapping THEN dates and description are preserved`() {
        val person =
            PersonApiModel(
                7,
                "Actor",
                " Biography ",
                "/portrait",
                "1980-01-02",
                "2020-03-04",
                "Madrid",
            ).toDomain()
        person.biography shouldBeEqualTo "Biography"
        person.profilePath shouldBeEqualTo "/portrait"
        person.birthday shouldBeEqualTo "1980-01-02"
        person.deathday shouldBeEqualTo "2020-03-04"
        person.birthplace shouldBeEqualTo "Madrid"
    }

    @Test
    fun `GIVEN multiple roles and dates WHEN mapping THEN movies group per section with stable ordering`() {
        val credits =
            PersonCreditsApiModel(
                cast =
                    listOf(
                        credit(4, null, "Unknown"),
                        credit(3, "2020-01-01", "Hero"),
                        credit(2, "2020-01-01", "Narrator"),
                        credit(3, "2020-01-01", "Villain"),
                        credit(3, "2020-01-01", "Hero"),
                        credit(1, "2024-01-01", "Lead"),
                    ),
                crew = listOf(credit(3, "2020-01-01", job = "Director"), credit(3, "2020-01-01", job = "Writer")),
            ).toDomain()
        credits.acting.map { it.movieId } shouldBeEqualTo listOf(1, 2, 3, 4)
        credits.acting.first { it.movieId == 3 }.roles shouldBeEqualTo listOf("Hero", "Villain")
        credits.crew.single().roles shouldBeEqualTo listOf("Director", "Writer")
    }

    @Test
    fun `GIVEN absent credits WHEN mapping THEN filmography is empty`() {
        val credits = PersonCreditsApiModel(null, null).toDomain()
        credits.acting shouldBeEqualTo emptyList()
        credits.crew shouldBeEqualTo emptyList()
    }

    @Test
    fun `GIVEN malformed movies WHEN mapped THEN invalid rows are omitted and missing fields are optional`() {
        val credits =
            PersonCreditsApiModel(
                listOf(credit(0, null), credit(1, null).copy(title = " "), credit(2, "bad").copy(posterPath = " ")),
                null,
            ).toDomain()
        credits.acting.single().movieId shouldBeEqualTo 2
        credits.acting.single().releaseDate shouldBeEqualTo null
        credits.acting.single().posterPath shouldBeEqualTo null
        credits.acting.single().roles shouldBeEqualTo emptyList()
    }

    private fun credit(
        id: Int,
        date: String?,
        character: String? = null,
        job: String? = null,
    ) = PersonCreditApiModel(id, "Movie $id", null, date, character, job)
}

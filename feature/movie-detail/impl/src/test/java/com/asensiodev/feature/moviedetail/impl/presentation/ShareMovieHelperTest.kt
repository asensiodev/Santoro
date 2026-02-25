package com.asensiodev.feature.moviedetail.impl.presentation

import android.content.Context
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.asensiodev.santoro.core.stringresources.R as SR

class ShareMovieHelperTest {
    private val context: Context = mockk(relaxed = true)

    private val inceptionMovie =
        MovieUi(
            id = 27205,
            title = "Inception",
            overview = "A thief who steals corporate secrets.",
            posterPath = null,
            releaseDate = "2010-07-16",
            popularity = 8.3,
            voteAverage = 8.8,
            voteCount = 32000,
            genres = emptyList(),
            productionCountries = emptyList(),
            cast = emptyList(),
            runtime = "2h 28m",
            director = "Christopher Nolan",
            isWatched = false,
            isInWatchlist = false,
        )

    @BeforeEach
    fun setUp() {
        every {
            context.getString(SR.string.movie_detail_share_text, any(), any(), any(), any())
        } answers {
            val title = it.invocation.args[1] as String
            val year = it.invocation.args[2] as String
            val rating = it.invocation.args[3] as Double
            val id = it.invocation.args[4] as Int
            "🎬 $title ($year)\n⭐ ${"%.1f".format(
                rating,
            )}/10\n\nCheck it out on TMDB:\nhttps://www.themoviedb.org/movie/$id"
        }
    }

    @Test
    fun `GIVEN a movie WHEN buildShareText THEN text contains title`() {
        val result = ShareMovieHelper.buildShareText(inceptionMovie, context)

        result shouldContain "Inception"
    }

    @Test
    fun `GIVEN a movie WHEN buildShareText THEN text contains release year`() {
        val result = ShareMovieHelper.buildShareText(inceptionMovie, context)

        result shouldContain "2010"
    }

    @Test
    fun `GIVEN a movie WHEN buildShareText THEN text contains rating`() {
        val result = ShareMovieHelper.buildShareText(inceptionMovie, context)

        result shouldContain "8.8"
    }

    @Test
    fun `GIVEN a movie WHEN buildShareText THEN text contains TMDB URL with movie id`() {
        val result = ShareMovieHelper.buildShareText(inceptionMovie, context)

        result shouldContain "https://www.themoviedb.org/movie/27205"
    }

    @Test
    fun `GIVEN a movie with null releaseDate WHEN buildShareText THEN year is empty`() {
        val movieWithoutDate = inceptionMovie.copy(releaseDate = null)

        val result = ShareMovieHelper.buildShareText(movieWithoutDate, context)

        result shouldContain "()"
    }
}

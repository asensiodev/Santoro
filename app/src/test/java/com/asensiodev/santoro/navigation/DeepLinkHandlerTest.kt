package com.asensiodev.santoro.navigation

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class DeepLinkHandlerTest {
    @Test
    fun `GIVEN valid TMDB movie URL WHEN parseMovieIdFromUrl THEN returns correct movie ID`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl("https://www.themoviedb.org/movie/27205")

        result shouldBeEqualTo 27205
    }

    @Test
    fun `GIVEN TMDB URL with slug WHEN parseMovieIdFromUrl THEN returns numeric ID only`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl("https://www.themoviedb.org/movie/27205-inception")

        result shouldBeEqualTo 27205
    }

    @Test
    fun `GIVEN http scheme WHEN parseMovieIdFromUrl THEN returns correct movie ID`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl("http://www.themoviedb.org/movie/550")

        result shouldBeEqualTo 550
    }

    @Test
    fun `GIVEN URL without www WHEN parseMovieIdFromUrl THEN returns correct movie ID`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl("https://themoviedb.org/movie/550")

        result shouldBeEqualTo 550
    }

    @Test
    fun `GIVEN URL without movie segment WHEN parseMovieIdFromUrl THEN returns null`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl("https://www.themoviedb.org/person/12345")

        result shouldBeEqualTo null
    }

    @Test
    fun `GIVEN URL with non-numeric ID WHEN parseMovieIdFromUrl THEN returns null`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl("https://www.themoviedb.org/movie/abc")

        result shouldBeEqualTo null
    }

    @Test
    fun `GIVEN null URL WHEN parseMovieIdFromUrl THEN returns null`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl(null)

        result shouldBeEqualTo null
    }

    @Test
    fun `GIVEN URL with only movie path and no ID WHEN parseMovieIdFromUrl THEN returns null`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl("https://www.themoviedb.org/movie/")

        result shouldBeEqualTo null
    }

    @Test
    fun `GIVEN empty string WHEN parseMovieIdFromUrl THEN returns null`() {
        val result = DeepLinkHandler.parseMovieIdFromUrl("")

        result shouldBeEqualTo null
    }

    @Test
    fun `GIVEN null intent WHEN parseMovieIdFromIntent THEN returns null`() {
        val result = DeepLinkHandler.parseMovieIdFromIntent(null)

        result shouldBeEqualTo null
    }
}

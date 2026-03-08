package com.asensiodev.santoro.navigation

import android.content.Intent

internal object DeepLinkHandler {
    fun parseMovieIdFromUrl(url: String?): Int? {
        val path =
            url
                ?.substringAfter("://", "")
                ?.substringAfter("/", "")
                ?: return null
        val segments = path.split("/").filter { it.isNotEmpty() }
        return segments
            .takeIf { it.size >= PATH_SEGMENTS_MIN_SIZE && it[0] == MOVIE_SEGMENT }
            ?.let { it[1].substringBefore(SLUG_SEPARATOR).toIntOrNull() }
    }

    fun parseMovieIdFromIntent(intent: Intent?): Int? =
        parseMovieIdFromUrl(intent?.data?.toString())
}

private const val MOVIE_SEGMENT = "movie"
private const val SLUG_SEPARATOR = '-'
private const val PATH_SEGMENTS_MIN_SIZE = 2

package com.asensiodev.santoro.navigation

import android.content.Intent
import java.net.URI

internal object DeepLinkHandler {
    fun parseMovieIdFromUrl(url: String?): Int? =
        url
            ?.let { runCatching { URI(it) }.getOrNull() }
            ?.takeIf { uri -> uri.scheme in SUPPORTED_SCHEMES && uri.host in SUPPORTED_HOSTS }
            ?.path
            ?.split("/")
            ?.filter { segment -> segment.isNotEmpty() }
            ?.takeIf { segments ->
                segments.size >= PATH_SEGMENTS_MIN_SIZE &&
                    segments[0] == MOVIE_SEGMENT
            }?.let { segments -> segments[1].substringBefore(SLUG_SEPARATOR).toIntOrNull() }

    fun parseMovieIdFromIntent(intent: Intent?): Int? =
        parseMovieIdFromUrl(intent?.data?.toString())
}

private const val MOVIE_SEGMENT = "movie"
private const val SLUG_SEPARATOR = '-'
private const val PATH_SEGMENTS_MIN_SIZE = 2
private val SUPPORTED_SCHEMES = setOf("http", "https")
private val SUPPORTED_HOSTS = setOf("themoviedb.org", "www.themoviedb.org")

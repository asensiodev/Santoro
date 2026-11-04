package com.asensiodev.feature.watchedmovies.impl.presentation.director

import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import kotlin.math.roundToInt

internal enum class FilmographyState {
    LOADING,
    CONTENT,
    ERROR,
}

internal data class DirectorCollectionUiState(
    val directorName: String = "",
    val films: List<DirectorFilm> = emptyList(),
    val watchedCount: Int = 0,
    val totalCount: Int? = null,
    val filmographyState: FilmographyState = FilmographyState.LOADING,
    val showPending: Boolean = true,
) {
    val visibleFilms: List<DirectorFilm> = if (showPending) films else films.filter { it.isWatched }
    val progressPercent: Int? =
        totalCount?.takeIf { it > 0 }?.let { total ->
            (watchedCount * PERCENT / total.toFloat()).roundToInt()
        }
}

private const val PERCENT = 100

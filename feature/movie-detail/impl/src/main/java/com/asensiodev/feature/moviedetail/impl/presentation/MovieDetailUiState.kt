package com.asensiodev.feature.moviedetail.impl.presentation

import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi

internal sealed interface MovieDetailScreenState {
    data object Loading : MovieDetailScreenState
    data object Content : MovieDetailScreenState
    data class Error(
        val message: String,
    ) : MovieDetailScreenState
}

internal data class MovieDetailUiState(
    val screenState: MovieDetailScreenState = MovieDetailScreenState.Loading,
    val movie: MovieUi? = null,
)

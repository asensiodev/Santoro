package com.asensiodev.feature.moviedetail.impl.presentation

import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi

internal data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movie: MovieUi? = null,
    val errorMessage: String? = null,
    val hasResults: Boolean = false,
)

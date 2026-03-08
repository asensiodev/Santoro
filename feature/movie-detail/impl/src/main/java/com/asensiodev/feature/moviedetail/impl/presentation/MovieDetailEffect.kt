package com.asensiodev.feature.moviedetail.impl.presentation

import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi

internal sealed interface MovieDetailEffect {
    data class ShareMovie(
        val movie: MovieUi,
    ) : MovieDetailEffect
    data object NavigateBack : MovieDetailEffect
    data class ShowError(
        val message: String,
    ) : MovieDetailEffect
}

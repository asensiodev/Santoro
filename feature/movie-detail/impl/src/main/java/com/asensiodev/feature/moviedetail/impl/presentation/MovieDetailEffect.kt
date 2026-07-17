package com.asensiodev.feature.moviedetail.impl.presentation

import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import com.asensiodev.ui.UiText

internal sealed interface MovieDetailEffect {
    data class ShareMovie(
        val movie: MovieUi,
    ) : MovieDetailEffect
    data class ShowError(
        val message: UiText,
    ) : MovieDetailEffect
}

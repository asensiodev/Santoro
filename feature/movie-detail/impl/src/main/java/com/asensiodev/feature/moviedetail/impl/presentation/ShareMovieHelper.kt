package com.asensiodev.feature.moviedetail.impl.presentation

import android.content.Context
import android.content.Intent
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.stringresources.R as SR

internal object ShareMovieHelper {
    fun share(
        context: Context,
        movie: MovieUi,
    ) {
        val shareText = buildShareText(movie, context)
        val chooserTitle = context.getString(SR.string.movie_detail_share_chooser_title)

        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE_TEXT
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_TITLE, chooserTitle)
            }

        context.startActivity(Intent.createChooser(intent, null))
    }

    internal fun buildShareText(
        movie: MovieUi,
        context: Context,
    ): String {
        val year = movie.releaseDate?.take(YEAR_LENGTH).orEmpty()
        return context.getString(
            SR.string.movie_detail_share_text,
            movie.title,
            year,
            movie.voteAverage,
            movie.id,
        )
    }
}

private const val YEAR_LENGTH = 4
private const val MIME_TYPE_TEXT = "text/plain"

package com.asensiodev.feature.searchmovies.impl.presentation.model

import androidx.annotation.StringRes
import com.asensiodev.santoro.core.stringresources.R

internal data class GenreUiModel(
    val id: Int,
    @StringRes val nameRes: Int,
)

internal object GenreConstants {
    private const val GENRE_ACTION = 28
    private const val GENRE_COMEDY = 35
    private const val GENRE_HORROR = 27
    private const val GENRE_ANIMATION = 16
    private const val GENRE_DOCUMENTARY = 99
    private const val GENRE_DRAMA = 18
    private const val GENRE_HISTORY = 36
    private const val GENRE_MUSIC = 10402
    private const val GENRE_MYSTERY = 9648
    private const val GENRE_SCIFI = 878
    private const val GENRE_THRILLER = 53
    private const val GENRE_WESTERN = 37

    val availableGenres =
        listOf(
            GenreUiModel(GENRE_ACTION, R.string.search_movies_action_title),
            GenreUiModel(GENRE_COMEDY, R.string.search_movies_comedy_title),
            GenreUiModel(GENRE_HORROR, R.string.search_movies_horror_title),
            GenreUiModel(GENRE_ANIMATION, R.string.search_movies_animation_title),
            GenreUiModel(GENRE_DOCUMENTARY, R.string.search_movies_documentary_title),
            GenreUiModel(GENRE_DRAMA, R.string.search_movies_drama_title),
            GenreUiModel(GENRE_HISTORY, R.string.search_movies_history_title),
            GenreUiModel(GENRE_MUSIC, R.string.search_movies_music_title),
            GenreUiModel(GENRE_MYSTERY, R.string.search_movies_mystery_title),
            GenreUiModel(GENRE_SCIFI, R.string.search_movies_scifi_title),
            GenreUiModel(GENRE_THRILLER, R.string.search_movies_thriller_title),
            GenreUiModel(GENRE_WESTERN, R.string.search_movies_western_title),
        )
}

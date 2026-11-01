package com.asensiodev.feature.watchedmovies.impl.presentation.mapper

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupUi

internal fun List<Movie>.toWatchedGroups(grouping: WatchedGrouping): List<WatchedGroupUi> =
    when (grouping) {
        WatchedGrouping.DATE_WATCHED -> groupByDateWatched()
        WatchedGrouping.DIRECTOR -> groupByDirector()
        WatchedGrouping.GENRE -> groupByGenre()
        WatchedGrouping.DECADE -> groupByDecade()
    }

private fun List<Movie>.groupByDateWatched(): List<WatchedGroupUi> =
    map { it.toUi() }
        .groupBy { it.watchedDate }
        .map { (watchedDate, movies) ->
            WatchedGroupUi(
                key = "$DATE_KEY${watchedDate.orEmpty()}",
                label =
                    watchedDate?.let {
                        WatchedGroupLabel.Text(
                            it,
                        )
                    } ?: WatchedGroupLabel.UnknownDate,
                movies = movies,
            )
        }

private fun List<Movie>.groupByDirector(): List<WatchedGroupUi> {
    val (unknown, known) = partition { it.directors.isNullOrEmpty() }
    val directors =
        known
            .flatMap { movie ->
                movie.directors
                    .orEmpty()
                    .distinctBy { it.id }
                    .map { it to movie }
            }.groupBy({ it.first.id }, { it })
            .map { (id, pairs) ->
                WatchedGroupUi(
                    key = "$DIRECTOR_KEY$id",
                    label = WatchedGroupLabel.Text(pairs.first().first.name),
                    movies = pairs.map { it.second }.toSortedUi(),
                    directorId = id,
                )
            }.sortedByCountThenLabel()
    return directors +
        unknown.toFallbackGroup(UNKNOWN_DIRECTOR_KEY, WatchedGroupLabel.UnknownDirector)
}

private fun List<Movie>.groupByGenre(): List<WatchedGroupUi> {
    val (untagged, tagged) = partition { it.genres.isEmpty() }
    val genres =
        tagged
            .flatMap { movie -> movie.genres.distinctBy { it.id }.map { it to movie } }
            .groupBy({ it.first.id }, { it })
            .map { (id, pairs) ->
                WatchedGroupUi(
                    key = "$GENRE_KEY$id",
                    label = WatchedGroupLabel.Text(pairs.first().first.name),
                    movies = pairs.map { it.second }.toSortedUi(),
                )
            }.sortedByCountThenLabel()
    return genres + untagged.toFallbackGroup(NO_GENRE_KEY, WatchedGroupLabel.NoGenre)
}

private fun List<Movie>.groupByDecade(): List<WatchedGroupUi> {
    val undated = filter { it.releaseYear() == null }
    val decades =
        mapNotNull { movie -> movie.releaseYear()?.let { year -> year / DECADE * DECADE to movie } }
            .groupBy({ it.first }, { it.second })
            .toSortedMap(compareByDescending { it })
            .map { (decade, movies) ->
                WatchedGroupUi(
                    key = "$DECADE_KEY$decade",
                    label = WatchedGroupLabel.Decade(decade),
                    movies = movies.toSortedUi(),
                )
            }
    return decades + undated.toFallbackGroup(UNDATED_KEY, WatchedGroupLabel.Undated)
}

private fun List<Movie>.toFallbackGroup(
    key: String,
    label: WatchedGroupLabel,
): List<WatchedGroupUi> =
    if (isEmpty()) {
        emptyList()
    } else {
        listOf(
            WatchedGroupUi(key = key, label = label, movies = toSortedUi()),
        )
    }

private fun List<Movie>.toSortedUi() =
    sortedWith(
        compareBy<Movie, String?>(nullsLast()) { it.sortableReleaseDate() }
            .thenBy { it.title },
    ).map { it.toUi() }

private fun List<WatchedGroupUi>.sortedByCountThenLabel() =
    sortedWith(
        compareByDescending<WatchedGroupUi> { it.movies.size }
            .thenBy { (it.label as? WatchedGroupLabel.Text)?.value.orEmpty() },
    )

internal fun Movie.releaseYear(): Int? = releaseDate?.take(YEAR_LENGTH)?.toIntOrNull()

private fun Movie.sortableReleaseDate(): String? = releaseDate?.takeIf { releaseYear() != null }

private const val DECADE = 10
private const val YEAR_LENGTH = 4
private const val DATE_KEY = "date:"
private const val DIRECTOR_KEY = "director:"
private const val GENRE_KEY = "genre:"
private const val DECADE_KEY = "decade:"
private const val UNKNOWN_DIRECTOR_KEY = "director:unknown"
private const val NO_GENRE_KEY = "genre:none"
private const val UNDATED_KEY = "decade:undated"

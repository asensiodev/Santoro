package com.asensiodev.feature.watchedmovies.impl.presentation.mapper

import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.WatchedGrouping
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupLabel
import com.asensiodev.feature.watchedmovies.impl.presentation.model.WatchedGroupUi
import java.text.Collator
import java.text.Normalizer
import java.util.Locale

internal fun List<Movie>.toWatchedGroups(grouping: WatchedGrouping): List<WatchedGroupUi> =
    when (grouping) {
        WatchedGrouping.RECENT -> groupByRecent()
        WatchedGrouping.ALPHABETICAL -> groupAlphabetically()
        WatchedGrouping.DIRECTOR -> groupByDirector()
        WatchedGrouping.GENRE -> groupByGenre()
        WatchedGrouping.DECADE -> groupByDecade()
    }

private fun List<Movie>.groupByRecent(): List<WatchedGroupUi> =
    if (isEmpty()) {
        emptyList()
    } else {
        listOf(WatchedGroupUi(key = RECENT_KEY, label = null, movies = map { it.toUi() }))
    }

private fun List<Movie>.groupAlphabetically(): List<WatchedGroupUi> {
    val byInitial = groupBy { it.titleInitial() }
    val letters =
        byInitial.entries
            .mapNotNull { (initial, movies) -> initial?.let { it to movies } }
            .sortedBy { it.first }
            .map { (letter, movies) ->
                WatchedGroupUi(
                    key = "$ALPHABET_KEY$letter",
                    label = WatchedGroupLabel.Text(letter.toString()),
                    movies = movies.toAlphabeticalUi(),
                )
            }
    val otherTitles = byInitial[null].orEmpty()
    return if (otherTitles.isEmpty()) {
        letters
    } else {
        letters +
            WatchedGroupUi(
                key = OTHER_TITLES_KEY,
                label = WatchedGroupLabel.OtherTitles,
                movies = otherTitles.toAlphabeticalUi(),
            )
    }
}

private fun Movie.titleInitial(): Char? =
    title
        .trimStart()
        .firstOrNull()
        ?.let { Normalizer.normalize(it.toString(), Normalizer.Form.NFD).first().uppercaseChar() }
        ?.takeIf { it in 'A'..'Z' }

private fun List<Movie>.toAlphabeticalUi(): List<MovieUi> {
    val collator = Collator.getInstance(Locale.ROOT).apply { strength = Collator.PRIMARY }
    return sortedWith(
        compareBy<Movie, String>(collator) { it.title.trim() }
            .thenBy(nullsLast()) { it.sortableReleaseDate() },
    ).map { it.toUi() }
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
                    isCollapsible = true,
                )
            }.sortedByCountThenLabel()
    return directors +
        unknown
            .toFallbackGroup(UNKNOWN_DIRECTOR_KEY, WatchedGroupLabel.UnknownDirector)
            .map { it.copy(isCollapsible = true) }
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
private const val RECENT_KEY = "recent"
private const val ALPHABET_KEY = "letter:"
private const val OTHER_TITLES_KEY = "letter:other"
private const val DIRECTOR_KEY = "director:"
private const val GENRE_KEY = "genre:"
private const val DECADE_KEY = "decade:"
private const val UNKNOWN_DIRECTOR_KEY = "director:unknown"
private const val NO_GENRE_KEY = "genre:none"
private const val UNDATED_KEY = "decade:undated"

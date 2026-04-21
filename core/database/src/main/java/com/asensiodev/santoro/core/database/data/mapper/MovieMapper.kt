package com.asensiodev.santoro.core.database.data.mapper

import com.asensiodev.core.domain.model.Genre
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.core.domain.model.ProductionCountry
import com.asensiodev.santoro.core.database.data.model.MovieEntity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

fun MovieEntity.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = null,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.toGenres(),
        productionCountries = productionCountries.toProductionCountries(),
        tagline = tagline,
        runtime = runtime,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
        updatedAt = updatedAt,
    )

fun String.toGenres(): List<Genre> {
    val gson = Gson()
    val parseAsStored =
        if (isBlank()) {
            emptyList()
        } else {
            runCatching {
                gson
                    .fromJson(this, Array<StoredGenre>::class.java)
                    ?.mapNotNull { storedGenre -> storedGenre.toDomain() }
                    .orEmpty()
            }.getOrDefault(emptyList())
        }

    val parseAsLegacy =
        runCatching {
            gson
                .fromJson(this, Array<Genre>::class.java)
                ?.mapNotNull { genre -> genre.toStoredGenre()?.toDomain() }
                .orEmpty()
        }.getOrDefault(emptyList())

    return parseAsStored.ifEmpty { parseAsLegacy }
}

fun String.toProductionCountries(): List<ProductionCountry> {
    if (isBlank()) return emptyList()
    val gson = Gson()
    return gson.fromJson(this, Array<ProductionCountry>::class.java)?.toList().orEmpty()
}

fun Movie.toEntity(): MovieEntity {
    val gson = Gson()
    return MovieEntity(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        releaseDate = releaseDate,
        popularity = popularity,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = gson.toJson(genres.mapNotNull { genre -> genre.toStoredGenre() }),
        productionCountries = gson.toJson(productionCountries),
        tagline = tagline,
        runtime = runtime,
        isWatched = isWatched,
        isInWatchlist = isInWatchlist,
        watchedAt = watchedAt,
        updatedAt = System.currentTimeMillis(),
    )
}

private data class StoredGenre(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
)

private fun StoredGenre.toDomain(): Genre? =
    id?.let { safeId ->
        name.toValidGenreName()?.let { safeName ->
            Genre(id = safeId, name = safeName)
        }
    }

private fun Genre.toStoredGenre(): StoredGenre? {
    val safeName = runCatching { name }.getOrNull().toValidGenreName() ?: return null
    return StoredGenre(id = id, name = safeName)
}

private fun String?.toValidGenreName(): String? =
    this?.takeUnless { genreName ->
        genreName.isBlank() || genreName.equals(NULL_LITERAL, ignoreCase = true)
    }

private const val NULL_LITERAL = "null"

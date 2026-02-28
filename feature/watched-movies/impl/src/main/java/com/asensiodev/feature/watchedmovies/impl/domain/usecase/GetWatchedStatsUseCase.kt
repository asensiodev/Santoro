package com.asensiodev.feature.watchedmovies.impl.domain.usecase

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.feature.watchedmovies.impl.domain.model.WatchedStats
import com.asensiodev.santoro.core.database.domain.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

internal class GetWatchedStatsUseCase
    @Inject
    constructor(
        private val repository: DatabaseRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke(): Flow<WatchedStats> =
            repository
                .getWatchedMovies()
                .map { result ->
                    when (result) {
                        is Result.Success -> {
                            val movies = result.data
                            val totalWatched = movies.size
                            val totalRuntimeMinutes = movies.sumOf { it.runtime ?: 0 }
                            val totalRuntimeHours = totalRuntimeMinutes / MINUTES_PER_HOUR
                            val favouriteGenre =
                                movies
                                    .flatMap { it.genres }
                                    .groupingBy { it.name }
                                    .eachCount()
                                    .maxByOrNull { it.value }
                                    ?.key
                            val longestStreakWeeks =
                                computeLongestStreakWeeks(
                                    movies.mapNotNull { it.watchedAt },
                                )
                            WatchedStats(
                                totalWatched = totalWatched,
                                totalRuntimeHours = totalRuntimeHours,
                                favouriteGenre = favouriteGenre,
                                longestStreakWeeks = longestStreakWeeks,
                            )
                        }

                        is Result.Error -> {
                            WatchedStats(
                                totalWatched = 0,
                                totalRuntimeHours = 0,
                                favouriteGenre = null,
                                longestStreakWeeks = 0,
                            )
                        }
                    }
                }.flowOn(dispatchers.io)

        private fun computeLongestStreakWeeks(timestamps: List<Long>): Int {
            if (timestamps.isEmpty()) return 0
            val isoWeekFields = WeekFields.of(Locale.getDefault())
            val weeks =
                timestamps
                    .map { timestamp ->
                        val date =
                            Instant
                                .ofEpochMilli(
                                    timestamp,
                                ).atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        val weekYear = date.get(isoWeekFields.weekBasedYear())
                        val weekOfYear = date.get(isoWeekFields.weekOfWeekBasedYear())
                        weekYear * WEEKS_PER_YEAR + weekOfYear
                    }.toSortedSet()

            var longest = 1
            var current = 1
            val iterator = weeks.iterator()
            var previous = iterator.next()

            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next == previous + 1) {
                    current++
                    if (current > longest) longest = current
                } else {
                    current = 1
                }
                previous = next
            }
            return longest
        }
    }

private const val MINUTES_PER_HOUR = 60
private const val WEEKS_PER_YEAR = 53

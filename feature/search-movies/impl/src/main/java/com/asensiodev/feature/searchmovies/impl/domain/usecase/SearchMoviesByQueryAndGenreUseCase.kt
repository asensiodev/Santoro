package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class SearchMoviesByQueryAndGenreUseCase
    @Inject
    constructor(
        private val repository: SearchMoviesRepository,
        private val dispatchers: DispatcherProvider,
    ) {
        operator fun invoke(
            query: String,
            genreId: Int,
            page: Int,
        ): Flow<Result<List<Movie>>> =
            repository
                .searchMovies(query, page)
                .map { result ->
                    when (result) {
                        is Result.Success -> {
                            val filteredMovies =
                                result.data.filter { movie ->
                                    movie.genreIds.contains(genreId) ||
                                        movie.genres.any { it.id == genreId }
                                }
                            Result.Success(filteredMovies)
                        }

                        is Result.Error -> {
                            result
                        }
                    }
                }.flowOn(dispatchers.io)
    }

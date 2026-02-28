package com.asensiodev.feature.searchmovies.impl.data.repository

import com.asensiodev.core.domain.Result
import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.impl.data.datasource.BrowseCacheLocalDataSource
import com.asensiodev.feature.searchmovies.impl.data.datasource.SearchMoviesDatasource
import com.asensiodev.feature.searchmovies.impl.domain.repository.SearchMoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class CachingSearchMoviesRepository
    @Inject
    constructor(
        private val localDataSource: BrowseCacheLocalDataSource,
        private val remoteDatasource: SearchMoviesDatasource,
        private val dispatchers: DispatcherProvider,
    ) : SearchMoviesRepository {
        override fun searchMovies(
            query: String,
            page: Int,
        ): Flow<Result<List<Movie>>> =
            cachedFlow(
                section = BrowseSectionKeys.searchKey(query),
                page = page,
                ttlMs = BrowseCacheTtl.SEARCH_MS,
            ) { remoteDatasource.searchMovies(query, page) }

        override fun getNowPlayingMovies(page: Int): Flow<Result<List<Movie>>> =
            cachedFlow(
                section = BrowseSectionKeys.NOW_PLAYING,
                page = page,
                ttlMs = BrowseCacheTtl.CURATED_MS,
            ) { remoteDatasource.getNowPlayingMovies(page) }

        override fun getPopularMovies(page: Int): Flow<Result<List<Movie>>> =
            cachedFlow(
                section = BrowseSectionKeys.POPULAR,
                page = page,
                ttlMs = BrowseCacheTtl.CURATED_MS,
            ) { remoteDatasource.getPopularMovies(page) }

        override fun getTopRatedMovies(page: Int): Flow<Result<List<Movie>>> =
            cachedFlow(
                section = BrowseSectionKeys.TOP_RATED,
                page = page,
                ttlMs = BrowseCacheTtl.CURATED_MS,
            ) { remoteDatasource.getTopRatedMovies(page) }

        override fun getUpcomingMovies(page: Int): Flow<Result<List<Movie>>> =
            cachedFlow(
                section = BrowseSectionKeys.UPCOMING,
                page = page,
                ttlMs = BrowseCacheTtl.CURATED_MS,
            ) { remoteDatasource.getUpcomingMovies(page) }

        override fun getTrendingMovies(page: Int): Flow<Result<List<Movie>>> =
            cachedFlow(
                section = BrowseSectionKeys.TRENDING,
                page = page,
                ttlMs = BrowseCacheTtl.CURATED_MS,
            ) { remoteDatasource.getTrendingMovies(page) }

        override fun getMoviesByGenre(
            genreId: Int,
            page: Int,
        ): Flow<Result<List<Movie>>> =
            flow {
                withContext(dispatchers.io) {
                    remoteDatasource.getMoviesByGenre(genreId, page)
                }.let { result -> emit(result) }
            }

        private fun cachedFlow(
            section: String,
            page: Int,
            ttlMs: Long,
            remoteFetch: suspend () -> Result<List<Movie>>,
        ): Flow<Result<List<Movie>>> =
            flow {
                val cached =
                    withContext(dispatchers.io) { localDataSource.getCachedPage(section, page) }
                val now = System.currentTimeMillis()

                if (cached != null && now - cached.cachedAt < ttlMs) {
                    emit(Result.Success(cached.movies))
                    return@flow
                }

                try {
                    val result = withContext(dispatchers.io) { remoteFetch() }
                    if (result is Result.Success) {
                        withContext(dispatchers.io) {
                            localDataSource.savePage(
                                section,
                                page,
                                result.data,
                                System.currentTimeMillis(),
                            )
                        }
                        emit(Result.Success(result.data))
                    } else if (result is Result.Error) {
                        if (cached != null) {
                            emit(Result.Success(cached.movies))
                            emit(Result.Error(StaleDataException()))
                        } else {
                            emit(result)
                        }
                    }
                } catch (e: IOException) {
                    if (cached != null) {
                        emit(Result.Success(cached.movies))
                        emit(Result.Error(StaleDataException()))
                    } else {
                        emit(Result.Error(e))
                    }
                } catch (e: HttpException) {
                    if (cached != null) {
                        emit(Result.Success(cached.movies))
                        emit(Result.Error(StaleDataException()))
                    } else {
                        emit(Result.Error(e))
                    }
                }
            }

        suspend fun clearStaleEntries() {
            withContext(dispatchers.io) {
                localDataSource.clearStaleEntries(
                    System.currentTimeMillis() - BrowseCacheTtl.CURATED_MS,
                )
            }
        }

        suspend fun clearAllSections() {
            withContext(dispatchers.io) {
                listOf(
                    BrowseSectionKeys.NOW_PLAYING,
                    BrowseSectionKeys.POPULAR,
                    BrowseSectionKeys.TOP_RATED,
                    BrowseSectionKeys.UPCOMING,
                    BrowseSectionKeys.TRENDING,
                ).forEach { section -> localDataSource.clearSection(section) }
            }
        }
    }

package com.asensiodev.feature.searchmovies.impl.presentation.seeall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.api.navigation.SeeAllMoviesRoute
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTrendingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetUpcomingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.presentation.mapper.toUiList
import com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SeeAllMoviesViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
        private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
        private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCase,
        private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
    ) : ViewModel() {
        private val sectionType: SectionType =
            SectionType.fromKey(
                savedStateHandle.toRoute<SeeAllMoviesRoute>().sectionType,
            )

        private val _uiState = MutableStateFlow(SeeAllMoviesUiState(sectionType = sectionType))
        val uiState: StateFlow<SeeAllMoviesUiState> = _uiState.asStateFlow()

        private val _effect = Channel<SeeAllMoviesEffect>(Channel.BUFFERED)
        val effect: Flow<SeeAllMoviesEffect> = _effect.receiveAsFlow()

        private var currentPage = FIRST_PAGE

        fun process(intent: SeeAllMoviesIntent) {
            when (intent) {
                is SeeAllMoviesIntent.LoadInitial -> loadInitial()
                is SeeAllMoviesIntent.LoadMore -> loadMore()
                is SeeAllMoviesIntent.MovieClicked -> onMovieClicked(intent.movieId)
                is SeeAllMoviesIntent.Retry -> retry()
            }
        }

        private fun loadInitial() {
            currentPage = FIRST_PAGE
            _uiState.update {
                it.copy(
                    screenState = SeeAllScreenState.Loading,
                    movies = emptyList(),
                    isEndReached = false,
                )
            }
            loadPage(FIRST_PAGE, isInitialLoad = true)
        }

        private fun loadMore() {
            if (_uiState.value.isLoadingMore || _uiState.value.isEndReached) return
            loadPage(currentPage + NEXT_PAGE, isInitialLoad = false)
        }

        private fun retry() {
            loadInitial()
        }

        private fun onMovieClicked(movieId: Int) {
            _effect.trySend(SeeAllMoviesEffect.NavigateToDetail(movieId))
        }

        private fun loadPage(
            page: Int,
            isInitialLoad: Boolean,
        ) {
            _uiState.update { it.copy(isLoadingMore = !isInitialLoad) }

            viewModelScope.launch {
                getMoviesFlow(page).collect { result ->
                    result.fold(
                        onSuccess = { movies ->
                            val newMovies = movies.toUiList()
                            currentPage = page

                            _uiState.update { state ->
                                val updatedMovies =
                                    if (isInitialLoad) newMovies else state.movies + newMovies

                                state.copy(
                                    screenState =
                                        if (isInitialLoad && newMovies.isEmpty()) {
                                            SeeAllScreenState.Empty
                                        } else {
                                            SeeAllScreenState.Content
                                        },
                                    movies = updatedMovies,
                                    isLoadingMore = false,
                                    isEndReached = newMovies.isEmpty(),
                                )
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update { state ->
                                state.copy(
                                    screenState =
                                        if (isInitialLoad && state.movies.isEmpty()) {
                                            SeeAllScreenState.Error(
                                                exception.message.orEmpty(),
                                            )
                                        } else {
                                            state.screenState
                                        },
                                    isLoadingMore = false,
                                    isEndReached = true,
                                )
                            }
                        },
                    )
                }
            }
        }

        private fun getMoviesFlow(page: Int): Flow<Result<List<Movie>>> =
            when (sectionType) {
                SectionType.TRENDING -> getTrendingMoviesUseCase(page)
                SectionType.POPULAR -> getPopularMoviesUseCase(page)
                SectionType.TOP_RATED -> getTopRatedMoviesUseCase(page)
                SectionType.UPCOMING -> getUpcomingMoviesUseCase(page)
            }
    }

private const val FIRST_PAGE = 1
private const val NEXT_PAGE = 1

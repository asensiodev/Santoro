package com.asensiodev.feature.searchmovies.impl.presentation.seeall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.asensiodev.core.domain.model.Movie
import com.asensiodev.feature.searchmovies.api.navigation.SeeAllMoviesRoute
import com.asensiodev.feature.searchmovies.impl.data.repository.StaleDataException
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetPopularMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTopRatedMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetTrendingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.domain.usecase.GetUpcomingMoviesUseCase
import com.asensiodev.feature.searchmovies.impl.presentation.mapper.toUiList
import com.asensiodev.feature.searchmovies.impl.presentation.model.SectionType
import com.asensiodev.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.asensiodev.santoro.core.stringresources.R as SR

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

        private val _effect = MutableSharedFlow<SeeAllMoviesEffect>(extraBufferCapacity = 1)
        val effect: Flow<SeeAllMoviesEffect> = _effect.asSharedFlow()

        private var currentPage = FIRST_PAGE
        private var pageJob: Job? = null
        private var requestId = 0

        fun process(intent: SeeAllMoviesIntent) {
            when (intent) {
                is SeeAllMoviesIntent.LoadInitial -> loadInitial()
                is SeeAllMoviesIntent.LoadMore -> loadMore()
                is SeeAllMoviesIntent.MovieClicked -> onMovieClicked(intent.movieId)
                is SeeAllMoviesIntent.Retry -> retry()
            }
        }

        private fun loadInitial() {
            pageJob?.cancel()
            currentPage = FIRST_PAGE
            _uiState.update {
                it.copy(
                    screenState = SeeAllScreenState.Loading,
                    movies = emptyList(),
                    isEndReached = false,
                    isShowingStaleData = false,
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
            viewModelScope.launch {
                _effect.emit(SeeAllMoviesEffect.NavigateToDetail(movieId))
            }
        }

        private fun loadPage(
            page: Int,
            isInitialLoad: Boolean,
        ) {
            pageJob?.cancel()
            _uiState.update { it.copy(isLoadingMore = !isInitialLoad) }
            val activeRequestId = ++requestId

            pageJob =
                viewModelScope.launch {
                    getMoviesFlow(page).collect { result ->
                        if (activeRequestId != requestId) return@collect
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
                                        isShowingStaleData = false,
                                    )
                                }
                            },
                            onFailure = { exception ->
                                _uiState.update { state ->
                                    if (exception is StaleDataException) {
                                        return@update state.copy(
                                            isLoadingMore = false,
                                            isShowingStaleData = true,
                                        )
                                    }
                                    state.copy(
                                        screenState =
                                            if (isInitialLoad && state.movies.isEmpty()) {
                                                SeeAllScreenState.Error(
                                                    UiText.StringResource(
                                                        SR.string.error_message_retry,
                                                    ),
                                                )
                                            } else {
                                                state.screenState
                                            },
                                        isLoadingMore = false,
                                        isEndReached = true,
                                        isShowingStaleData = false,
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

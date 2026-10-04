package com.asensiodev.feature.persondetail.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.feature.persondetail.api.navigation.FilmographySection
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PersonFilmographyScreen(
    state: PersonDetailUiState,
    section: FilmographySection,
    onBackClicked: () -> Unit,
    onMovieClicked: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(section.title))
                        state.person?.let {
                            Text(
                                it.name,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(AppIcons.ArrowBack, stringResource(SR.string.navigate_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (state.listLoadState) {
                PersonLoadState.Loading -> LoadingIndicator()
                PersonLoadState.Error ->
                    ErrorContent(
                        stringResource(SR.string.person_filmography_error),
                        onRetry,
                    )
                PersonLoadState.Content -> {
                    val movies = state.movies(section)
                    if (movies.isEmpty()) {
                        Text(stringResource(SR.string.person_filmography_empty))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacings.spacing16),
                            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
                        ) {
                            item(key = "count", contentType = "heading") {
                                Text(
                                    pluralStringResource(
                                        SR.plurals.person_movie_count,
                                        movies.size,
                                        movies.size,
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                            state.moviesByYear(section).forEach { (year, yearMovies) ->
                                item(key = "year-$year", contentType = "heading") {
                                    Text(
                                        year
                                            ?: stringResource(
                                                SR.string.person_release_date_unknown,
                                            ),
                                        modifier =
                                            Modifier
                                                .padding(top = Spacings.spacing12)
                                                .semantics { heading() },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                items(
                                    yearMovies,
                                    key = { it.movieId },
                                    contentType = { "movie" },
                                ) { movie ->
                                    FilmographyMovie(movie, onMovieClicked)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PersonFilmographyScreenPreview() {
    PreviewContentFullSize {
        PersonFilmographyScreen(
            PersonDetailUiState(
                person = Person(1, "Person", "", null, null, null, null),
                profileState = PersonLoadState.Content,
                filmographyState = PersonLoadState.Content,
                filmography =
                    PersonFilmography(
                        listOf(PersonMovieCredit(1, "Movie", null, "2024-01-01", listOf("Lead"))),
                        emptyList(),
                    ),
            ),
            FilmographySection.ACTING,
            {},
            {},
            {},
        )
    }
}

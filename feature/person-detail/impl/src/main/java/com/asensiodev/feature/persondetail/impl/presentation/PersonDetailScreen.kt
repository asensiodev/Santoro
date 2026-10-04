package com.asensiodev.feature.persondetail.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.component.errorContent.ErrorContent
import com.asensiodev.core.designsystem.component.loadingIndicator.LoadingIndicator
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.core.designsystem.theme.Weights
import com.asensiodev.core.domain.model.Person
import com.asensiodev.core.domain.model.PersonFilmography
import com.asensiodev.core.domain.model.PersonMovieCredit
import com.asensiodev.feature.persondetail.api.navigation.FilmographySection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import com.asensiodev.santoro.core.stringresources.R as SR

private const val PREVIEW_MOVIES = 8
private const val BIOGRAPHY_LINES = 5
private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w185"

@Composable
internal fun PersonDetailRoute(
    personId: Int,
    onBackClicked: () -> Unit,
    onMovieClicked: (Int) -> Unit,
    onSeeAllClicked: (FilmographySection) -> Unit,
    viewModel: PersonDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.process(PersonDetailIntent.Initialize(personId))
    }
    PersonDetailScreen(
        state = state,
        onBackClicked = onBackClicked,
        onMovieClicked = onMovieClicked,
        onSeeAllClicked = onSeeAllClicked,
        onRetryProfile = { viewModel.process(PersonDetailIntent.RetryProfile) },
        onRetryFilmography = { viewModel.process(PersonDetailIntent.RetryFilmography) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PersonDetailScreen(
    state: PersonDetailUiState,
    onBackClicked: () -> Unit,
    onMovieClicked: (Int) -> Unit,
    onRetryProfile: () -> Unit,
    onRetryFilmography: () -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClicked: (FilmographySection) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.person?.name ?: stringResource(SR.string.person_detail_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            AppIcons.ArrowBack,
                            contentDescription = stringResource(SR.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state.profileState) {
                PersonLoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                }

                PersonLoadState.Error -> {
                    ErrorContent(stringResource(SR.string.person_detail_error), onRetryProfile)
                }

                PersonLoadState.Content -> {
                    state.person?.let { person ->
                        LazyColumn(
                            contentPadding = PaddingValues(Spacings.spacing16),
                            verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
                        ) {
                            item(key = "profile", contentType = "profile") { PersonHeader(person) }
                            item(
                                key = "biography",
                                contentType = "biography",
                            ) { PersonBiography(person.biography) }
                            item(key = "filmography", contentType = "heading") {
                                Text(
                                    stringResource(SR.string.person_filmography),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }
                            filmography(state, onMovieClicked, onRetryFilmography, onSeeAllClicked)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonHeader(person: Person) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacings.spacing16)) {
        PersonImage(
            path = person.profilePath,
            description = person.name,
            placeholder = AppIcons.Profile,
            modifier = Modifier.size(width = Size.size100, height = Size.size144),
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
            Text(person.name, style = MaterialTheme.typography.headlineMedium)
            person.birthday?.let {
                Text(
                    stringResource(SR.string.person_born, formatDate(it)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            person.deathday?.let {
                Text(
                    stringResource(SR.string.person_died, formatDate(it)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            person.birthplace?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun PersonBiography(biography: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var truncated by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            stringResource(SR.string.person_biography),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = biography.ifBlank { stringResource(SR.string.person_biography_empty) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else BIOGRAPHY_LINES,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) truncated = it.hasVisualOverflow },
        )
        if (expanded || truncated) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    stringResource(
                        if (expanded) {
                            SR.string.person_biography_less
                        } else {
                            SR.string.person_biography_more
                        },
                    ),
                )
            }
        }
    }
}

private fun LazyListScope.filmography(
    state: PersonDetailUiState,
    onMovieClicked: (Int) -> Unit,
    onRetry: () -> Unit,
    onSeeAll: (FilmographySection) -> Unit,
) {
    when (state.filmographyState) {
        PersonLoadState.Loading ->
            item(key = "credits-loading", contentType = "loading") {
                LoadingIndicator()
            }
        PersonLoadState.Error ->
            item(key = "credits-error", contentType = "error") {
                Column {
                    Text(
                        stringResource(SR.string.person_filmography_error),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(
                        onClick = onRetry,
                    ) { Text(stringResource(SR.string.error_content_button)) }
                }
            }

        PersonLoadState.Content -> {
            if (state.filmography.acting.isEmpty() && state.filmography.crew.isEmpty()) {
                item(key = "credits-empty", contentType = "empty") {
                    Text(
                        stringResource(SR.string.person_filmography_empty),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            state.filmographySections.forEach { section ->
                movieCredits(state.movies(section), section, onMovieClicked, onSeeAll)
            }
        }
    }
}

private fun LazyListScope.movieCredits(
    movies: List<PersonMovieCredit>,
    section: FilmographySection,
    onClick: (Int) -> Unit,
    onSeeAll: (FilmographySection) -> Unit,
) {
    if (movies.isEmpty()) return
    item(key = section.name, contentType = "section") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing12)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(Weights.W10)) {
                    Text(stringResource(section.title), style = MaterialTheme.typography.titleLarge)
                    Text(
                        pluralStringResource(
                            SR.plurals.person_movie_count,
                            movies.size,
                            movies.size,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (movies.size > PREVIEW_MOVIES) {
                    TextButton(onClick = { onSeeAll(section) }) {
                        Text(stringResource(SR.string.browse_see_all))
                    }
                }
            }
            LazyRow(
                modifier = Modifier.testTag("filmography-${section.name}"),
                horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
            ) {
                items(
                    movies.take(PREVIEW_MOVIES),
                    key = { it.movieId },
                    contentType = { "poster" },
                ) { movie ->
                    FilmographyPoster(movie, onClick)
                }
            }
        }
    }
}

@Composable
private fun FilmographyPoster(
    movie: PersonMovieCredit,
    onClick: (Int) -> Unit,
) {
    Card(onClick = { onClick(movie.movieId) }, modifier = Modifier.width(Size.size128)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
            PersonImage(
                movie.posterPath,
                null,
                AppIcons.Director,
                Modifier.size(width = Size.size128, height = Size.size180),
            )
            Column(
                modifier = Modifier.padding(Spacings.spacing8),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing4),
            ) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    movie.releaseDate?.substringBefore('-').orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    movie.roles.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun FilmographyMovie(
    movie: PersonMovieCredit,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = {
        onClick(movie.movieId)
    }, modifier = modifier.fillMaxWidth().heightIn(min = Size.size48)) {
        Row(
            modifier = Modifier.padding(Spacings.spacing12),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PersonImage(
                path = movie.posterPath,
                description = null,
                placeholder = AppIcons.Director,
                modifier = Modifier.size(width = Size.size64, height = Size.size96),
            )
            Column(
                modifier = Modifier.weight(Weights.W10),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing4),
            ) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                if (movie.roles.isNotEmpty()) {
                    Text(
                        movie.roles.joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonImage(
    path: String?,
    description: String?,
    placeholder: ImageVector,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = path?.let { IMAGE_BASE_URL + it },
        contentDescription = description,
        contentScale = ContentScale.Crop,
        modifier =
            modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        loading = { ImagePlaceholder(placeholder) },
        error = { ImagePlaceholder(placeholder) },
        success = { SubcomposeAsyncImageContent() },
    )
}

@Composable
private fun ImagePlaceholder(icon: ImageVector) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Size.size48),
        )
    }
}

private fun formatDate(date: String): String =
    LocalDate
        .parse(
            date,
        ).format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()),
        )

@PreviewLightDark
@Composable
private fun PersonDetailScreenPreview() {
    PreviewContentFullSize {
        PersonDetailScreen(
            state =
                PersonDetailUiState(
                    person = Person(1, "Person", "Biography", null, null, null, null),
                    profileState = PersonLoadState.Content,
                    filmographyState = PersonLoadState.Content,
                    filmography = PersonFilmography(emptyList(), emptyList()),
                ),
            onBackClicked = {},
            onMovieClicked = {},
            onRetryProfile = {},
            onRetryFilmography = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun FilmographyMoviePreview() {
    PreviewContentFullSize {
        FilmographyMovie(PersonMovieCredit(1, "Movie", null, "2024-01-01", listOf("Lead")), {})
    }
}

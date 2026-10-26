package com.asensiodev.feature.watchlist.impl.presentation.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.PreviewContentFullSize
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.core.designsystem.theme.Spacings
import com.asensiodev.feature.watchlist.impl.presentation.model.MovieUi
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
internal fun WatchlistSurpriseContent(
    movie: MovieUi,
    hasAlternatives: Boolean,
    onViewDetails: () -> Unit,
    onPickAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacings.spacing16)
                .padding(bottom = Spacings.spacing24),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Icon(
                imageVector = AppIcons.Casino,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(SR.string.watchlist_surprise_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Crossfade(targetState = movie, label = "watchlist_suggestion") { suggestion ->
            WatchlistMovieItem(movie = suggestion, onClick = onViewDetails)
        }
        Button(
            onClick = onViewDetails,
            modifier = Modifier.fillMaxWidth().heightIn(min = Size.size48),
        ) {
            Text(text = stringResource(SR.string.watchlist_surprise_details))
        }
        if (hasAlternatives) {
            TextButton(
                onClick = onPickAnother,
                modifier = Modifier.fillMaxWidth().heightIn(min = Size.size48),
            ) {
                Text(text = stringResource(SR.string.watchlist_surprise_another))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun WatchlistSurpriseContentPreview() {
    PreviewContentFullSize {
        WatchlistSurpriseContent(
            movie = MovieUi(1, "Arrival", null, "2016", "Science Fiction", PREVIEW_MOVIE_RATING),
            hasAlternatives = true,
            onViewDetails = {},
            onPickAnother = {},
        )
    }
}

private const val PREVIEW_MOVIE_RATING = 7.9

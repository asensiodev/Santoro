package com.asensiodev.feature.searchmovies.impl.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.test.core.app.ApplicationProvider
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus
import com.asensiodev.feature.searchmovies.impl.presentation.model.MovieUi
import org.junit.Rule
import org.junit.Test
import com.asensiodev.santoro.core.stringresources.R as SR

class MovieCardStatusSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noneStatusHasNoStatusDescription() {
        setContent(status = null)
        val watchedDescription = statusDescription(SR.string.watched_icon_button_marked)
        val watchlistDescription = statusDescription(SR.string.watchlist_icon_button_added)

        composeRule
            .onAllNodesWithContentDescription(watchedDescription)
            .assertCountEquals(0)
        composeRule
            .onAllNodesWithContentDescription(watchlistDescription)
            .assertCountEquals(0)
    }

    @Test
    fun watchedStatusHasExactlyOneWatchedDescription() {
        setContent(status = MovieLibraryStatus.Watched)
        val watchedDescription = statusDescription(SR.string.watched_icon_button_marked)
        val watchlistDescription = statusDescription(SR.string.watchlist_icon_button_added)

        composeRule
            .onAllNodesWithContentDescription(watchedDescription)
            .assertCountEquals(1)
        composeRule
            .onAllNodesWithContentDescription(watchlistDescription)
            .assertCountEquals(0)
    }

    @Test
    fun watchlistStatusHasExactlyOneWatchlistDescription() {
        setContent(status = MovieLibraryStatus.Watchlist)
        val watchedDescription = statusDescription(SR.string.watched_icon_button_marked)
        val watchlistDescription = statusDescription(SR.string.watchlist_icon_button_added)

        composeRule
            .onAllNodesWithContentDescription(watchedDescription)
            .assertCountEquals(0)
        composeRule
            .onAllNodesWithContentDescription(watchlistDescription)
            .assertCountEquals(1)
    }

    private fun setContent(status: MovieLibraryStatus?) {
        composeRule.setContent {
            SantoroTheme {
                MovieCard(
                    movie =
                        MovieUi(
                            id = 1,
                            title = "Movie",
                            posterPath = null,
                            backdropPath = null,
                            voteAverage = 0.0,
                            libraryStatus = status,
                        ),
                    onClick = {},
                    modifier = Modifier.size(width = Size.size120, height = Size.size180),
                )
            }
        }
    }

    private fun statusDescription(resourceId: Int): String =
        ApplicationProvider.getApplicationContext<android.content.Context>().getString(resourceId)
}

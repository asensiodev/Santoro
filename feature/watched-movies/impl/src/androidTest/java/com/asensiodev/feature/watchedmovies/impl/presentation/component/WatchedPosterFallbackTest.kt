package com.asensiodev.feature.watchedmovies.impl.presentation.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.EventListener
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.feature.watchedmovies.impl.domain.model.DirectorFilm
import com.asensiodev.feature.watchedmovies.impl.presentation.director.DirectorFilmCard
import com.asensiodev.feature.watchedmovies.impl.presentation.model.MovieUi
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(DelicateCoilApi::class)
@RunWith(AndroidJUnit4::class)
class WatchedPosterFallbackTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var previousImageLoader: ImageLoader
    private lateinit var imageLoader: ImageLoader
    private val failed = CompletableDeferred<Unit>()

    @Before
    fun setUp() {
        previousImageLoader = SingletonImageLoader.get(composeRule.activity)
        imageLoader =
            ImageLoader
                .Builder(composeRule.activity)
                .components {
                    add(
                        Interceptor { chain ->
                            ErrorResult(null, chain.request, IOException("Offline"))
                        },
                    )
                }.eventListener(
                    object : EventListener() {
                        override fun onError(
                            request: ImageRequest,
                            result: ErrorResult,
                        ) {
                            failed.complete(Unit)
                        }
                    },
                ).build()
        SingletonImageLoader.setUnsafe(imageLoader)
    }

    @After
    fun tearDown() {
        SingletonImageLoader.setUnsafe(previousImageLoader)
        imageLoader.shutdown()
    }

    @Test
    fun givenShelfPosterFailsToLoad_whenShown_thenPlaceholderShowsTheTitle() {
        composeRule.setContent {
            SantoroTheme {
                MovieCard(
                    movie = MovieUi(1, TITLE, POSTER_URL, "March 2026"),
                    onClick = {},
                    modifier = Modifier.width(120.dp).aspectRatio(2f / 3f),
                )
            }
        }

        composeRule.waitUntil { failed.isCompleted }

        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
    }

    @Test
    fun givenUnwatchedDirectorFilmPosterFailsToLoad_whenShown_thenPlaceholderShowsTheTitle() {
        composeRule.setContent {
            SantoroTheme {
                DirectorFilmCard(
                    film = DirectorFilm(1, TITLE, "/poster.jpg", "2021-04-07", isWatched = false),
                    onClick = {},
                    modifier = Modifier.width(120.dp).aspectRatio(2f / 3f),
                )
            }
        }

        composeRule.waitUntil { failed.isCompleted }

        composeRule.onNodeWithText(TITLE).assertIsDisplayed()
    }

    private companion object {
        const val TITLE = "Mortal Kombat"
        const val POSTER_URL = "https://image.tmdb.org/t/p/w500/poster.jpg"
    }
}

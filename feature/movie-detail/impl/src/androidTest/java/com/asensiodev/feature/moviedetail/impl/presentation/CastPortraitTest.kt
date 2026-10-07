package com.asensiodev.feature.moviedetail.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import coil3.ColorImage
import coil3.EventListener
import coil3.Image
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.designsystem.theme.Size
import com.asensiodev.feature.moviedetail.impl.presentation.model.CastMemberUi
import com.asensiodev.feature.moviedetail.impl.presentation.model.MovieUi
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.IOException

@OptIn(DelicateCoilApi::class)
@RunWith(Parameterized::class)
class CastPortraitTest(
    private val dark: Boolean,
) {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var previousImageLoader: ImageLoader
    private lateinit var imageLoader: ImageLoader
    private val requested = CompletableDeferred<Unit>()
    private val response = CompletableDeferred<Image?>()
    private val completed = CompletableDeferred<Unit>()

    @Before
    fun setUp() {
        previousImageLoader = SingletonImageLoader.get(composeRule.activity)
        imageLoader =
            ImageLoader
                .Builder(composeRule.activity)
                .components {
                    add(
                        Interceptor { chain ->
                            if (chain.request.data == MOVIE_POSTER) {
                                return@Interceptor SuccessResult(
                                    ColorImage(Color.Gray.toArgb()),
                                    chain.request,
                                )
                            }
                            requested.complete(Unit)
                            val image = response.await()
                            if (image == null) {
                                ErrorResult(
                                    null,
                                    chain.request,
                                    IOException("Portrait unavailable"),
                                )
                            } else {
                                SuccessResult(image, chain.request)
                            }
                        },
                    )
                }.eventListener(
                    object : EventListener() {
                        override fun onError(
                            request: ImageRequest,
                            result: ErrorResult,
                        ) {
                            if (request.data != MOVIE_POSTER) completed.complete(Unit)
                        }

                        override fun onSuccess(
                            request: ImageRequest,
                            result: SuccessResult,
                        ) {
                            if (request.data != MOVIE_POSTER) completed.complete(Unit)
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
    fun loadingPortraitShowsProfileIcon() {
        showCast()
        composeRule.waitUntil { requested.isCompleted }
        assertProfileIcon()
    }

    @Test
    fun failedPortraitKeepsProfileIcon() {
        showCast()
        composeRule.waitUntil { requested.isCompleted }
        response.complete(null)
        composeRule.waitUntil { completed.isCompleted }
        assertProfileIcon()
    }

    @Test
    fun missingPortraitShowsProfileIcon() {
        showCast(profileUrl = null)
        composeRule.waitUntil { completed.isCompleted }
        assertProfileIcon()
    }

    @Test
    fun loadedPortraitReplacesProfileIcon() {
        showCast()
        composeRule.waitUntil { requested.isCompleted }
        assertProfileIcon()
        response.complete(ColorImage(Color.Red.toArgb()))
        composeRule.waitUntil { completed.isCompleted }
        val image =
            composeRule
                .onNodeWithContentDescription(
                    ACTOR_NAME,
                    useUnmergedTree = true,
                ).captureToImage()
                .toPixelMap()
        val center = image.width / 2
        assertEquals(Color.Red, image[center, image.height / 4])
        assertEquals(Color.Red, image[center, image.height / 2])
        assertEquals(Color.Red, image[center, image.height * 3 / 4])
    }

    private fun showCast(profileUrl: String? = PORTRAIT_URL) {
        val movie =
            MovieUi(
                id = 1,
                title = "The Long Journey",
                overview = "A journey across a changing world.",
                posterPath = MOVIE_POSTER,
                releaseDate = "2024-03-15",
                popularity = 10.0,
                voteAverage = 8.5,
                voteCount = 100,
                genres = emptyList(),
                productionCountries = emptyList(),
                cast = listOf(CastMemberUi(2, "credit-2", ACTOR_NAME, "Traveller", profileUrl)),
                runtime = "1h 45m",
                director = null,
                isWatched = false,
                isInWatchlist = false,
            )
        composeRule.setContent {
            SantoroTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column {
                        MovieDetailContent(
                            uiState =
                                MovieDetailUiState(
                                    screenState = MovieDetailScreenState.Content,
                                    movie = movie,
                                ),
                            onToggleWatchlist = {},
                            onToggleWatched = {},
                            onDismissTooltip = {},
                            modifier = Modifier.weight(1f),
                            scrollState = rememberScrollState(),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .size(Size.size64)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("expected-profile-icon"),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = AppIcons.Profile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Size.size48),
                            )
                        }
                    }
                }
            }
        }
        composeRule
            .onNodeWithContentDescription(
                ACTOR_NAME,
                useUnmergedTree = true,
            ).performScrollTo()
            .assertIsDisplayed()
    }

    private fun assertProfileIcon() {
        val actual =
            composeRule
                .onNodeWithContentDescription(
                    ACTOR_NAME,
                    useUnmergedTree = true,
                ).captureToImage()
                .toPixelMap()
        val expected =
            composeRule
                .onNodeWithTag(
                    "expected-profile-icon",
                ).captureToImage()
                .toPixelMap()
        assertEquals(expected.width, actual.width)
        assertEquals(expected.height, actual.height)
        val center = expected.width / 2f
        val radius = center - 2f
        var differentPixels = 0
        for (y in 0 until expected.height) {
            for (x in 0 until expected.width) {
                val dx = x + 0.5f - center
                val dy = y + 0.5f - center
                if (dx * dx + dy * dy < radius * radius &&
                    actual[x, y] != expected[x, y]
                ) {
                    differentPixels++
                }
            }
        }
        assertTrue(
            "Portrait must display the profile icon; $differentPixels pixels differ",
            differentPixels == 0,
        )
    }

    companion object {
        private const val ACTOR_NAME = "Alex Morgan"
        private const val PORTRAIT_URL = "https://example.test/portrait.jpg"
        private const val MOVIE_POSTER = "https://example.test/poster.jpg"

        @JvmStatic
        @Parameterized.Parameters(name = "dark={0}")
        fun themes(): List<Array<Boolean>> = listOf(arrayOf(false), arrayOf(true))
    }
}

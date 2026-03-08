package com.asensiodev.feature.searchmovies.impl.presentation.component

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.asensiodev.core.designsystem.PreviewContent
import org.junit.Rule
import org.junit.Test

class SearchSuggestionsContentScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_3,
            theme = "Theme.Santoro",
            renderingMode = SessionParams.RenderingMode.SHRINK,
            showSystemUi = false,
        )

    @Test
    fun captureWithRecentsAndTrending() {
        paparazzi.snapshot {
            PreviewContent {
                SearchSuggestionsContent(
                    recentSearches = listOf("Inception", "Avatar", "Dune"),
                    trendingSuggestions = listOf("Oppenheimer", "Barbie", "Killers of the Flower Moon", "Poor Things"),
                    onSuggestionTap = {},
                    onClearRecents = {},
                )
            }
        }
    }

    @Test
    fun captureWithTrendingOnly() {
        paparazzi.snapshot {
            PreviewContent {
                SearchSuggestionsContent(
                    recentSearches = emptyList(),
                    trendingSuggestions = listOf("Oppenheimer", "Barbie", "Killers of the Flower Moon"),
                    onSuggestionTap = {},
                    onClearRecents = {},
                )
            }
        }
    }
}

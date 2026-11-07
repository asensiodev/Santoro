package com.asensiodev.feature.watchedmovies.impl.presentation.director

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

class DirectorCollectionUiStateTest {
    @Test
    fun `GIVEN a partial collection WHEN reading the remaining count THEN it is the unwatched total`() {
        DirectorCollectionUiState(watchedCount = 4, totalCount = 13).remainingCount shouldBeEqualTo 9
    }

    @Test
    fun `GIVEN a complete collection WHEN reading the remaining count THEN it is zero`() {
        DirectorCollectionUiState(watchedCount = 13, totalCount = 13).remainingCount shouldBeEqualTo 0
    }

    @Test
    fun `GIVEN more watched films than the total WHEN reading the remaining count THEN it is never negative`() {
        DirectorCollectionUiState(watchedCount = 14, totalCount = 13).remainingCount shouldBeEqualTo 0
    }

    @Test
    fun `GIVEN an unknown total WHEN reading the remaining count THEN it is unknown`() {
        DirectorCollectionUiState(watchedCount = 4, totalCount = null).remainingCount.shouldBeNull()
    }
}

package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SaveRecentSearchUseCaseTest {
    private val repository: RecentSearchesRepository = mockk()

    private lateinit var sut: SaveRecentSearchUseCase

    @BeforeEach
    fun setUp() {
        sut = SaveRecentSearchUseCase(repository)
        coJustRun { repository.saveSearch(any()) }
    }

    @Test
    fun `GIVEN valid query WHEN invoke THEN saves trimmed query`() =
        runTest {
            // WHEN
            sut("  inception  ")

            // THEN
            coVerify(exactly = 1) { repository.saveSearch("inception") }
        }

    @Test
    fun `GIVEN blank query WHEN invoke THEN does not save`() =
        runTest {
            // WHEN
            sut("   ")

            // THEN
            coVerify(exactly = 0) { repository.saveSearch(any()) }
        }

    @Test
    fun `GIVEN empty query WHEN invoke THEN does not save`() =
        runTest {
            // WHEN
            sut("")

            // THEN
            coVerify(exactly = 0) { repository.saveSearch(any()) }
        }
}

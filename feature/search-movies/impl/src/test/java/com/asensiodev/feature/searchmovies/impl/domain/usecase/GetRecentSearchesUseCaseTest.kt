package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetRecentSearchesUseCaseTest {
    private val repository: RecentSearchesRepository = mockk()

    private lateinit var sut: GetRecentSearchesUseCase

    @BeforeEach
    fun setUp() {
        sut = GetRecentSearchesUseCase(repository)
    }

    @Test
    fun `GIVEN history with two entries WHEN invoke THEN emits same list`() =
        runTest {
            // GIVEN
            val searches = listOf("inception", "avatar")
            every { repository.getRecentSearches() } returns flowOf(searches)

            // WHEN
            val result = mutableListOf<List<String>>()
            sut().collect { result.add(it) }

            // THEN
            result.last() shouldBeEqualTo searches
        }

    @Test
    fun `GIVEN empty history WHEN invoke THEN emits empty list`() =
        runTest {
            // GIVEN
            every { repository.getRecentSearches() } returns flowOf(emptyList())

            // WHEN
            val result = mutableListOf<List<String>>()
            sut().collect { result.add(it) }

            // THEN
            result.last() shouldBeEqualTo emptyList()
        }
}

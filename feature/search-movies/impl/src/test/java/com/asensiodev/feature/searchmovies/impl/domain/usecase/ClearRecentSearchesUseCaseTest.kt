package com.asensiodev.feature.searchmovies.impl.domain.usecase

import com.asensiodev.feature.searchmovies.impl.domain.repository.RecentSearchesRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClearRecentSearchesUseCaseTest {
    private val repository: RecentSearchesRepository = mockk()

    private lateinit var sut: ClearRecentSearchesUseCase

    @BeforeEach
    fun setUp() {
        sut = ClearRecentSearchesUseCase(repository)
        coJustRun { repository.clearAll() }
    }

    @Test
    fun `GIVEN non-empty history WHEN invoke THEN calls clearAll on repository`() =
        runTest {
            // WHEN
            sut()

            // THEN
            coVerify(exactly = 1) { repository.clearAll() }
        }
}

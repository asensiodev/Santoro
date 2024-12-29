package com.asensiodev.core.buildconfig
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsDebugBuildTypeUseCaseTest {
    private val buildConfigProvider: BuildConfigProvider = mockk()

    private lateinit var useCase: IsDebugBuildTypeUseCase

    @BeforeEach
    fun setUp() {
        useCase =
            IsDebugBuildTypeUseCase(
                buildConfigProvider,
            )
    }

    @Test
    fun `GIVEN debug WHEN invoke THEN get true`() {
        every { buildConfigProvider.getBuildType() } returns "debug"

        val result = useCase()

        result shouldBe true
    }

    @Test
    fun `GIVEN release WHEN invoke THEN get true`() {
        every { buildConfigProvider.getBuildType() } returns "release"

        val result = useCase()

        result shouldBe false
    }
}

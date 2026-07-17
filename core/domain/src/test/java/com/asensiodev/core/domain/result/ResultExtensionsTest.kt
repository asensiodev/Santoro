package com.asensiodev.core.domain.result

import kotlinx.coroutines.CancellationException
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ResultExtensionsTest {
    @Test
    fun `GIVEN success WHEN rethrowCancellation THEN returns original result`() {
        val result = Result.success("value")

        result.rethrowCancellation() shouldBeEqualTo result
    }

    @Test
    fun `GIVEN ordinary failure WHEN rethrowCancellation THEN returns original result`() {
        val result = Result.failure<String>(IllegalStateException("failure"))

        result.rethrowCancellation() shouldBeEqualTo result
    }

    @Test
    fun `GIVEN cancellation failure WHEN rethrowCancellation THEN throws original cancellation`() {
        val cancellation = CancellationException("cancelled")

        val thrown =
            assertThrows<CancellationException> {
                Result.failure<String>(cancellation).rethrowCancellation()
            }

        thrown shouldBeEqualTo cancellation
    }
}

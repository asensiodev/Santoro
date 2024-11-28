package com.asensiodev.core.testing

import io.kotest.data.blocking.forAll
import io.kotest.data.row

/**
 * Check a callback with 1 boolean parameter for both true and false.
 * Ensure that the mocks are cleared after each loop using [io.mockk.clearMocks] or equivalent.
 */
fun checkBoolean(callback: (Boolean) -> Unit) {
    forAll(row(true), row(false)) {
        callback(it)
    }
}

/**
 * Check a callback with 2 boolean parameters for all possible combinations.
 * Ensure that the mocks are cleared after each loop using [io.mockk.clearMocks] or equivalent.
 */
fun check2Booleans(callback: (Boolean, Boolean) -> Unit) {
    forAll(
        row(TruthTableWith2Booleans(first = true, second = true)),
        row(TruthTableWith2Booleans(first = true, second = false)),
        row(TruthTableWith2Booleans(first = false, second = true)),
        row(TruthTableWith2Booleans(first = false, second = false)),
    ) {
        callback(it.first, it.second)
    }
}

/**
 * Check a callback with 3 boolean parameters for all possible combinations.
 * Ensure that the mocks are cleared after each loop using [io.mockk.clearMocks] or equivalent.
 */
fun check3Booleans(callback: (Boolean, Boolean, Boolean) -> Unit) {
    forAll(
        row(TruthTableWith3Booleans(first = true, second = true, third = true)),
        row(TruthTableWith3Booleans(first = true, second = true, third = false)),
        row(TruthTableWith3Booleans(first = true, second = false, third = true)),
        row(TruthTableWith3Booleans(first = true, second = false, third = false)),
        row(TruthTableWith3Booleans(first = false, second = true, third = true)),
        row(TruthTableWith3Booleans(first = false, second = true, third = false)),
        row(TruthTableWith3Booleans(first = false, second = false, third = true)),
        row(TruthTableWith3Booleans(first = false, second = false, third = false)),
    ) {
        callback(it.first, it.second, it.third)
    }
}

private data class TruthTableWith2Booleans(
    val first: Boolean,
    val second: Boolean,
)

private data class TruthTableWith3Booleans(
    val first: Boolean,
    val second: Boolean,
    val third: Boolean,
)

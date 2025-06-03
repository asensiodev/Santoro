package com.asensiodev.core.testing.dispatcher

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class TestDispatcherProvider
    @OptIn(ExperimentalCoroutinesApi::class)
    constructor(
        override val io: CoroutineDispatcher = UnconfinedTestDispatcher(),
        override val default: CoroutineDispatcher = UnconfinedTestDispatcher(),
        override val main: CoroutineDispatcher = UnconfinedTestDispatcher(),
    ) : DispatcherProvider

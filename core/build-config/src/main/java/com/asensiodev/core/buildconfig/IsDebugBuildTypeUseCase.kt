package com.asensiodev.core.buildconfig

import javax.inject.Inject

class IsDebugBuildTypeUseCase
    @Inject
    constructor(
        private val buildConfigProvider: BuildConfigProvider,
    ) {
        operator fun invoke(): Boolean = buildConfigProvider.getBuildType() == "debug"
    }

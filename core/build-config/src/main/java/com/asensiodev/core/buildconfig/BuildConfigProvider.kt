package com.asensiodev.core.buildconfig

import javax.inject.Inject

class BuildConfigProvider
    @Inject
    constructor() {
        fun getBuildType(): String = BuildConfig.BUILD_TYPE
    }

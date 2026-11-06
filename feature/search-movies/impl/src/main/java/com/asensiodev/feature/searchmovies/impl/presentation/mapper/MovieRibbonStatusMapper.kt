package com.asensiodev.feature.searchmovies.impl.presentation.mapper

import com.asensiodev.core.designsystem.component.statusribbon.MovieRibbonStatus
import com.asensiodev.feature.searchmovies.impl.domain.model.MovieLibraryStatus

internal fun MovieLibraryStatus.toRibbonStatus(): MovieRibbonStatus =
    when (this) {
        MovieLibraryStatus.Watched -> MovieRibbonStatus.WATCHED
        MovieLibraryStatus.Watchlist -> MovieRibbonStatus.WATCHLIST
    }

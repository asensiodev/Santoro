package com.asensiodev.feature.searchmovies.impl.data.repository

internal class StaleDataException : Exception("Serving stale cached data due to network failure")

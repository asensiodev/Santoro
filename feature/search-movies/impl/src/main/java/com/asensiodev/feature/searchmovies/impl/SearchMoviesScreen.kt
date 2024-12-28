package com.asensiodev.feature.searchmovies.impl

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SearchMoviesScreen(onMovieClick: (Int) -> Unit) {
    Text(text = "Search Movies Screen")
    Button(
        onClick = { onMovieClick(1) },
    ) {
        Text(text = "Navigate to Movie Details")
    }
}

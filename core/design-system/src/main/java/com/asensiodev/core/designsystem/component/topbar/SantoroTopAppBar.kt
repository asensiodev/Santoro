package com.asensiodev.core.designsystem.component.topbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.santoro.core.stringresources.R as SR

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SantoroTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            onBackClick?.let {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = AppIcons.ArrowBackIcon,
                        contentDescription = stringResource(SR.string.navigate_back),
                    )
                }
            }
        },
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
fun SantoroTopAppBarPreview() {
    SantoroTopAppBar(
        title = stringResource(SR.string.search_movies_top_bar_title),
        onBackClick = {},
    )
}

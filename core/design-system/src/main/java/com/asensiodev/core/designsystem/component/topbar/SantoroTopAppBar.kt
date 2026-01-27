package com.asensiodev.core.designsystem.component.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.asensiodev.core.designsystem.theme.AppIcons
import com.asensiodev.core.designsystem.theme.SantoroTheme
import com.asensiodev.core.designsystem.theme.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SantoroAppBar(
    title: String,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit) = {},
    backgroundColor: Color? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = AppIcons.ArrowBackIcon,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = actions,
                modifier = modifier,
                colors =
                    if (backgroundColor != null) {
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = backgroundColor,
                        )
                    } else {
                        TopAppBarDefaults
                            .topAppBarColors()
                    },
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        if (backgroundColor ==
                            Color.Transparent
                        ) {
                            PaddingValues(
                                Size.size0,
                            )
                        } else {
                            padding
                        },
                    ),
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun SantoroAppBarPreview() {
    SantoroTheme {
        SantoroAppBar(
            title = "Movie Detail",
            onBackClicked = {},
            actions = {},
            content = {},
        )
    }
}

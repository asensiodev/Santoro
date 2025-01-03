package com.asensiodev.core.designsystem.component.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.asensiodev.core.designsystem.component.bottombar.BottomNavItem
import com.asensiodev.core.designsystem.component.bottombar.BottomNavigationBar

@Composable
fun SantoroScaffold(
    topBar: @Composable () -> Unit,
    bottomNavItems: List<BottomNavItem>,
    selectedBottomNavItem: BottomNavItem?,
    onBottomNavItemSelected: (BottomNavItem) -> Unit,
    content: @Composable () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { topBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavItems,
                selectedItem = selectedBottomNavItem,
                onItemSelected = onBottomNavItemSelected,
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

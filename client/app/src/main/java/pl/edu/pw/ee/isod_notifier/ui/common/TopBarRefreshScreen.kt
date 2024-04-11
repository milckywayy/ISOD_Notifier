package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.ui.UiConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarRefreshScreen(
    navController: NavController,
    title: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    scrollState: ScrollState,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { NavigationText(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Go back")
                    }
                },
            )
        }
    ) { innerPadding ->
        PullToRefreshColumn(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = isRefreshing,
            scrollState = scrollState,
            onRefresh = onRefresh,
            content = {
                content(PaddingValues(vertical = innerPadding.calculateTopPadding() + UiConstants.COMPOSABLE_PADDING))
            },
        )
    }
}
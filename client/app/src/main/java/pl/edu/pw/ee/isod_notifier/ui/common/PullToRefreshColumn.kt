package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshColumn(
    content: @Composable () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }

        if(pullToRefreshState.isRefreshing) {
            LaunchedEffect(true) {
                onRefresh()
            }
        }

        LaunchedEffect(isRefreshing) {
            if(isRefreshing) {
                pullToRefreshState.startRefresh()
            } else {
                pullToRefreshState.endRefresh()
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
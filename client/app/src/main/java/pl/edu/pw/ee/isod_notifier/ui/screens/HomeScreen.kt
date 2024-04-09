package pl.edu.pw.ee.isod_notifier.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.model.ActivityItem
import pl.edu.pw.ee.isod_notifier.model.NewsItem
import pl.edu.pw.ee.isod_notifier.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(0.dp, 18.dp, 0.dp, 0.dp),
                title = { BigTitleText("Hello, Mikołaj!") },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("settings") }
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            isRefreshing = isRefreshing,
            scrollState = scrollState,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    delay(1000L)
                    isRefreshing = false
                }
            },
            content = {
                Spacer(modifier = Modifier)

                LatestNewsPager(navController)
                TileRow(navController)
                ScheduleWidget(navController)
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LatestNewsPager(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionText("Latest news")

        val newsItems = listOf(
            NewsItem("Zajęcia - SIKOMP: Nowa wartość: '5' w polu 'c1-wejściówka' bez komentarza", "2024-03-19", "1002", "2137"),
            NewsItem("Zajęcia - TEMIL: Nowa wartość: '50' w polu 'przetwornik C/A (osc XY)' bez komentarza", "2024-03-19", "1002", "2137"),
            NewsItem("Profil dla klienta VPN", "2024-03-19", "1002", "2137"),
            NewsItem("Ogłoszenie - PROIN: Spotkanie informacyjne", "2024-03-19", "1002", "2137")
        )

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { newsItems.size }
        )

        HorizontalPager(
            state = pagerState,
        ) { page ->
            val newsItem = newsItems[page]

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 0.dp)
            ) {
                NewsTile(
                    newsItem,
                    onClick = {

                    }
                )
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp, 0.dp, 2.dp, 0.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(4.dp)
                )
            }
        }
    }
}

@Composable
fun TileRow(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionText("Activities")

        val tiles = listOf(
            ActivityItem("Classes", Icons.Filled.Home, MaterialTheme.colorScheme.primary, "news"),
            ActivityItem("News", Icons.Filled.Home, MaterialTheme.colorScheme.secondary, "news"),
            ActivityItem("Schedule", Icons.Filled.Home, MaterialTheme.colorScheme.tertiary, "news"),
            ActivityItem("Events", Icons.Filled.Home, MaterialTheme.colorScheme.tertiary, "news"),
        )

        LazyRow(
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tiles) { tile ->
                ActivityTile(tile, onClick = {
                })
            }
        }
    }
}

@Composable
fun ScheduleWidget(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionText("Today's schedule")
    }
}

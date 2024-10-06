package pl.edu.pw.ee.isod_notifier.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.model.ActivityItem
import pl.edu.pw.ee.isod_notifier.model.NewsItem
import pl.edu.pw.ee.isod_notifier.model.NewsTypes
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*
import pl.edu.pw.ee.isod_notifier.ui.theme.*
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val firstname = PreferencesManager.getString(context, "FIRSTNAME", "World")

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(0.dp, 18.dp, 0.dp, 0.dp),
                title = { BigTitleText("Hello, $firstname!") },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("settings") }
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(UiConstants.BIG_SPACE),
            content = {
                Spacer(modifier = Modifier)

                LatestNewsPager(navController)
                TileRow(navController)
                ScheduleWidget(navController)
            },
        )
    }
}

@Composable
fun LatestNewsPager(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(UiConstants.DEFAULT_SPACE)
    ) {
        SectionText("Latest news", padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING))

        val newsItems = listOf(
            NewsItem("Zajęcia - SIKOMP: Nowa wartość: '5' w polu 'c1-wejściówka' bez komentarza", "2137","ISOD", NewsTypes.CLASSES, "2024-03-19", "06:30"),
            NewsItem("Zajęcia - TEMIL: Nowa wartość: '50' w polu 'przetwornik C/A (osc XY)' bez komentarza", "2137", "ISOD", NewsTypes.CLASSES, "2024-03-19", "06:30"),
            NewsItem("Profil dla klienta VPN", "2137", "ISOD", NewsTypes.CLASSES, "2024-03-19", "06:30"),
            NewsItem("Ogłoszenie - PROIN: Spotkanie informacyjne", "2137",  "ISOD", NewsTypes.CLASSES, "2024-03-19", "06:30")
        )

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { newsItems.size }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.height(75.dp)
        ) { page ->
            val newsItem = newsItems[page]

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UiConstants.TILE_PADDING, 0.dp, UiConstants.TILE_PADDING, 0.dp)
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
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface
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
        verticalArrangement = Arrangement.spacedBy(UiConstants.DEFAULT_SPACE)
    ) {

        SectionText("Activities", padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING))

        val tiles = listOf(
            ActivityItem("Classes", Icons.Filled.Class, ColorClasses, "classes"),
            ActivityItem("News", Icons.Filled.Newspaper, ColorNews, "news"),
            ActivityItem("Schedule", Icons.Filled.Schedule, ColorSchedule, "home"),
            ActivityItem("Exams", Icons.Filled.Assessment, ColorExams, "home"),
            ActivityItem("Events", Icons.Filled.Celebration, ColorEvents, "home"),
        )

        LazyRow(
            modifier = Modifier.padding(UiConstants.TILE_PADDING, 0.dp, UiConstants.TILE_PADDING, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(UiConstants.SPACE_BTW_TILES)
        ) {
            items(tiles) { tile ->
                ActivityTile(tile, onClick = {
                    navController.navigate(tile.route)
                })
            }
        }
    }
}

@Composable
fun ScheduleWidget(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(UiConstants.DEFAULT_SPACE)
    ) {
        SectionText("Today's schedule", padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING))
    }
}

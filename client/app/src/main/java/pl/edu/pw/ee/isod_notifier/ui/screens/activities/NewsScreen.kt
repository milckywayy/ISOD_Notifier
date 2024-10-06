package pl.edu.pw.ee.isod_notifier.ui.screens.activities

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.model.NewsItem
import pl.edu.pw.ee.isod_notifier.model.NewsTypes
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*
import pl.edu.pw.ee.isod_notifier.utils.showToast
import pl.edu.pw.ee.isod_notifier.repository.NewsRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun NewsScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = remember { getOkHttpClient(context) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val gson = remember { Gson() }
    val newsRepository = remember { NewsRepository(context, httpClient, gson) }

    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(NewsTypes.ALL) }

    val newsItems = remember { mutableStateListOf<NewsItem>() }

    val pageSize = 10
    var page by remember { mutableIntStateOf(0) }
    var elementsCount by remember { mutableIntStateOf(pageSize) }
    var allNewsCount by remember { mutableIntStateOf(0) }

    fun loadNews(pageToFetch: Int = 0, append: Boolean = false) {
        newsRepository.fetchNews(
            pageToFetch = pageToFetch,
            pageSize = pageSize,
            onSuccess = { fetchedNews ->
                if (!append) {
                    newsItems.clear()
                }

                allNewsCount = fetchedNews.first
                Log.i("after fetch", allNewsCount.toString())
                newsItems.addAll(fetchedNews.second)

                if (append) {
                    elementsCount += pageSize
                } else {
                    page = 0
                    elementsCount = pageSize
                }

                isLoading = false
                isRefreshing = false
            },
            onError = { message ->
                context.showToast(message ?: "Error")
                isLoading = false
                isRefreshing = false
            },
            onFailure = {
                scope.launch {
                    navController.navigate("connection_error")
                }
                isLoading = false
                isRefreshing = false
            }
        )
    }

    LaunchedEffect(Unit) {
        isLoading = true
        loadNews()
    }

    TopBarScreen(
        navController,
        "News",
    ) { innerPadding ->
        PullToRefreshColumn(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = isRefreshing,
            scrollState = scrollState,
            onRefresh = {
                isRefreshing = true
                loadNews()
            },
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(
                                horizontal = UiConstants.TILE_PADDING * 2,
                                vertical = UiConstants.TILE_PADDING
                            )
                            .fillMaxWidth()
                    ) {
                        SectionText(filter.displayName)

                        Box(
                            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                        ) {
                            DropdownMenu(
                                expanded = dropDownMenuExpanded,
                                onDismissRequest = {
                                    dropDownMenuExpanded = false
                                }
                            ) {
                                val menuItems = NewsTypes.entries.associateWith { it.displayName }
                                menuItems.forEach { (type, text) ->
                                    DropdownMenuItem(
                                        text = { Text(text) },
                                        onClick = {
                                            filter = type
                                            dropDownMenuExpanded = false
                                        }
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    dropDownMenuExpanded = true
                                }
                            ) {
                                Icon(Icons.Filled.FilterList, "News filter options")
                            }
                        }
                    }

                    if (newsItems.isNotEmpty()) {
                        ShowNews(navController, newsItems, filter, elementsCount)
                    }

                    if (!isLoading) {
                        Log.i("with button", allNewsCount.toString())
                        if (elementsCount < allNewsCount) {
                            Button(
                                modifier = Modifier.padding(bottom = UiConstants.COMPOSABLE_PADDING),
                                onClick = {
                                    isLoading = true
                                    loadNews(++page, append = true)
                                }
                            ) {
                                Text("Load more")
                            }
                        }
                    } else {
                        LoadingAnimation(
                            modifier = Modifier.padding(bottom = UiConstants.COMPOSABLE_PADDING)
                        )
                    }
                }
            }
        )
    }
}


@Composable
fun ShowNews(
    navController: NavController,
    newsItems: List<NewsItem>,
    filter: NewsTypes,
    elementsCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(UiConstants.DEFAULT_SPACE),
        modifier = Modifier
            .padding(
                UiConstants.TILE_PADDING,
                UiConstants.DEFAULT_SPACE,
                UiConstants.TILE_PADDING,
                UiConstants.BIG_SPACE
            )
    ) {
        var lastDisplayedDay = ""

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val today = LocalDate.now()
        val yesterday = today.minus(1, ChronoUnit.DAYS)

        newsItems.take(elementsCount).forEach {
            if (filter != NewsTypes.ALL && it.type != filter) {
                return@forEach
            }

            val newsDate = LocalDate.parse(it.day, dateFormatter)

            val dayLabel = when (newsDate) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> it.day
            }

            if (lastDisplayedDay != it.day) {
                lastDisplayedDay = it.day
                SubsectionText(
                    dayLabel,
                    PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING)
                )
            }

            val hash = it.hash
            val service = it.service

            NewsTile(
                it,
                onClick = {
                    navController.navigate("newsInfo/$hash/$service")
                }
            )
        }
    }
}

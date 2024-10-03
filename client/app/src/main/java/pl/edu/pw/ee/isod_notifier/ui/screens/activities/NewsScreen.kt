package pl.edu.pw.ee.isod_notifier.ui.screens.activities

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
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.NewsItem
import pl.edu.pw.ee.isod_notifier.model.NewsTypes
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import pl.edu.pw.ee.isod_notifier.utils.getNewsType
import pl.edu.pw.ee.isod_notifier.utils.showToast
import java.util.*

@Composable
fun NewsScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val gson = Gson()

    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingAnotherPage by remember { mutableStateOf(false) }
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(NewsTypes.ALL) }

    val newsItems = remember { mutableStateListOf<NewsItem>() }

    val pageSize = 10
    var page by remember { mutableStateOf(0) }
    var elementsCount by remember { mutableStateOf(pageSize) }

    fun fetchNewsFromService(pageToFetch: Int=0, append: Boolean=false) {
        sendRequest(
            context,
            httpClient,
            "get_student_news",
            mapOf(
                "user_token" to PreferencesManager.getString(context, "USER_ID"),
                "page" to pageToFetch.toString(),
                "page_size" to (pageSize + 1).toString(),
                "language" to Locale.getDefault().language
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()

                if (responseBodyString != null) {

                    val mapType = object : TypeToken<Map<String, Any>>() {}.type
                    val newsMap: Map<String, Any> = gson.fromJson(responseBodyString, mapType)
                    val news = newsMap["news"] as List<Map<String, String>>

                    if (!append) {
                        newsItems.clear()
                    }

                    news.forEach {
                        newsItems.add(
                            NewsItem(
                                it["subject"] as String,
                                it["hash"] as String,
                                it["service"] as String,
                                getNewsType(it["type"] as String),
                                it["day"] as String,
                                it["hour"] as String
                            )
                        )
                    }
                }

                if (append) {
                    elementsCount += pageSize
                }
                else {
                    page = 0
                    elementsCount = pageSize
                }

                isLoading = false
                isRefreshing = false
                isLoadingAnotherPage = false
            },
            onError = { response ->
                val responseBodyString = response.body?.string()

                val message = extractFieldFromResponse(responseBodyString, "message")
                context.showToast(message ?: "Error")

                isLoading = false
                isRefreshing = false
                isLoadingAnotherPage = false
            },
            onFailure = { _ ->
                scope.launch {
                    navController.navigate("connection_error")
                }

                isLoading = false
                isRefreshing = false
                isLoadingAnotherPage = false
            }
        )
    }

    LaunchedEffect(Unit) {
        isLoading = true
        fetchNewsFromService()
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
                fetchNewsFromService()
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
                    if (isLoading) {
                        LoadingAnimation()
                    } else if (newsItems.isNotEmpty()) {
                        ShowNews(navController, newsItems, filter, elementsCount)
                    }

                    if (newsItems.size >= elementsCount) {
                        if (!isLoadingAnotherPage) {
                            Button(
                                modifier = Modifier.padding(bottom = UiConstants.COMPOSABLE_PADDING),
                                onClick = {
                                    isLoadingAnotherPage = true
                                    fetchNewsFromService(++page, append = true)
                                }
                            ) {
                                Text("Load more")
                            }
                        } else {
                            LoadingAnimation(
                                modifier = Modifier.padding(bottom = UiConstants.COMPOSABLE_PADDING)
                            )
                        }
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
        var day = ""

        newsItems.take(elementsCount).forEach { it ->
            if (filter != NewsTypes.ALL && it.type != filter) {
                return@forEach
            }

            if (day != it.day) {
                day = it.day
                SubsectionText(
                    day,
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

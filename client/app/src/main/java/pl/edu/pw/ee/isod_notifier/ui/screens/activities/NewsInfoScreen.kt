package pl.edu.pw.ee.isod_notifier.ui.screens.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.model.FullNewsItem
import pl.edu.pw.ee.isod_notifier.repository.NewsInfoRepository
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*
import pl.edu.pw.ee.isod_notifier.utils.showToast

@Composable
fun NewsInfoScreen(navController: NavController, newsHash: String, newsService: String) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val scope = rememberCoroutineScope()
    val newsInfoRepository = NewsInfoRepository()

    var isLoading by remember { mutableStateOf(false) }
    var newsItem by remember { mutableStateOf<FullNewsItem?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true

        newsInfoRepository.fetchNewsInfo(
            newsHash,
            newsService,
            context,
            httpClient,
            onSuccess = { fetchedNewsItem ->
                newsItem = fetchedNewsItem
                isLoading = false
            },
            onError = { message ->
                context.showToast(message ?: "Error")

                isLoading = false
            },
            onFailure = {
                scope.launch {
                    navController.navigate("connection_error")
                }

                isLoading = false
            }
        )
    }

    TopBarScreen(
        navController,
        "News info",
    ) { innerPadding ->
        if (isLoading) {
            LoadingAnimation()
        }
        else {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                newsItem?.let { ScreenContent(innerPadding, it) }
            }
        }
    }
}

@Composable
fun ScreenContent(
    innerPadding: PaddingValues,
    newsItem: FullNewsItem
) {
    Column(
        modifier = Modifier.padding(
            PaddingValues(
                UiConstants.COMPOSABLE_PADDING,
                innerPadding.calculateTopPadding(),
                UiConstants.COMPOSABLE_PADDING,
                UiConstants.COMPOSABLE_PADDING,
            )
        ),
        verticalArrangement = Arrangement.spacedBy(UiConstants.DEFAULT_SPACE)
    ) {
        TitleText(
            newsItem.subject,
            padding = PaddingValues(vertical = UiConstants.BIG_SPACE)
        )

        HorizontalSpacer()
        HtmlView(newsItem.content)
        HorizontalSpacer()

        SecondaryText(newsItem.date + ", " + newsItem.who)
    }
}
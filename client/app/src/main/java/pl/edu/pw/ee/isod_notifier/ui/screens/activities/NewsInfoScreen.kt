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
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.FullNewsItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import pl.edu.pw.ee.isod_notifier.utils.showToast
import java.util.*

@Composable
fun NewsInfoScreen(navController: NavController, newsHash: String, newsService: String) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var newsItem by remember { mutableStateOf<FullNewsItem?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true

        sendRequest(
            context,
            httpClient,
            "get_single_news",
            mapOf(
                "user_token" to PreferencesManager.getString(context, "USER_ID"),
                "news_hash" to newsHash,
                "news_service" to newsService,
                "language" to Locale.getDefault().language
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()

                if (responseBodyString != null) {
                    val subject = extractFieldFromResponse(responseBodyString, "subject").toString()
                    val hash = extractFieldFromResponse(responseBodyString, "hash").toString()
                    val content = extractFieldFromResponse(responseBodyString, "content").toString()
                    val date = extractFieldFromResponse(responseBodyString, "date").toString()
                    val who = extractFieldFromResponse(responseBodyString, "who").toString()

                    newsItem = FullNewsItem(
                        subject,
                        hash,
                        content,
                        date,
                        who
                    )
                }

                isLoading = false
            },
            onError = { response ->
                val responseBodyString = response.body?.string()

                val message = extractFieldFromResponse(responseBodyString, "message")
                context.showToast(message ?: "Error")

                isLoading = false
            },
            onFailure = { _ ->
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
                newsItem?.let { ScreenContent(navController, innerPadding, it) }
            }
        }
    }
}

@Composable
fun ScreenContent(
    navController: NavController,
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
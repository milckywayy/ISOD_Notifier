package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.FullNewsItem
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import java.util.*

class NewsInfoRepository {
    fun fetchNewsInfo(
        newsHash: String,
        newsService: String,
        context: Context,
        httpClient: OkHttpClient = OkHttpClient(),
        onSuccess: (FullNewsItem) -> Unit,
        onError: (String?) -> Unit,
        onFailure: () -> Unit
    ) {
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

                    val newsItem = FullNewsItem(
                        subject,
                        hash,
                        content,
                        date,
                        who
                    )

                    onSuccess(newsItem)
                } else {
                    onError("Empty response")
                }

            },
            onError = { response ->
                val responseBodyString = response.body?.string()
                val message = extractFieldFromResponse(responseBodyString, "message")

                onError(message)
            },
            onFailure = { _ ->
                onFailure()
            }
        )
    }
}
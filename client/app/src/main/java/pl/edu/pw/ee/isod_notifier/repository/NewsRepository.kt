package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.NewsItem
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import pl.edu.pw.ee.isod_notifier.utils.getNewsType
import java.util.*

class NewsRepository(private val context: Context, private val httpClient: OkHttpClient, private val gson: Gson) {
    fun fetchNews(
        pageToFetch: Int = 0,
        pageSize: Int = 10,
        onSuccess: (List<NewsItem>) -> Unit,
        onError: (String?) -> Unit,
        onFailure: () -> Unit,
    ) {
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

                    val newsItems = news.map {
                        NewsItem(
                            it["subject"] as String,
                            it["hash"] as String,
                            it["service"] as String,
                            getNewsType(it["type"] as String),
                            it["day"] as String,
                            it["hour"] as String
                        )
                    }
                    onSuccess(newsItems)
                } else {
                    onError("Empty response")
                }
            },
            onError = { response ->
                val responseBodyString = response.body?.string()
                val message = extractFieldFromResponse(responseBodyString, "message")
                onError(message)
            },
            onFailure = {
                onFailure()
            }
        )
    }
}
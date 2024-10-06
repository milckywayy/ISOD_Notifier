package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import java.util.*

class UsosStatusRepository(private val context: Context, private val httpClient: OkHttpClient) {
    fun fetchUsosLinkStatus(
        onSuccess: (Boolean) -> Unit,
        onError: (String?) -> Unit,
        onFailure: () -> Unit
    ) {
        val userId = PreferencesManager.getString(context, "USER_ID", "")
        if (userId.isEmpty()) {
            onError("User ID is empty")
            return
        }

        sendRequest(
            context,
            httpClient,
            "get_usos_link_status",
            mapOf(
                "user_token" to userId,
                "language" to Locale.getDefault().language
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()
                if (responseBodyString != null) {
                    val isUsosLinked = extractFieldFromResponse(responseBodyString, "is_usos_linked").toBoolean()
                    onSuccess(isUsosLinked)
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

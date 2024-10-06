package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import java.util.*

class UserStatusRepository(private val context: Context, private val httpClient: OkHttpClient) {
    fun fetchUserStatus(
        onSuccess: (Boolean, Boolean) -> Unit,
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
            "get_user_status",
            mapOf(
                "user_token" to userId,
                "token_fcm" to "test",
                "app_version" to "test",
                "language" to Locale.getDefault().language
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()
                if (responseBodyString != null) {
                    val isIsodLinked = extractFieldFromResponse(responseBodyString, "is_isod_linked").toBoolean()
                    val isUsosLinked = extractFieldFromResponse(responseBodyString, "is_usos_linked").toBoolean()
                    onSuccess(isIsodLinked, isUsosLinked)
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

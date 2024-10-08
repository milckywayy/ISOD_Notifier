package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import pl.edu.pw.ee.isod_notifier.utils.showToast
import java.util.*

class EraseAllDataRepository (private val context: Context, private val httpClient: OkHttpClient) {
    fun sendEraseAllDataRequest(
        onSuccess: () -> Unit,
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
            "delete_user_data",
            mapOf(
                "user_token" to userId,
                "language" to Locale.getDefault().language
            ),
            onSuccess = {
                onSuccess()
            },
            onError = { response ->
                val responseBodyString = response.body?.string()
                val message = extractFieldFromResponse(responseBodyString, "message")

                context.showToast(message ?: "Error")

                onError(message)
            },
            onFailure = {
                onFailure()
            }
        )
    }
}
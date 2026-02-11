package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
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

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                onError("Fetching FCM registration token failed")
                return@addOnCompleteListener
            }
            val token = task.result

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = packageInfo.versionName ?: "1.0.0"

            sendRequest(
                context,
                httpClient,
                "get_usos_link_status",
                mapOf(
                    "user_token" to userId,
                    "token_fcm" to token,
                    "app_version" to version,
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
}

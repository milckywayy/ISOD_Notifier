package pl.edu.pw.ee.isod_notifier.repository;

import android.content.Context;
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient;
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import pl.edu.pw.ee.isod_notifier.utils.showToast
import java.util.*

class LogoutOtherDevicesRepository (private val context:Context, private val httpClient:OkHttpClient) {
    fun sendLogoutOthersDevicesRequest(
        onSuccess: () -> Unit,
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
                return@addOnCompleteListener
            }
            val token = task.result

            sendRequest(
                context,
                httpClient,
                "logout_from_all_other_devices",
                mapOf(
                    "user_token" to userId,
                    "token_fcm" to token,
                    "language" to Locale.getDefault().language
                ),
                onSuccess = { response ->
                    val responseBodyString = response.body?.string()
                    val userToken = extractFieldFromResponse(responseBodyString, "user_token").toString()

                    PreferencesManager.saveString(context, "USER_ID", userToken)

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
}

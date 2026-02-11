package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
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

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                onError("Fetching FCM registration token failed")
                return@addOnCompleteListener
            }
            val token = task.result

            val version = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0)).versionName
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                }
            } catch (e: Exception) {
                null
            } ?: "1.0.0"

            sendRequest(
                context,
                httpClient,
                "get_user_status",
                mapOf(
                    "user_token" to userId,
                    "token_fcm" to token,
                    "app_version" to version,
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
}

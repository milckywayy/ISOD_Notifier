package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.ScheduleDayItem
import pl.edu.pw.ee.isod_notifier.model.ScheduleResponseItem
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import java.util.*

class ScheduleRepository(
    private val context: Context,
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val gson: Gson = Gson()
) {
    fun fetchSchedule(
        onSuccess: (List<ScheduleDayItem>) -> Unit,
        onError: (String?) -> Unit,
        onFailure: () -> Unit
    ) {
        sendRequest(
            context,
            httpClient,
            "get_student_schedule",
            mapOf(
                "user_token" to PreferencesManager.getString(context, "USER_ID"),
                "language" to Locale.getDefault().language
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()

                if (responseBodyString != null) {
                    try {
                        val schedule = gson.fromJson(responseBodyString, ScheduleResponseItem::class.java)
                        onSuccess(schedule.classes)
                    } catch (e: Exception) {
                        onError("Failed to parse schedule data")
                    }
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

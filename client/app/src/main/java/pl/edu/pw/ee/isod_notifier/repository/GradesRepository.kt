package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.GradeItem
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import java.util.*

class GradesRepository(
    private val context: Context,
    private val httpClient: OkHttpClient,
    private val gson: Gson
) {

    fun fetchGrades(
        courseId: String,
        classType: String,
        term: String,
        onSuccess: (List<GradeItem>) -> Unit,
        onError: (String?) -> Unit,
        onFailure: () -> Unit
    ) {
        sendRequest(
            context,
            httpClient,
            "get_student_grades",
            mapOf(
                "user_token" to PreferencesManager.getString(context, "USER_ID"),
                "course_id" to courseId,
                "classtype" to classType,
                "semester" to term,
                "language" to Locale.getDefault().language,
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()
                if (responseBodyString != null) {
                    try {
                        val mapType = object : TypeToken<Map<String, Any>>() {}.type
                        val responseMap: Map<String, Any> = gson.fromJson(responseBodyString, mapType)
                        val itemsList = responseMap["items"] as List<Map<String, Any>>

                        val gradeItems = itemsList.map { item ->
                            GradeItem(
                                name = item["name"] as String,
                                value = item["value"] as String? ?: "",
                                weight = (item["weight"] as Number).toFloat(),
                                accounted = item["accounted"] as Boolean,
                                valueNote = item["value_note"] as String? ?: ""
                            )
                        }
                        onSuccess(gradeItems)
                    } catch (e: Exception) {
                        onError("Failed to parse response")
                    }
                } else {
                    onError("Empty response")
                }
            },
            onError = { response ->
                val message = extractFieldFromResponse(response.body?.string(), "message")
                onError(message)
            },
            onFailure = {
                onFailure()
            }
        )
    }
}

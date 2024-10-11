package pl.edu.pw.ee.isod_notifier.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.ClassItem
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import java.util.*

class ClassesRepository(
    private val context: Context,
    private val httpClient: OkHttpClient,
    private val gson: Gson
) {
    fun fetchTerms(
        onSuccess: (List<String>) -> Unit,
        onError: (String?) -> Unit,
        onFailure: () -> Unit
    ) {
        sendRequest(
            context,
            httpClient,
            "get_semesters",
            mapOf(
                "user_token" to PreferencesManager.getString(context, "USER_ID"),
                "language" to Locale.getDefault().language
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()
                if (responseBodyString != null) {
                    val termsArray = responseBodyString.trim('[', ']').split(", ").map { it.trim('\"') }
                    onSuccess(termsArray)
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

    fun fetchClasses(
        term: String,
        onSuccess: (List<ClassItem>) -> Unit,
        onError: (String?) -> Unit,
        onFailure: () -> Unit
    ) {
        sendRequest(
            context,
            httpClient,
            "get_student_courses",
            mapOf(
                "user_token" to PreferencesManager.getString(context, "USER_ID"),
                "semester" to term,
                "language" to Locale.getDefault().language
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()
                if (responseBodyString != null) {
                    val mapType = object : TypeToken<Map<String, Any>>() {}.type
                    val courseMap: Map<String, Any> = gson.fromJson(responseBodyString, mapType)
                    val courses = courseMap["courses"] as List<Map<String, Any>>

                    val classItems = courses.flatMap { course ->
                        val courseName = course["name"] as String
                        val courseId = course["course_id"] as String
                        val classesTemp = course["classes"] as List<Map<String, String>>
                        classesTemp.map { classInfo ->
                            ClassItem(courseName, classInfo["classtype"] ?: "", courseId)
                        }
                    }
                    onSuccess(classItems)
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

package pl.edu.pw.ee.isod_notifier.ui.screens.activities

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.logger.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.ClassItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ClassTile
import pl.edu.pw.ee.isod_notifier.ui.common.LoadingAnimation
import pl.edu.pw.ee.isod_notifier.ui.common.SectionText
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import pl.edu.pw.ee.isod_notifier.utils.showToast
import java.util.*

@Composable
fun ClassesScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val scope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }

    val terms = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        isLoading = true

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
                    val array = responseBodyString.trim('[', ']').split(", ").map { it.trim('\"') }.toTypedArray()

                    array.forEach { Log.d("Response", it) }

                    terms.clear()
                    terms.addAll(array)
                }

                isLoading = false
            },
            onError = { response ->
                val responseBodyString = response.body?.string()

                val message = extractFieldFromResponse(responseBodyString, "message")
                context.showToast(message ?: "Error")

                PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
                isLoading = false
            },
            onFailure = { _ ->
                scope.launch {
                    navController.navigate("connection_error")
                }

                PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
                isLoading = false
            }
        )
    }

    TopBarScreen(
        navController,
        "Classes"
    ) { innerPadding ->
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            if (terms.isNotEmpty()) {
                ScreenContent(
                    navController,
                    terms,
                    innerPadding,
                    httpClient
                )
            }
        }
    }
}

@Composable
fun ScreenContent(
    navController: NavController,
    terms: List<String>,
    innerPadding: PaddingValues,
    httpClient: OkHttpClient
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val gson = Gson()

    var isLoading by remember { mutableStateOf(false) }
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    var termFilter by remember { mutableStateOf(terms[0]) }
    val classes = remember { mutableListOf<ClassItem>() }

    LaunchedEffect(termFilter) {
        isLoading = true

        sendRequest(
            context,
            httpClient,
            "get_student_courses",
            mapOf(
                "user_token" to PreferencesManager.getString(context, "USER_ID"),
                "semester" to termFilter,
                "language" to Locale.getDefault().language
            ),
            onSuccess = { response ->
                val responseBodyString = response.body?.string()

                if (responseBodyString != null) {
                    val mapType = object : TypeToken<Map<String, Any>>() {}.type
                    val courseMap: Map<String, Any> = gson.fromJson(responseBodyString, mapType)
                    val courses = courseMap["courses"] as List<Map<String, Any>>

                    classes.clear()
                    courses.forEach { course ->
                        val courseName = course["name"] as String
                        val courseId = course["course_id"] as String
                        val classesTemp = course["classes"] as List<Map<String, String>>

                        classesTemp.forEach { classInfo ->
                            val classType = classInfo["classtype"] as String
                            classes.add(ClassItem(courseName, classType, courseId))
                        }
                    }
                }

                isLoading = false
            },
            onError = { response ->
                val responseBodyString = response.body?.string()

                val message = extractFieldFromResponse(responseBodyString, "message")
                context.showToast(message ?: "Error")

                PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
                isLoading = false
            },
            onFailure = { _ ->
                scope.launch {
                    navController.navigate("connection_error")
                }

                PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
                isLoading = false
            }
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(UiConstants.DEFAULT_SPACE),
        modifier = Modifier
            .padding(
                UiConstants.TILE_PADDING,
                innerPadding.calculateTopPadding() + UiConstants.DEFAULT_SPACE,
                UiConstants.TILE_PADDING,
                UiConstants.BIG_SPACE
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = UiConstants.TILE_PADDING)
                .fillMaxSize()
        ) {
            SectionText("Term: $termFilter")

            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                DropdownMenu(
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = {
                        dropDownMenuExpanded = false
                    }
                ) {
                    terms.forEach { term ->
                        DropdownMenuItem(
                            text = { Text(term) },
                            onClick = {
                                termFilter = term
                                dropDownMenuExpanded = false
                            }
                        )
                    }
                }

                IconButton(
                    onClick = {
                        dropDownMenuExpanded = true
                    }
                ) {
                    Icon(Icons.Filled.FilterList, "News filter options")
                }
            }
        }

        if (isLoading) {
            LoadingAnimation()
        }
        else {
            for (classItem in classes) {
                ClassTile(
                    classItem,
                    onClick = {

                    }
                )
            }
        }
    }
}
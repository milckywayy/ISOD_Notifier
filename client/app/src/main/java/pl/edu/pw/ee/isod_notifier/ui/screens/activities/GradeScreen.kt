package pl.edu.pw.ee.isod_notifier.ui.screens.activities

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.gson.Gson
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.model.GradeItem
import pl.edu.pw.ee.isod_notifier.model.NewsItem
import pl.edu.pw.ee.isod_notifier.repository.GradesRepository
import pl.edu.pw.ee.isod_notifier.repository.NewsRepository
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.LoadingAnimation
import pl.edu.pw.ee.isod_notifier.ui.common.PullToRefreshColumn
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen

@Composable
fun GradeScreen(
    navController: NavController,
    courseId: String,
    classType: String,
    term: String,
) {
    val context = LocalContext.current
    val httpClient = remember { getOkHttpClient(context) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val gradesItems = remember { mutableStateListOf<GradeItem>() }

    val gson = remember { Gson() }
    val gradesRepository = remember { GradesRepository(context, httpClient, gson) }

    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    fun loadGrades() {
        gradesRepository.fetchGrades(
            courseId,
            classType,
            term,
            onSuccess = { grades ->
                gradesItems.addAll(grades)
                
                isLoading = false
                isRefreshing = false
            },
            onError = {
                isLoading = false
                isRefreshing = false
            },
            onFailure = {
                isLoading = false
                isRefreshing = false
            }
        )
    }

    LaunchedEffect(Unit) {
        isLoading = true
        loadGrades()
    }

    TopBarScreen(
        navController,
        "Grades",
    ) { innerPadding ->
        PullToRefreshColumn(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = isRefreshing,
            scrollState = scrollState,
            onRefresh = {
                isRefreshing = true
            },
            content = {
                if (isLoading) {
                    LoadingAnimation(
                        modifier = Modifier.padding(bottom = UiConstants.COMPOSABLE_PADDING)
                    )
                } else {
                    Text(text = gradesItems.size.toString())
                    gradesItems.forEach { grade ->

                        Text(text = "${grade.name} ${grade.value}")
                    }
                }
            }
        )
    }
}
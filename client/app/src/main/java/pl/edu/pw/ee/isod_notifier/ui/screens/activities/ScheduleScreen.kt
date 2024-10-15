package pl.edu.pw.ee.isod_notifier.ui.screens.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.model.ScheduleDayItem
import pl.edu.pw.ee.isod_notifier.repository.ScheduleRepository
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager

@Composable
fun ScheduleScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val gson = Gson()
    val scheduleRepository = remember { ScheduleRepository(context, httpClient, gson) }
    val scope = rememberCoroutineScope()

    val scheduleDays = remember { mutableStateListOf<ScheduleDayItem>() }

    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }

    fun loadSchedule() {
        scope.launch {
            isLoading = true
            scheduleRepository.fetchSchedule(
                onSuccess = { fetchedScheduleDays ->
                    scheduleDays.clear()
                    scheduleDays.addAll(fetchedScheduleDays)
                    isLoading = false
                },
                onError = {
                    isLoading = false
                },
                onFailure = {
                    navController.navigate("connection_error")
                    PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
                    isLoading = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        loadSchedule()
    }

    TopBarScreen(
        navController,
        "Classes"
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            if (scheduleDays.isNotEmpty()) {
                ScreenContent(
                    navController,
                    scheduleDays
                )
            } else if (isLoading) {
                Text(text = "Loading...")
            } else {
                Text(text = "No classes scheduled.")
            }
        }
    }
}

@Composable
fun ScreenContent(
    navController: NavController,
    scheduleDays: List<ScheduleDayItem>,
) {
    for (day in scheduleDays) {
        Text(text = day.dayOfWeek.toString())
    }
}

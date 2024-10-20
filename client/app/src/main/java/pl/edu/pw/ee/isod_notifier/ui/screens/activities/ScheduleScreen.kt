package pl.edu.pw.ee.isod_notifier.ui.screens.activities

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.model.ScheduleDayItem
import pl.edu.pw.ee.isod_notifier.repository.ScheduleRepository
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*

@Composable
fun ScheduleScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val gson = Gson()
    val scheduleRepository = remember { ScheduleRepository(context, httpClient, gson) }
    val scope = rememberCoroutineScope()

    val scheduleDays = remember { mutableStateListOf<ScheduleDayItem>() }
    val selectedDayIndex = remember { mutableIntStateOf(0) }

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
                    isLoading = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        loadSchedule()
    }

    TopBarScreen(
        navController,
        "Schedule"
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            if (isLoading) {
                LoadingAnimation()
            } else if (scheduleDays.isNotEmpty()) {
                val pagerState = rememberPagerState(
                    initialPage = selectedDayIndex.value,
                    pageCount = { scheduleDays.size }
                )

                LaunchedEffect(pagerState.currentPage) {
                    if (selectedDayIndex.intValue != pagerState.currentPage) {
                        selectedDayIndex.intValue = pagerState.currentPage
                    }
                }


//                DayPicker(
//                    scheduleDays = scheduleDays,
//                    selectedDayIndex = selectedDayIndex.value,
//                    onDaySelected = { index ->
//                        selectedDayIndex.value = index
//                        scope.launch {
//                            pagerState.scrollToPage(index)
//                        }
//                    }
//                )
                Spacer(modifier = Modifier.height(UiConstants.DEFAULT_SPACE))

                HorizontalPager(
                    verticalAlignment = Alignment.Top,
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    key = { page -> scheduleDays[page].dayOfWeek }
                ) { page ->
                    ScheduleDayTable(scheduleDays[page])
                }
            } else {
                ContentText(
                    text = "No classes scheduled.",
                )
            }
        }
    }
}

@Composable
fun DayPicker(
    scheduleDays: List<ScheduleDayItem>,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = UiConstants.TILE_PADDING)
    ) {
        Text(
            text = "Select a day:",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(UiConstants.DEFAULT_SPACE))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            scheduleDays.forEachIndexed { index, day ->
                Button(
                    onClick = { onDaySelected(index) },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .border(
                            2.dp,
                            if (index == selectedDayIndex) Color.Blue else Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(text = getDayName(day.dayOfWeek))
                }
            }
        }
    }
}

fun generateTimeSlots(): List<String> {
    val timeSlots = mutableListOf<String>()
    var currentTime = 8 * 60
    val endTime = 20 * 60

    while (currentTime < endTime) {
        val hours = currentTime / 60
        val minutes = currentTime % 60
        val timeSlot = String.format("%02d:%02d", hours, minutes)
        timeSlots.add(timeSlot)
        currentTime += 120
    }

    return timeSlots
}

fun getDayName(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> "Unknown"
    }
}


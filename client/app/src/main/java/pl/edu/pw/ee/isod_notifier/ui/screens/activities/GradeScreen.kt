package pl.edu.pw.ee.isod_notifier.ui.screens.activities

import androidx.collection.emptyObjectList
import androidx.compose.foundation.background
import pl.edu.pw.ee.isod_notifier.ui.common.GradeTile
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.gson.Gson
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.model.CourseDetailsItem
import pl.edu.pw.ee.isod_notifier.model.GradeItem
import pl.edu.pw.ee.isod_notifier.repository.GradesRepository
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*

@Composable
fun GradeScreen(
    navController: NavController,
    courseName: String,
    courseId: String,
    classType: String,
    term: String,
) {
    val context = LocalContext.current
    val httpClient = remember { getOkHttpClient(context) }
    val scrollState = rememberScrollState()

    val gradesItems = remember { mutableStateListOf<GradeItem>() }
    val courseDetailsItem = remember { mutableStateOf(CourseDetailsItem("", 0.0, emptyList(), "")) }

    val gson = remember { Gson() }
    val gradesRepository = remember { GradesRepository(context, httpClient, gson) }

    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    fun loadGrades() {
        gradesRepository.fetchGrades(
            courseId,
            classType,
            term,
            onSuccess = { result ->
                courseDetailsItem.value = result.first
                val grades = result.second

                gradesItems.clear()
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
            verticalArrangement = Arrangement.Bottom,
            isRefreshing = isRefreshing,
            scrollState = scrollState,
            onRefresh = {
                isRefreshing = true
                loadGrades()
            },
            content = {
                if (isLoading) {
                    LoadingAnimation(
                        modifier = Modifier.padding(vertical = innerPadding.calculateTopPadding())
                    )
                } else {
                    ScreenContent(courseName, classType, courseId, courseDetailsItem.value, gradesItems)
                }
            }
        )
    }
}

@Composable
fun ScreenContent(courseName: String, classType: String, courseId: String, courseDetailsItem: CourseDetailsItem, gradesItems: List<GradeItem>) {
    Column(
        modifier = Modifier
            .padding(
                PaddingValues(
                    vertical = UiConstants.BIG_SPACE,
                    horizontal = UiConstants.TILE_PADDING
                )
            )
            .fillMaxWidth()
            .padding(UiConstants.SMALL_SPACE)
    ) {
        TitleText(text = courseName)
        SecondaryText(text = "$courseId - $classType")

        Spacer(modifier = Modifier.height(UiConstants.DEFAULT_SPACE))

        if (courseDetailsItem.place.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Miejsce",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_SPACE))
                SmallContentText(text = courseDetailsItem.place)
            }
        }

        if (courseDetailsItem.teachers.isNotEmpty()) {
            courseDetailsItem.teachers.forEach { teacher ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Prowadzący",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(UiConstants.EXTRA_SMALL_SPACE))
                    SmallContentText(text = teacher)
                }
            }
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = UiConstants.TILE_PADDING)
    ) {
        if (courseDetailsItem.finalGrade.isNotEmpty()) {
            FinalGradeTile(courseDetailsItem)
            Spacer(modifier = Modifier.height(UiConstants.BIG_SPACE))
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(UiConstants.DEFAULT_SPACE),
        modifier = Modifier
            .padding(
                horizontal = UiConstants.TILE_PADDING,
            )
    ) {
        if (gradesItems.isNotEmpty()) {
            gradesItems.forEach { grade ->
                GradeTile(grade)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UiConstants.DEFAULT_SPACE)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No grades icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ContentText(text = "No grades yet!")
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(
                PaddingValues(
                    vertical = UiConstants.DEFAULT_SPACE,
                    horizontal = UiConstants.TILE_PADDING
                )
            )
            .fillMaxWidth()
            .padding(UiConstants.SMALL_SPACE)
    ) {
        if (courseDetailsItem.totalPoints > 0.0) {
            SubsectionText(
                text = "Total points: ${courseDetailsItem.totalPoints}",
            )
        }
    }

    Spacer(modifier = Modifier.height(UiConstants.DEFAULT_SPACE))
}
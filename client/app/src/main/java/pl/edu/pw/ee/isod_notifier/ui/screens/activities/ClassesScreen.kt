package pl.edu.pw.ee.isod_notifier.ui.screens.activities

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
import com.google.gson.Gson
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.model.ClassItem
import pl.edu.pw.ee.isod_notifier.repository.ClassesRepository
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ClassTile
import pl.edu.pw.ee.isod_notifier.ui.common.LoadingAnimation
import pl.edu.pw.ee.isod_notifier.ui.common.SectionText
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.showToast

@Composable
fun ClassesScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val gson = Gson()
    val repository = remember { ClassesRepository(context, httpClient, gson) }
    val scope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }

    val terms = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        isLoading = true
        repository.fetchTerms(
            onSuccess = { fetchedTerms ->
                terms.clear()
                terms.addAll(fetchedTerms)
                isLoading = false
            },
            onError = { message ->
                context.showToast(message ?: "Error")
                PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
                isLoading = false
            },
            onFailure = {
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
                    repository
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
    repository: ClassesRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    var termFilter by remember { mutableStateOf(terms[0]) }
    val classes = remember { mutableListOf<ClassItem>() }

    LaunchedEffect(termFilter) {
        isLoading = true
        repository.fetchClasses(
            term = termFilter,
            onSuccess = { fetchedClasses ->
                classes.clear()
                classes.addAll(fetchedClasses)
                isLoading = false
            },
            onError = { message ->
                context.showToast(message ?: "Error")
                PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
                isLoading = false
            },
            onFailure = {
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
        } else {
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

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
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.model.ClassItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ClassTile
import pl.edu.pw.ee.isod_notifier.ui.common.SectionText
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen

@Composable
fun ClassesScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    val terms = listOf(
        "2022Z",
        "2023L",
        "2023Z",
        "2024L"
    ).reversed()

    TopBarScreen(
        navController,
        "Classes"
    ) { innerPadding ->
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            ScreenContent(
                navController,
                terms,
                innerPadding
            )
        }
    }
}

private fun getClasses(term: String): List<ClassItem> {
    if (term == "2024L") {
        return listOf(
            ClassItem("Sieci komputerowee wew few gew gwe jbjkbjkhvh uu", "WYK", "1"),
            ClassItem("Toria obwodów i sygnałów", "LAB", "1"),
            ClassItem("Metody numeryczne", "WYK","1"),
            ClassItem("Inżynieria oprogramowania", "WYK", "1"),
            ClassItem("Języki i metody programowania", "PRO", "1"),
        )
    }
    return emptyList()
}

@Composable
fun ScreenContent(
    navController: NavController,
    terms: List<String>,
    innerPadding: PaddingValues
) {

    var dropDownMenuExpanded by remember { mutableStateOf(false) }

    var termFilter by remember { mutableStateOf(terms[0]) }
    val newsGroups = getClasses(termFilter)

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

        for (classItem in getClasses(termFilter)) {
            ClassTile(
                classItem,
                onClick = {

                }
            )
        }
    }
}
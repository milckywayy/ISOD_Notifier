package pl.edu.pw.ee.isod_notifier.ui.screens.activities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.model.NewsItem
import pl.edu.pw.ee.isod_notifier.model.NewsTypes
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.NewsTile
import pl.edu.pw.ee.isod_notifier.ui.common.SectionText
import pl.edu.pw.ee.isod_notifier.ui.common.SubsectionText
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen
import java.util.*

@Composable
fun NewsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    val newsItems = listOf(
        NewsItem("Zajęcia - SIKOMP: Nowa wartość: '5' w polu 'c1-wejściówka' bez komentarza", "2137","ISOD", "1002", "2024-03-19", "22:30"),
        NewsItem("Zajęcia - TEMIL: Nowa wartość: '50' w polu 'przetwornik C/A (osc XY)' bez komentarza", "2137", "ISOD", "1002", "2024-03-19", "21:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1002", "2024-03-19", "20:30"),
        NewsItem("Profil dla klienta VPNx", "2137", "ISOD", "1002", "2024-03-19", "23:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1000", "2024-03-19", "18:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1011", "2024-03-18", "17:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1002", "2024-03-18", "16:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1000", "2024-03-20", "15:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1002", "2024-03-18", "14:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1002", "2024-03-17", "13:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1000", "2024-03-17", "12:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1002", "2024-03-17", "11:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1010", "2024-03-16", "10:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1009", "2024-03-16", "09:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1008", "2024-03-16", "08:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1002", "2024-03-15", "06:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "2414", "2024-03-15", "07:30"),
        NewsItem("Profil dla klienta VPN", "2137", "ISOD", "1002", "2024-03-15", "06:30"),
        NewsItem("Ogłoszenie - PROIN: Spotkanie informacyjne", "2137",  "ISOD", "1002", "2024-03-11", "06:30")
    )

    TopBarScreen(
        navController,
        "News"
    ) { innerPadding ->
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            ScreenContent(
                navController,
                newsItems,
                innerPadding
            )
        }
    }
}

private fun filterNews(newsItems: List<NewsItem>, type: NewsTypes): SortedMap<String, List<NewsItem>> {
    when (type) {
        NewsTypes.ALL -> {
            return newsItems.groupBy { it.day }.toSortedMap(reverseOrder())
        }
        NewsTypes.CLASSES -> {
            val filter = listOf("1001", "1002", "1003", "1004", "1005")
            val newsItemsFiltered = newsItems.filter { filter.contains(it.type) }
            return newsItemsFiltered.groupBy { it.day }.toSortedMap(reverseOrder())
        }
        NewsTypes.FACULTY -> {
            val filter = listOf("1000")
            val newsItemsFiltered = newsItems.filter { filter.contains(it.type) }
            return newsItemsFiltered.groupBy { it.day }.toSortedMap(reverseOrder())
        }
        NewsTypes.WRS -> {
            val filter = listOf("2414")
            val newsItemsFiltered = newsItems.filter { filter.contains(it.type) }
            return newsItemsFiltered.groupBy { it.day }.toSortedMap(reverseOrder())
        }

        // Other
        else -> {
            val filter = listOf("1000", "1001", "1002", "1003", "1004", "1005", "2414")
            val newsItemsFiltered = newsItems.filter { !filter.contains(it.type) }
            return newsItemsFiltered.groupBy { it.day }.toSortedMap(reverseOrder())
        }
    }
}

@Composable
fun ScreenContent(
    navController: NavController,
    newsItems: List<NewsItem>,
    innerPadding: PaddingValues
) {
    var dropDownMenuExpanded by remember { mutableStateOf(false) }

    var filter by remember { mutableStateOf(NewsTypes.FACULTY) }
    val newsGroups = filterNews(newsItems, filter)

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
            SectionText("All news")

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
                    val menuItems = listOf(
                        "All news" to NewsTypes.ALL,
                        "Class news" to NewsTypes.CLASSES,
                        "Faculty announcements" to NewsTypes.FACULTY,
                        "WRS news" to NewsTypes.WRS,
                        "Other" to NewsTypes.OTHER
                    )
                    menuItems.forEach { (text, type) ->
                        DropdownMenuItem(
                            text = { Text(text) },
                            onClick = {
                                filter = type
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

        for (newsGroup in newsGroups) {
            SubsectionText(
                newsGroup.key,
                PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING)
            )

            for (newsItem in newsGroup.value.sortedBy { it.hour }.reversed()) {
                NewsTile(
                    newsItem,
                    onClick = {

                    }
                )
            }
        }
    }
}

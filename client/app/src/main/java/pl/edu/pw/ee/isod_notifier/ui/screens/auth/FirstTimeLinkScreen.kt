package pl.edu.pw.ee.isod_notifier.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.R
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.model.ActivityItem
import pl.edu.pw.ee.isod_notifier.repository.UserStatusRepository
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ActivityTile
import pl.edu.pw.ee.isod_notifier.ui.common.BigTitleText
import pl.edu.pw.ee.isod_notifier.ui.common.InfoBar
import pl.edu.pw.ee.isod_notifier.ui.common.LoadingAnimation
import pl.edu.pw.ee.isod_notifier.utils.*
import java.util.*

@Composable
fun FirstTimeLinkScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val scope = rememberCoroutineScope()
    val repository = UserStatusRepository(context, httpClient)
    val notificationStorage = NotificationStorage(context)

    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(true) }
    var isIsodLinked by remember { mutableStateOf(false) }
    var isUsosLinked by remember { mutableStateOf(false) }

    // TODO ask for notification permissions

    LaunchedEffect(Unit) {
        isLoading = true

        val userId = PreferencesManager.getString(context, "USER_ID", "")
        if (userId != "") {
            repository.fetchUserStatus(
                onSuccess = { isodLinked, usosLinked ->
                    isIsodLinked = isodLinked
                    isUsosLinked = usosLinked

                    if (!PreferencesManager.getBoolean(context, "STATUS_CHECKED")) {
                        if (isIsodLinked || isUsosLinked) {
                            PreferencesManager.saveBoolean(context, "LET_IN", true)
                        }
                        else {
                            notificationStorage.clearNotifications() // Clear home screen recent news
                        }
                    }

                    if (PreferencesManager.getBoolean(context, "LET_IN")) {
                        PreferencesManager.saveBoolean(context, "STATUS_CHECKED", false)

                        scope.launch {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }
                    }

                    PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
                    isLoading = false
                },
                onError = { message ->
                    context.showToast(message ?: "Error")
                    PreferencesManager.saveString(context, "USER_ID", "")
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
        } else {
            PreferencesManager.saveBoolean(context, "STATUS_CHECKED", true)
            isLoading = false
        }
    }

    val tiles = mutableListOf<ActivityItem>()

    if (isIsodLinked) {
        tiles.add(
            ActivityItem(
                "ISOD",
                ImageVector.vectorResource(R.drawable.we_logo),
                MaterialTheme.colorScheme.primary,
                "link_isod"
            )
        )
    }
    else {
        tiles.add(
            ActivityItem(
                "ISOD",
                ImageVector.vectorResource(R.drawable.we_logo),
                MaterialTheme.colorScheme.secondary,
                "link_isod"
            )
        )
    }

    if (isUsosLinked) {
        tiles.add(
            ActivityItem(
                "USOS",
                ImageVector.vectorResource(R.drawable.usos_logo),
                MaterialTheme.colorScheme.primary,
                "link_usos"
            )
        )
    }
    else {
        tiles.add(
            ActivityItem(
                "USOS",
                ImageVector.vectorResource(R.drawable.usos_logo),
                MaterialTheme.colorScheme.secondary,
                "link_usos"
            )
        )
    }

    if (isLoading || PreferencesManager.getBoolean(context, "LET_IN")) {
        LoadingAnimation()
    }
    else {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .verticalScroll(scrollState)
        ) {
            ScreenContent(
                navController,
                tiles,
                isIsodLinked,
                isUsosLinked
            )
        }
    }

}

@Composable
fun ScreenContent(
    navController: NavController,
    tiles: MutableList<ActivityItem>,
    isIsodLinked: Boolean,
    isUsosLinked: Boolean,
) {
    val context = LocalContext.current

    BigTitleText(
        "Link service",
        padding = PaddingValues(
            horizontal = UiConstants.COMPOSABLE_PADDING,
            vertical = 32.dp
        )
    )

    Column {
        InfoBar(
            "In order to use app, please log in to at least one university service.",
            icon = Icons.Filled.Info,
            paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
        )

        Spacer(modifier = Modifier.height(UiConstants.DEFAULT_SPACE))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            items(tiles) { tile ->
                Row(modifier = Modifier.padding(horizontal = UiConstants.SPACE_BTW_TILES / 2)) {
                    ActivityTile(tile, onClick = {
                        navController.navigate(tile.route)
                    })
                }
            }
        }
    }

    Spacer(modifier = Modifier)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Button(
            modifier = Modifier.padding(UiConstants.COMPOSABLE_PADDING),
            onClick = {
                if (isIsodLinked || isUsosLinked) {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
                else {
                    context.showToast("At least one service must be linked")
                }
            }
        ) {
            Text("Start")
        }
    }
}
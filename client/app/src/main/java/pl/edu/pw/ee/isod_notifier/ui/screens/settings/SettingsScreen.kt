package pl.edu.pw.ee.isod_notifier.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.R
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ClickSetting
import pl.edu.pw.ee.isod_notifier.ui.common.SettingsSection
import pl.edu.pw.ee.isod_notifier.ui.common.SwitchSetting
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.openURL

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var notificationsEnabled by remember { mutableStateOf(PreferencesManager.getBoolean(context, "NEWS_ALLOWED", true)) }
    var classesEnabled by remember { mutableStateOf(PreferencesManager.getBoolean(context, "RECEIVE_CLASSES_NEWS", true)) }
    var announcementsEnabled by remember { mutableStateOf(PreferencesManager.getBoolean(context, "RECEIVE_FACULTY_NEWS", true)) }
    var wrsEnabled by remember { mutableStateOf(PreferencesManager.getBoolean(context, "RECEIVE_WRS_NEWS", true)) }
    var otherEnabled by remember { mutableStateOf(PreferencesManager.getBoolean(context, "RECEIVE_OTHER_NEWS", true)) }

    LaunchedEffect(Unit) {
        PreferencesManager.saveBoolean(context, "STATUS_CHECKED", false)
        PreferencesManager.saveBoolean(context, "LET_IN", false)
    }

    TopBarScreen(
        navController = navController,
        title = "App Settings",
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(
                        UiConstants.TILE_PADDING,
                        innerPadding.calculateTopPadding(),
                        UiConstants.TILE_PADDING,
                        0.dp,
                    )
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(UiConstants.BIG_SPACE),
            ) {
                Spacer(modifier = Modifier)

                SettingsSection(title = "Notification Settings") {
                    SwitchSetting(
                        title = "Enable Notifications",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            PreferencesManager.saveBoolean(context, "NEWS_ALLOWED", it)
                            notificationsEnabled = it
                        },
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notification settings") }
                    )
                    AnimatedVisibility(visible = notificationsEnabled) {
                        Column {
                            SwitchSetting(
                                title = "Receive classes related news",
                                checked = classesEnabled,
                                onCheckedChange = {
                                    PreferencesManager.saveBoolean(context, "RECEIVE_CLASSES_NEWS", it)
                                    classesEnabled = it
                                }
                            )
                            SwitchSetting(
                                title = "Receive faculty announcements",
                                checked = announcementsEnabled,
                                onCheckedChange = {
                                    PreferencesManager.saveBoolean(context, "RECEIVE_FACULTY_NEWS", it)
                                    announcementsEnabled = it
                                }
                            )
                            SwitchSetting(
                                title = "Receive WRS news",
                                checked = wrsEnabled,
                                onCheckedChange = {
                                    PreferencesManager.saveBoolean(context, "RECEIVE_WRS_NEWS", it)
                                    wrsEnabled = it
                                }
                            )
                            SwitchSetting(
                                title = "Receive other news",
                                checked = otherEnabled,
                                onCheckedChange = {
                                    PreferencesManager.saveBoolean(context, "RECEIVE_OTHER_NEWS", it)
                                    otherEnabled = it
                                }
                            )
                        }
                    }
                }

                SettingsSection(title = "University services") {
                    ClickSetting(
                        title = "Manage ISOD account",
                        onClick = {
                            navController.navigate("link_isod")
                        },
                        icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Notification settings") }
                    )
                    ClickSetting(
                        title = "Manage USOS account",
                        onClick = {
                            navController.navigate("link_usos")
                        },
                        icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Notification settings") }
                    )
                }

                SettingsSection(title = "User") {
                    ClickSetting(
                        title = "Logout from all other devices",
                        onClick = {

                        },
                        icon = { Icon(Icons.Filled.Output, contentDescription = "Notification settings") }
                    )
                    ClickSetting(
                        title = "Delete all user data",
                        onClick = {

                        },
                        icon = { Icon(Icons.Filled.DeleteForever, contentDescription = "Notification settings") }
                    )
                }

                SettingsSection(title = "App info") {
                    ClickSetting(
                        title = "See app info",
                        onClick = {
                            navController.navigate("app_info")
                        },
                        icon = { Icon(Icons.Filled.Info, contentDescription = "Notification settings") }
                    )
                    ClickSetting(
                        title = "Visit app on Play Store",
                        onClick = {
                            openURL(context, context.getString(R.string.app_play_store_url))
                        },
                        icon = { Icon(Icons.Filled.Web, contentDescription = "Notification settings") }
                    )
                }

                Spacer(modifier = Modifier)
            }
        }
    )
}

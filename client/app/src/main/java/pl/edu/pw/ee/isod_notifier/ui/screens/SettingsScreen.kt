package pl.edu.pw.ee.isod_notifier.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ClickSetting
import pl.edu.pw.ee.isod_notifier.ui.common.SettingsSection
import pl.edu.pw.ee.isod_notifier.ui.common.SwitchSetting
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen

@Composable
fun SettingsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    var notificationsEnabled by remember { mutableStateOf(true) }
    var classesEnabled by remember { mutableStateOf(false) }
    var announcementsEnabled by remember { mutableStateOf(false) }
    var wrsEnabled by remember { mutableStateOf(false) }
    var otherEnabled by remember { mutableStateOf(false) }

    TopBarScreen(
        navController = navController,
        title = "App Settings",
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(
                        UiConstants.COMPOSABLE_PADDING,
                        innerPadding.calculateTopPadding(),
                        UiConstants.COMPOSABLE_PADDING,
                        0.dp,
                    )
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SettingsSection(title = "Notification Settings") {
                    SwitchSetting(
                        title = "Enable Notifications",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notification settings") }
                    )
                    AnimatedVisibility(visible = notificationsEnabled) {
                        Column {
                            SwitchSetting(
                                title = "Receive classes related news",
                                checked = classesEnabled,
                                onCheckedChange = { classesEnabled = it }
                            )
                            SwitchSetting(
                                title = "Receive faculty announcements",
                                checked = announcementsEnabled,
                                onCheckedChange = { announcementsEnabled = it }
                            )
                            SwitchSetting(
                                title = "Receive WRS news",
                                checked = wrsEnabled,
                                onCheckedChange = { wrsEnabled = it }
                            )
                            SwitchSetting(
                                title = "Receive other news",
                                checked = otherEnabled,
                                onCheckedChange = { otherEnabled = it }
                            )
                        }
                    }
                }

                SettingsSection(title = "Link service") {
                    ClickSetting(
                        title = "Link ISOD account",
                        onClick = {

                        },
                        icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Notification settings") }
                    )
                    ClickSetting(
                        title = "Link USOS account",
                        onClick = {

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

                        },
                        icon = { Icon(Icons.Filled.Info, contentDescription = "Notification settings") }
                    )
                    ClickSetting(
                        title = "Visit app on Play Store",
                        onClick = {

                        },
                        icon = { Icon(Icons.Filled.Web, contentDescription = "Notification settings") }
                    )
                }
            }
        }
    )
}

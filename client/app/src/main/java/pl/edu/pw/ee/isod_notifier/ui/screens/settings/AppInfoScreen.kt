package pl.edu.pw.ee.isod_notifier.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import pl.edu.pw.ee.isod_notifier.R
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ContentText
import pl.edu.pw.ee.isod_notifier.ui.common.OutlinedWideButton
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen
import pl.edu.pw.ee.isod_notifier.utils.openURL

@Composable
fun AppInfoScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    TopBarScreen(
        navController = navController,
        title = "App Info",
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
                verticalArrangement = Arrangement.spacedBy(UiConstants.BIG_SPACE * 2),
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 48.dp, 0.dp, 0.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.logo_sunny_filled),
                        contentDescription = "Notification settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = UiConstants.COMPOSABLE_PADDING)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ContentText("Made by:")
                        ContentText("Mikołaj Frączek, WRS EE")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ContentText("Support:")
                        ContentText(context.getString(R.string.support_mail))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ContentText("App version:")
                        ContentText("2.0.0")
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = UiConstants.COMPOSABLE_PADDING)
                ) {
                    OutlinedWideButton(
                        text = "Send mail",
                        onClick = {
                            openURL(context, "mailto:" + context.getString(R.string.support_mail))
                        }
                    )

                    OutlinedWideButton(
                        text = "Visit Facebook",
                        onClick = {
                            openURL(context, "https://www.facebook.com/wrs.ee")
                        }
                    )

                    OutlinedWideButton(
                        text = "Visit Instagram",
                        onClick = {
                            openURL(context, "https://www.instagram.com/wrs.elektryczny")
                        }
                    )
                }
            }
        }
    )
}
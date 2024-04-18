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
import pl.edu.pw.ee.isod_notifier.ui.common.TopBarScreen

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
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.logo_sunny_filled),
                        contentDescription = "Notification settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}
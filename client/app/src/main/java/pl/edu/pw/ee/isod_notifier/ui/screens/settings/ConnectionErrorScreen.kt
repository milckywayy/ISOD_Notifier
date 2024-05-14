package pl.edu.pw.ee.isod_notifier.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ContentText

@Composable
fun ConnectionErrorScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(horizontal = UiConstants.COMPOSABLE_PADDING * 3),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(UiConstants.DEFAULT_SPACE),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.WifiOff,
                contentDescription = "Notification settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(128.dp)
            )

            ContentText(
                "Could not connect to server.",
                padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING)
            )
        }

        Button(
            modifier = Modifier.padding(horizontal = UiConstants.COMPOSABLE_PADDING),
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("Retry")
        }
    }
}
package pl.edu.pw.ee.isod_notifier.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*

@Composable
fun LinkIsodScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    var username = remember { mutableStateOf("") }
    var apiKey = remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .verticalScroll(scrollState)
    ) {
        BigTitleText(
            "Link ISOD account",
            padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING, vertical = 32.dp)
        )

        Column {
            InfoBar(
                "Get ISOD API key. Click button below, log in to ISOD and scroll down in order to find the API key.",
                icon = Icons.Filled.LooksOne,
                paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            WideButton(
                text = "Get API key",
                padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                onClick = {

                }
            )
        }

        Column {
            InfoBar(
                "Fill both fields with your ISOD username (e.g \"kowalsj\") and API key from previous step.",
                icon = Icons.Filled.LooksTwo,
                paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                text = username.value,
                placeholder = "ISOD Username",
                padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                onValueChange = { newValue ->
                    username.value = newValue
                }
            )

            TextField(
                text = apiKey.value,
                placeholder = "ISOD API Key",
                padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                onValueChange = { newValue ->
                    apiKey.value = newValue
                }
            )
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
                    navController.popBackStack()
                }
            ) {
                Text("Link account")
            }
        }
    }
}
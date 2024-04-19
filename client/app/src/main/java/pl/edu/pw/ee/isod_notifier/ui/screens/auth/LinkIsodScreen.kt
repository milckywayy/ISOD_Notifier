package pl.edu.pw.ee.isod_notifier.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

    TopBarScreen(
    navController,
        "Link ISOD Account"
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ScreenContent(navController, innerPadding)
        }
    }
}

@Composable
private fun ScreenContent(navController: NavController, innerPadding: PaddingValues) {
    val username = remember { mutableStateOf("") }
    val apiKey = remember { mutableStateOf("") }

    Column (
        verticalArrangement = Arrangement.spacedBy(UiConstants.BIG_SPACE * 2),
        modifier = Modifier
            .padding(
                0.dp,
                innerPadding.calculateTopPadding() + (UiConstants.BIG_SPACE * 2),
                0.dp,
                0.dp
            )
    ) {
        Column {
            InfoBar(
                "Get ISOD API key. Click button below, log in to ISOD and scroll down in order to find the API key.",
                icon = Icons.Filled.LooksOne,
                paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
            )

            Spacer(modifier = Modifier.height(UiConstants.SMALL_SPACE))

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

            Spacer(modifier = Modifier.height(UiConstants.SMALL_SPACE))

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

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            WideButton(
                "Link account",
                onClick = {
                    navController.popBackStack()
                },
                padding = PaddingValues(
                    UiConstants.COMPOSABLE_PADDING,
                    0.dp,
                    UiConstants.COMPOSABLE_PADDING,
                    UiConstants.COMPOSABLE_PADDING
                )
            )
        }
    }
}
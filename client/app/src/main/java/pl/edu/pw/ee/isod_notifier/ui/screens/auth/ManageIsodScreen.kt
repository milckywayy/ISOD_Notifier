package pl.edu.pw.ee.isod_notifier.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*

@Composable
fun LinkIsodScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }
    var isAccountLinked by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        isLoading = true
        delay(1000L)
        isAccountLinked = false
        isLoading = false
    }

    TopBarScreen(
        navController,
        "Manage ISOD Account"
    ) { innerPadding ->
        if (isLoading) {
            LoadingAnimation()
        }
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (isAccountLinked) {
                    UnlinkScreenContent(navController, innerPadding)
                }
                else {
                    LinkScreenContent(navController, innerPadding)
                }
            }
        }
    }
}

@Composable
private fun LinkScreenContent(navController: NavController, innerPadding: PaddingValues) {
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

@Composable
private fun UnlinkScreenContent(navController: NavController, innerPadding: PaddingValues) {
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
                "Your ISOD account is currently linked. You can disconnect it from the app. Please note that you will need to have a USOS account linked to continue using the app after disconnecting.",
                icon = Icons.Filled.Info,
                paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
            )
        }

        WideButton(
            "Unlink account",
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
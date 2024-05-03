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
fun LinkUsosScreen(navController: NavController) {
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
        "Manage USOS Account"
    ) { innerPadding ->
        if (isLoading) {
            LoadingAnimation()
        }
        else {
            Column(
                verticalArrangement = Arrangement.Top,
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
    val authPin = remember { mutableStateOf("") }

    Column (
        verticalArrangement = Arrangement.spacedBy(UiConstants.BIG_SPACE * 2),
        modifier = Modifier
            .padding(
                0.dp,
                innerPadding.calculateTopPadding() + (UiConstants.BIG_SPACE * 2),
                0.dp,
                0.dp
            )
            .fillMaxSize()
    ) {
        Column {
            InfoBar(
                "Get USOS auth pin. Click button below, log in to USOS and accept app privileges in order to get the auth pin.",
                icon = Icons.Filled.LooksOne,
                paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
            )

            Spacer(modifier = Modifier.height(UiConstants.SMALL_SPACE))

            WideButton(
                text = "Authorize in USOS",
                padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                onClick = {

                }
            )
        }

        Column {
            InfoBar(
                "Fill field below with auth pin from previous step.",
                icon = Icons.Filled.LooksTwo,
                paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
            )

            Spacer(modifier = Modifier.height(UiConstants.SMALL_SPACE))

            TextField(
                text = authPin.value,
                placeholder = "USOS Pin",
                padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                onValueChange = { newValue ->
                    authPin.value = newValue
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
                "Your USOS account is currently linked. You can disconnect it from the app. Please note that you will need to have a ISOD account linked to continue using the app after disconnecting.",
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
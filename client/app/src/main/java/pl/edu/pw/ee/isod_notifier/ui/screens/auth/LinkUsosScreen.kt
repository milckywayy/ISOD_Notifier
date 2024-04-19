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
fun LinkUsosScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    TopBarScreen(
        navController,
        "Link USOS Account"
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Top,
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
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
import pl.edu.pw.ee.isod_notifier.ui.common.BigTitleText
import pl.edu.pw.ee.isod_notifier.ui.common.InfoBar
import pl.edu.pw.ee.isod_notifier.ui.common.TextField
import pl.edu.pw.ee.isod_notifier.ui.common.WideButton

@Composable
fun LinkUsosScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    var authPin = remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .verticalScroll(scrollState)
    ) {
        BigTitleText(
            "Link USOS account",
            padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING, vertical = 32.dp)
        )

        Column {
            InfoBar(
                "Get USOS auth pin. Click button below, log in to USOS and accept app permissions in order to get auth pin.",
                icon = Icons.Filled.LooksOne,
                paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
            )

            Spacer(modifier = Modifier.height(8.dp))

            WideButton(
                text = "Get auth pin",
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

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                text = authPin.value,
                placeholder = "USOS Auth Pin",
                padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                onValueChange = { newValue ->
                    authPin.value = newValue
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
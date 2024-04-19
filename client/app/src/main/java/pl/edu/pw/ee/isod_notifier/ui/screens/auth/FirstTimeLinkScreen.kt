package pl.edu.pw.ee.isod_notifier.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.edu.pw.ee.isod_notifier.R
import pl.edu.pw.ee.isod_notifier.model.ActivityItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.ActivityTile
import pl.edu.pw.ee.isod_notifier.ui.common.BigTitleText
import pl.edu.pw.ee.isod_notifier.ui.common.InfoBar

@Composable
fun FirstTimeLinkScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    val tiles = listOf(
        ActivityItem("ISOD", ImageVector.vectorResource(R.drawable.we_logo), MaterialTheme.colorScheme.primary, "link_isod"),
        ActivityItem("USOS", ImageVector.vectorResource(R.drawable.usos_logo), MaterialTheme.colorScheme.surface, "link_usos"),
    )

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .verticalScroll(scrollState)
    ) {
        BigTitleText(
            "Link service",
            padding = PaddingValues(
                horizontal = UiConstants.COMPOSABLE_PADDING,
                vertical = 32.dp
            )
        )

        Column {
            InfoBar(
                "In order to use app, please log in to at least one university service.",
                icon = Icons.Filled.Info,
                paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
            )

            Spacer(modifier = Modifier.height(UiConstants.DEFAULT_SPACE))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                items(tiles) { tile ->
                    Row(modifier = Modifier.padding(horizontal = UiConstants.SPACE_BTW_TILES / 2)) {
                        ActivityTile(tile, onClick = {
                            navController.navigate(tile.route)
                        })
                    }
                }
            }
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
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            ) {
                Text("Start")
            }
        }
    }
}
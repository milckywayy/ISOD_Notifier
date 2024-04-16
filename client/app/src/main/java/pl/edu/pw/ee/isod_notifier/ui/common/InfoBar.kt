package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pl.edu.pw.ee.isod_notifier.ui.UiConstants

@Composable
fun InfoBar(
    text: String,
    icon: ImageVector,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        modifier = Modifier.padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = "Info",
        )
        ContentText(
            text,
            padding = PaddingValues(12.dp, 0.dp, 0.dp, 0.dp)
        )
    }
}
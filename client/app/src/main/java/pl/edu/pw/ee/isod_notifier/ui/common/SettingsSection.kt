package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.edu.pw.ee.isod_notifier.ui.UiConstants

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(UiConstants.TILE_PADDING),
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionText(
            text = title,
            padding = PaddingValues(horizontal = UiConstants.TILE_PADDING)
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(UiConstants.CORNER_RADIUS))
                .background(MaterialTheme.colorScheme.surface)
                .padding(UiConstants.SMALL_SPACE)
        ) {
            content()
        }
    }
}

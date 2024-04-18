package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.edu.pw.ee.isod_notifier.ui.UiConstants

@Composable
fun ClickSetting(
    title: String,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(UiConstants.CORNER_RADIUS))
            .clickable(onClick = onClick)
            .padding(horizontal = UiConstants.TILE_PADDING),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon.invoke()
                Spacer(modifier = Modifier.width(UiConstants.TILE_PADDING))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                ContentText(
                    text = title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
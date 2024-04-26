package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import pl.edu.pw.ee.isod_notifier.model.ClassItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants


@Composable
fun ClassTile(classItem: ClassItem, onClick: (ClassItem) -> Unit) {
    Surface(
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(UiConstants.CORNER_RADIUS))
            .clickable { onClick(classItem) }
    ) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = UiConstants.TILE_PADDING)
                ) {
                    ContentText(
                        text = classItem.name,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SecondaryText(
                    text = classItem.type,
                )
            }
        }
    }
}
package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.edu.pw.ee.isod_notifier.model.CourseDetailsItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants

@Composable
fun FinalGradeTile(
    courseDetailsItem: CourseDetailsItem,
    modifier: Modifier = Modifier
) {
    Surface(
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(UiConstants.CORNER_RADIUS))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = UiConstants.COMPOSABLE_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Final grade",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = courseDetailsItem.finalGrade.ifEmpty { "∅" },
                color = MaterialTheme.colorScheme.background,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

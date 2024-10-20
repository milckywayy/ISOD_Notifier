package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.edu.pw.ee.isod_notifier.model.ScheduleClassItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants

@Composable
fun ScheduleClassCard(lesson: ScheduleClassItem) {
    Card(
        modifier = Modifier
            .padding(vertical = UiConstants.EXTRA_SMALL_SPACE)
            .fillMaxWidth(),
        shape = RoundedCornerShape(UiConstants.CORNER_RADIUS),
        colors = if (lesson.isActive) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary) else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(UiConstants.TILE_PADDING)) {
            BoldContentText(text = lesson.name)
            SmallContentText(text = "${lesson.typeOfClasses} - ${lesson.building} ${lesson.room}")
            SmallContentText(text = "${lesson.startTime} - ${lesson.endTime}")
            if (lesson.note.isNotEmpty()) {
                SecondaryText(text = "Note: ${lesson.note}")
            }
        }
    }
}
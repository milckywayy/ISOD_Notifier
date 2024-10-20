package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pl.edu.pw.ee.isod_notifier.model.ScheduleDayItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.screens.activities.generateTimeSlots

@Composable
fun ScheduleDayTable(day: ScheduleDayItem) {
    val timeSlots = generateTimeSlots()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UiConstants.TILE_PADDING)
    ) {
        timeSlots.forEachIndexed { index, timeSlot ->
            val nextTimeSlot = if (index < timeSlots.size - 1) timeSlots[index + 1] else "20:00"
            val lessonsInThisSlot = day.lessons.filter {
                it.startTime < nextTimeSlot && it.endTime > timeSlot
            }


            if (index > 0 && index < timeSlots.size) {
                HorizontalSpacer(
                    color = MaterialTheme.colorScheme.surface,
                    padding = PaddingValues(vertical = UiConstants.SMALL_SPACE)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContentText(
                    text = "$timeSlot \n$nextTimeSlot",
                    padding = PaddingValues(horizontal = UiConstants.DEFAULT_SPACE, vertical = UiConstants.EXTRA_SMALL_SPACE),
                )

                Column(modifier = Modifier.weight(2f)) {
                    if (lessonsInThisSlot.isNotEmpty()) {
                        lessonsInThisSlot.forEach { lesson ->
                            ScheduleClassCard(lesson)
                        }
                    }
                }
            }
        }
    }
}
package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.pw.ee.isod_notifier.model.GradeItem
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@Composable
fun GradeTile(
    gradeItem: GradeItem,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(UiConstants.CORNER_RADIUS),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = gradeItem.value.ifEmpty { "∅" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (gradeItem.accounted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(UiConstants.DEFAULT_SPACE))

            Text(
                text = buildAnnotatedString {
                    append(gradeItem.name)
                    if (gradeItem.date.isNotEmpty()) {
                        append("  ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append(gradeItem.date)
                        }
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )

            if (gradeItem.valueNote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(UiConstants.DEFAULT_SPACE))
                Text(
                    text = gradeItem.valueNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

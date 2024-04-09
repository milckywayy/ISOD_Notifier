package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.pw.ee.isod_notifier.model.ActivityItem

@Composable
fun ActivityTile(activityItem: ActivityItem, onClick: (ActivityItem) -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 150.dp, height = 200.dp)
            .clickable { onClick(activityItem) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = activityItem.color),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = activityItem.icon,
                contentDescription = activityItem.name,
                modifier = Modifier.size(88.dp),
            )
            ActivityTileText(activityItem.name)
        }
    }
}
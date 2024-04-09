package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import pl.edu.pw.ee.isod_notifier.model.NewsItem


@Composable
fun NewsTile(newsItem: NewsItem, onClick: (NewsItem) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 4.dp,
        color = Color.DarkGray,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick(newsItem) }
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = newsItem.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
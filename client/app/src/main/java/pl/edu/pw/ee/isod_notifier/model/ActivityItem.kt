package pl.edu.pw.ee.isod_notifier.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ActivityItem(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

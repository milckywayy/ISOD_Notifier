package pl.edu.pw.ee.isod_notifier.model

import androidx.compose.ui.graphics.Color
import pl.edu.pw.ee.isod_notifier.ui.theme.*

enum class NewsTypes(val displayName: String, val color: Color) {
    ALL("All news", ColorNewsAll),
    CLASSES("Classes news", ColorNewsClasses),
    FACULTY("Faculty announcements", ColorNewsFaculty),
    WRS("WRS news", ColorNewsWRS),
    OTHER("Other news", ColorNewsOther);
}
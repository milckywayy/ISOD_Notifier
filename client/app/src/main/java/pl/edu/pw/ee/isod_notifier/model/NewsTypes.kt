package pl.edu.pw.ee.isod_notifier.model

enum class NewsTypes(val displayName: String) {
    ALL("All news"),
    CLASSES("Classes news"),
    FACULTY("Faculty announcements"),
    WRS("WRS news"),
    OTHER("Other news")
}
package pl.edu.pw.ee.isod_notifier.model

data class FullNewsItem (
    val subject: String,
    val hash: String,
    val content: String,
    val date: String,
    val who: String,
)
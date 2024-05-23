package pl.edu.pw.ee.isod_notifier.model

class NewsItem (
    val subject: String,
    val hash: String,
    val service: String,
    val type: NewsTypes,
    val day: String,
    val hour: String,
)
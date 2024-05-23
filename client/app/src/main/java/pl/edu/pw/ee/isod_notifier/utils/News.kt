package pl.edu.pw.ee.isod_notifier.utils

import pl.edu.pw.ee.isod_notifier.model.NewsTypes

fun getNewsType(type: String): NewsTypes {
    return when (type) {
        "1000" -> NewsTypes.FACULTY
        "2414" -> NewsTypes.WRS
        "1001", "1002", "1003", "1004", "1005" -> NewsTypes.CLASSES
        else -> NewsTypes.OTHER
    }
}
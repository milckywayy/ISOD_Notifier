package pl.edu.pw.ee.isod_notifier

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pl.edu.pw.ee.isod_notifier.messaging.InAppNotificationManager
import pl.edu.pw.ee.isod_notifier.model.NewsTypes
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.getNewsType


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!PreferencesManager.getBoolean(applicationContext, "NEWS_ALLOWED", true)) {
            return
        }
        super.onMessageReceived(remoteMessage)

        val notificationHelper = InAppNotificationManager(this)

        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["body"]
        val rawNewsType = remoteMessage.data["news_type"]

        val newsType = rawNewsType?.let { getNewsType(it) }

        val data = remoteMessage.data.toMutableMap().apply {
            remove("title")
            remove("body")
        }

        if (!title.isNullOrEmpty() && !message.isNullOrEmpty()) {
            when (newsType) {
                NewsTypes.ALL, null -> notificationHelper.sendNotification(title, message, data)
                NewsTypes.CLASSES -> if (PreferencesManager.getBoolean(applicationContext, "RECEIVE_CLASSES_NEWS", true)) {
                    notificationHelper.sendNotification(title, message, data)
                }
                NewsTypes.FACULTY -> if (PreferencesManager.getBoolean(applicationContext, "RECEIVE_FACULTY_NEWS", true)) {
                    notificationHelper.sendNotification(title, message, data)
                }
                NewsTypes.WRS -> if (PreferencesManager.getBoolean(applicationContext, "RECEIVE_WRS_NEWS", true)) {
                    notificationHelper.sendNotification(title, message, data)
                }
                NewsTypes.OTHER -> if (PreferencesManager.getBoolean(applicationContext, "RECEIVE_OTHER_NEWS", true)) {
                    notificationHelper.sendNotification(title, message, data)
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
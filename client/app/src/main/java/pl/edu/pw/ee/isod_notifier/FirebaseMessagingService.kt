package pl.edu.pw.ee.isod_notifier

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pl.edu.pw.ee.isod_notifier.messaging.InAppNotificationManager
import pl.edu.pw.ee.isod_notifier.model.NewsItem
import pl.edu.pw.ee.isod_notifier.model.NewsTypes
import pl.edu.pw.ee.isod_notifier.utils.NotificationStorage
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.getNewsType

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!PreferencesManager.getBoolean(applicationContext, "NEWS_ALLOWED", true)) return

        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["body"]
        val newsHash = remoteMessage.data["news_hash"]
        val service = remoteMessage.data["service"]
        val newsType = remoteMessage.data["news_type"]?.let { getNewsType(it) }

        val data = remoteMessage.data.toMutableMap().apply {
            remove("title")
            remove("body")
        }

        if (!title.isNullOrEmpty() && !message.isNullOrEmpty()) {
            when (newsType) {
                NewsTypes.ALL, null -> sendNotification(title, message, newsHash, newsType, service, data)
                NewsTypes.CLASSES -> if (isNotificationAllowed("RECEIVE_CLASSES_NEWS")) sendNotification(title, message, newsHash, newsType, service, data)
                NewsTypes.FACULTY -> if (isNotificationAllowed("RECEIVE_FACULTY_NEWS")) sendNotification(title, message, newsHash, newsType, service, data)
                NewsTypes.WRS -> if (isNotificationAllowed("RECEIVE_WRS_NEWS")) sendNotification(title, message, newsHash, newsType, service, data)
                NewsTypes.OTHER -> if (isNotificationAllowed("RECEIVE_OTHER_NEWS")) sendNotification(title, message, newsHash, newsType, service, data)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun sendNotification(title: String, message: String, newsHash: String?, newsType: NewsTypes?, service: String?, data: Map<String, String>) {
        val notificationHelper = InAppNotificationManager(this)
        val notificationStorage = NotificationStorage(this)

        newsHash?.let {
            service?.let {
                newsType?.let { type ->
                    notificationStorage.saveNotification(NewsItem(message, newsHash, service, type, "", ""))
                }
            }
        }

        notificationHelper.sendNotification(title, message, data)
    }

    private fun isNotificationAllowed(preferenceKey: String) =
        PreferencesManager.getBoolean(applicationContext, preferenceKey, true)
}
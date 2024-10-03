package pl.edu.pw.ee.isod_notifier

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pl.edu.pw.ee.isod_notifier.messaging.InAppNotificationManager


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationHelper = InAppNotificationManager(this)

        val title = remoteMessage.notification?.title
        val message = remoteMessage.notification?.body
        val newsHash = remoteMessage.data["news_hash"]

        if (title != null && message != null && newsHash != null) {
            notificationHelper.sendNotification(title, message, newsHash)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
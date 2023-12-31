package pl.edu.pw.ee.isod_notifier

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Show notification while app is active
        super.onMessageReceived(remoteMessage)

        val notificationHelper = InAppNotificationManager(this)

        val title = remoteMessage.notification?.title
        val message = remoteMessage.notification?.body

        if (title != null && message != null) {
            notificationHelper.sendNotification(title, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
package pl.edu.pw.ee.isod_notifier

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pl.edu.pw.ee.isod_notifier.messaging.InAppNotificationManager
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!PreferencesManager.getBoolean(applicationContext, "NEWS_ALLOWED", true)) {
            return
        }
        super.onMessageReceived(remoteMessage)

        val notificationHelper = InAppNotificationManager(this)

        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["body"]

        val data = remoteMessage.data.toMutableMap().apply {
            remove("title")
            remove("body")
        }

        if (title != null && message != null) {
            notificationHelper.sendNotification(title, message, data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
package pl.edu.pw.ee.isod_notifier

import android.content.Context
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

        val preferences = getSharedPreferences(PreferencesManager.getPreferenceFileKey(), Context.MODE_PRIVATE)
        val isRunning = PreferencesManager.getPreferenceNoContext(preferences, "IS_RUNNING") == "1"

        if (isRunning) {
            PreferencesManager.setPreferenceNoContext(preferences, "TOKEN", token)
            val username = PreferencesManager.getPreferenceNoContext(preferences, "USERNAME")
            val api_key = PreferencesManager.getPreferenceNoContext(preferences, "API_KEY")
            val version = this.packageManager.getPackageInfo(this.packageName, 0).versionName

            if (username != null && api_key != null) {
                sendPostRequest(token, username, api_key, version) {result ->
                    val (statusCode, exception) = result

                    // Stop notification service if failed
                    if (statusCode != 200) {
                        PreferencesManager.setPreferenceNoContext(preferences, "IS_RUNNING", "")
                    }
                }
            }
        }
    }
}
package pl.edu.pw.ee.isod_notifier.messaging

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import pl.edu.pw.ee.isod_notifier.MainActivity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // Android 13
class InAppNotificationManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var inAppNotificationManager: InAppNotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val shadowApp = Shadows.shadowOf(context as Application)
        shadowApp.grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        inAppNotificationManager = InAppNotificationManager(context)
    }

    @Test
    fun `init creates notification channel`() {
        val channel = notificationManager.getNotificationChannel("isod_in_app_notifications")

        assertNotNull("Notification channel should be created", channel)
        assertEquals("ISOD In-App Notifications", channel.name)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
    }

    @Test
    fun `sendNotification posts notification with correct details and intent extras`() {
        val title = "Test Title"
        val message = "Test Message"
        val data = mapOf("key1" to "value1", "key2" to "value2")

        inAppNotificationManager.sendNotification(title, message, data)

        val shadowManager = Shadows.shadowOf(notificationManager)
        assertEquals("Should have posted 1 notification", 1, shadowManager.size())

        val notification = shadowManager.allNotifications[0]
        val shadowNotification = Shadows.shadowOf(notification)

        assertEquals("Title should match", title, shadowNotification.contentTitle)
        assertEquals("Message should match", message, shadowNotification.contentText)

        val pendingIntent = notification.contentIntent
        val intent = Shadows.shadowOf(pendingIntent).savedIntent

        assertEquals(MainActivity::class.java.name, intent.component?.className)
        assertEquals("value1", intent.getStringExtra("key1"))
        assertEquals("value2", intent.getStringExtra("key2"))
    }

    @Test
    fun `sendNotification does not post notification if permission denied on Android 13+`() {
        val shadowApp = Shadows.shadowOf(context as Application)
        shadowApp.denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        inAppNotificationManager.sendNotification("Title", "Message")

        val shadowManager = Shadows.shadowOf(notificationManager)
        assertEquals("Should not post notification without permission", 0, shadowManager.size())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // Android 12
    fun `sendNotification posts notification on older Android versions without runtime permission`() {
        inAppNotificationManager.sendNotification("Title", "Message")

        val shadowManager = Shadows.shadowOf(notificationManager)
        assertEquals("Should post notification on older SDKs", 1, shadowManager.size())
    }
}
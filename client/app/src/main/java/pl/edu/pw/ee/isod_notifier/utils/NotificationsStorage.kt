package pl.edu.pw.ee.isod_notifier.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.edu.pw.ee.isod_notifier.model.NewsItem

class NotificationStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val notificationsKey = "RECENT_NOTIFICATIONS_STACK"

    fun saveNotification(notification: NewsItem) {
        val currentList = getNotifications().toMutableList()

        currentList.add(0, notification)

        if (currentList.size > 5) {
            currentList.removeAt(currentList.size - 1)
        }

        val json = gson.toJson(currentList)
        prefs.edit().putString(notificationsKey, json).apply()
    }

    fun getNotifications(): List<NewsItem> {
        val json = prefs.getString(notificationsKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<NewsItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun clearNotifications() {
        prefs.edit().remove(notificationsKey).apply()
    }
}
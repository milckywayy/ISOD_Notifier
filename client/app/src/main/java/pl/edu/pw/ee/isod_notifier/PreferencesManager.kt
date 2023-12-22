package pl.edu.pw.ee.isod_notifier

import android.content.Context
import android.content.SharedPreferences


object PreferencesManager {
    private const val PREFERENCES_FILE_KEY = "pl.edu.pw.ee.isod_notifier.preferences"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
    }

    fun setPreference(context: Context, key: String, value: String) {
        getPreferences(context).edit().putString(key, value).apply()
    }

    fun getPreference(context: Context, key: String): String {
        return getPreferences(context).getString(key, "") ?: ""
    }

    fun getPreferenceFileKey() : String {
        return PREFERENCES_FILE_KEY
    }

    fun getPreferenceNoContext(sharedPref: SharedPreferences, key: String) : String? {
        return sharedPref.getString(PREFERENCES_FILE_KEY, key)
    }

    fun setPreferenceNoContext(sharedPref: SharedPreferences, key: String, value: String) {
        return sharedPref.edit().putString(key, value).apply()
    }
}
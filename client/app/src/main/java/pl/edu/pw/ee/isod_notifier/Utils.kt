package pl.edu.pw.ee.isod_notifier;

import android.content.Context;
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


fun showToast(context:Context, text: String) {
    MainScope().launch(Dispatchers.Main) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}

fun encodeFilter(context: Context) : Int {
    val filterClasses = if(PreferencesManager.getPreference(context, "FILTER_CLASSES") == "1") 1 else 0
    val filterAnnouncements = if(PreferencesManager.getPreference(context, "FILTER_ANNOUNCEMENTS") == "1") 1 else 0
    val filterWRS = if(PreferencesManager.getPreference(context, "FILTER_WRS") == "1") 1 else 0
    val filterOther = if(PreferencesManager.getPreference(context, "FILTER_OTHER") == "1") 1 else 0

    // Combines the individual filters into a single integer, where:
    // - filterClasses is bit 0 (the least significant bit)
    // - filterAnnouncements is bit 1
    // - filterWRS is bit 2
    // - filterOther is bit 3 (the most significant bit)

    var result = filterClasses
    result = result or (filterAnnouncements shl 1)
    result = result or (filterWRS shl 2)
    result = result or (filterOther shl 3)

    return result
}

fun sendEmail(context: Context, emailAddress: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.contact_choose_email_client)))
    } catch (ex: android.content.ActivityNotFoundException) {
        showToast(context, context.getString(R.string.error_email_client_not_found))
    }
}
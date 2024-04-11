package pl.edu.pw.ee.isod_notifier.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openURL(context: Context, url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
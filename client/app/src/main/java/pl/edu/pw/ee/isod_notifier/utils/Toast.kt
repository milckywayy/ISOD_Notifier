package pl.edu.pw.ee.isod_notifier.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(this, message, duration).show()
    }
}

package pl.edu.pw.ee.isod_notifier

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException


fun sendPostRequest(context : Context, callback: (Pair<Int, Exception?>) -> Unit) {
    val client = OkHttpClient()

    val token = PreferencesManager.getPreference(context, "TOKEN")
    val username = PreferencesManager.getPreference(context, "USERNAME")
    val api_key = PreferencesManager.getPreference(context, "API_KEY")
    val version = "1.0"

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val jsonString = "{\"token\" : \"$token\", \"username\" : \"$username\", \"api_key\" : \"$api_key\", \"version\" : \"$version\"}"
    val body = jsonString.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("http://192.168.1.101:8080/register")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                callback(Pair(response.code, null))
            } else {
                callback(Pair(response.code, IOException(response.body?.string())))
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            if (e is SocketTimeoutException) {
                callback(Pair(-1, IOException("Couldn't connect to server. Check your internet connection.")))
            } else {
                callback(Pair(-1, e))
            }
        }
    })
}

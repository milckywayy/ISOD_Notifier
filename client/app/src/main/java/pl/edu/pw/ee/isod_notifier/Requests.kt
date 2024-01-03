package pl.edu.pw.ee.isod_notifier

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


fun registerRequest(context: Context, token: String, username: String, api_key: String, version: String, language: String, onSuccess: (Response) -> Unit, onFailure: () -> Unit) {
    val client = getSslOkHttpClient(context)

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val jsonString = "{\"token\" : \"$token\", \"username\" : \"$username\", \"api_key\" : \"$api_key\", \"version\" : \"$version\", \"language\" : \"$language\"}"
    val body = jsonString.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(context.getString(R.string.server_url) + "/register")
        .post(body)
        .build()

    handleResponse(context, client, request, onSuccess, onFailure)
}

fun unregisterRequest(context: Context, token: String, username: String, onSuccess: (Response) -> Unit, onFailure: () -> Unit) {
    val client = getSslOkHttpClient(context)

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val jsonString = "{\"token\" : \"$token\", \"username\" : \"$username\"}"
    val body = jsonString.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(context.getString(R.string.server_url) + "/unregister")
        .post(body)
        .build()

    handleResponse(context, client, request, onSuccess, onFailure)
}

fun registrationStatusRequest(context: Context, token: String, version: String, onSuccess: (Response) -> Unit, onFailure: () -> Unit) {
    val client = getSslOkHttpClient(context)

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val jsonString = "{\"token\" : \"$token\", \"version\" : \"$version\"}"
    val body = jsonString.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(context.getString(R.string.server_url) + "/registration_status")
        .post(body)
        .build()

    handleResponse(context, client, request, onSuccess, onFailure)
}

fun handleResponse(context: Context, client: OkHttpClient, request: Request, onSuccess: (Response) -> Unit, onFailure: () -> Unit) {
    fun showToast(text: String) {
        MainScope().launch(Dispatchers.Main) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                onSuccess(response)
            }
            else {
                showToast("${context.getString(R.string.error)}: ${response.body?.string()}")
                onFailure()
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            showToast(context.getString(R.string.error_could_not_connect_to_server))
            onFailure()
        }
    })
}
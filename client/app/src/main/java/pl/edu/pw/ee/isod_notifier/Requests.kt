package pl.edu.pw.ee.isod_notifier

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


fun registerRequest(context: Context, token: String, username: String, apiKey: String, version: String, language: String, filter: Int, onSuccess: (Response) -> Unit, onFailure: () -> Unit) {
    val client = getSslOkHttpClient(context)

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val jsonString = "{\"token\" : \"$token\", \"username\" : \"$username\", \"api_key\" : \"$apiKey\", \"version\" : \"$version\", \"language\" : \"$language\", \"filter\" : \"${filter}\"}"
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
    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                onSuccess(response)
            }
            else {
                showToast(context, "${context.getString(R.string.error)}: ${response.body?.string()}")
                onFailure()
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            showToast(context, context.getString(R.string.error_could_not_connect_to_server))
            onFailure()
        }
    })
}
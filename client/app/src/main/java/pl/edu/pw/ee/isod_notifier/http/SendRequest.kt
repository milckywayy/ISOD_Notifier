package pl.edu.pw.ee.isod_notifier.http

import android.content.Context
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import pl.edu.pw.ee.isod_notifier.R
import java.io.IOException

fun sendRequest(
    context: Context,
    client: OkHttpClient,
    endpoint: String,
    map: Map<String, String>,
    onSuccess: (Response) -> Unit,
    onError: (Response) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val jsonString = Gson().toJson(map)
    val body = jsonString.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(context.getString(R.string.server_base_url) + endpoint)
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                onSuccess(response)
            }
            else {
                onError(response)
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            onFailure(e)
        }
    })
}
package pl.edu.pw.ee.isod_notifier.utils

import org.json.JSONObject

fun extractFieldFromResponse(responseString: String?, fieldName: String): String? {
    return responseString?.let { it ->
        JSONObject(it).optString(fieldName).takeIf { it.isNotEmpty() }
    }
}


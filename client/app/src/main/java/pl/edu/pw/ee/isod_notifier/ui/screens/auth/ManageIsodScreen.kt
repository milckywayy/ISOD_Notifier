package pl.edu.pw.ee.isod_notifier.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.R
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import pl.edu.pw.ee.isod_notifier.utils.openURL
import pl.edu.pw.ee.isod_notifier.utils.showToast
import java.util.*

@Composable
fun LinkIsodScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val scope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }
    var isAccountLinked by remember { mutableStateOf(false) }

    val userId = PreferencesManager.getString(context, "USER_ID", "")
    if (userId != "") {
        LaunchedEffect(true) {
            isLoading = true

            sendRequest(
                context,
                httpClient,
                "get_isod_link_status",
                mapOf(
                    "user_token" to userId,
                    "language" to "test"
                ),
                onSuccess = { response ->
                    val responseBodyString = response.body?.string()

                    isAccountLinked = extractFieldFromResponse(responseBodyString, "is_isod_linked").toBoolean()

                    isLoading = false
                },
                onError = { response ->
                    val responseBodyString = response.body?.string()

                    val message = extractFieldFromResponse(responseBodyString, "message")
                    context.showToast(message ?: "Error")

                    isLoading = false
                },
                onFailure = { _ ->
                    scope.launch {
                        navController.navigate("connection_error")
                    }

                    isLoading = false
                }
            )
        }
    }

    TopBarScreen(
        navController,
        "Manage ISOD Account"
    ) { innerPadding ->
        if (isLoading) {
            LoadingAnimation()
        }
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (isAccountLinked) {
                    UnlinkScreenContent(navController, httpClient, innerPadding)
                }
                else {
                    LinkScreenContent(navController, httpClient, innerPadding)
                }
            }
        }
    }
}

@Composable
private fun LinkScreenContent(navController: NavController, httpClient: OkHttpClient, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val username = remember { mutableStateOf("") }
    val apiKey = remember { mutableStateOf("") }

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val version = packageInfo.versionName

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(UiConstants.BIG_SPACE * 2),
            modifier = Modifier
                .padding(
                    0.dp,
                    innerPadding.calculateTopPadding() + (UiConstants.BIG_SPACE * 2),
                    0.dp,
                    0.dp
                )
        ) {
            Column {
                InfoBar(
                    "Get ISOD API key. Click button below, log in to ISOD and scroll down in order to find the API key.",
                    icon = Icons.Filled.LooksOne,
                    paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
                )

                Spacer(modifier = Modifier.height(UiConstants.SMALL_SPACE))

                WideButton(
                    text = "Get API key",
                    padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                    onClick = {
                        openURL(context, context.getString(R.string.isod_api_key_url))
                    }
                )
            }

            Column {
                InfoBar(
                    "Fill both fields with your ISOD username (e.g \"kowalsj\") and API key from previous step.",
                    icon = Icons.Filled.LooksTwo,
                    paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
                )

                Spacer(modifier = Modifier.height(UiConstants.SMALL_SPACE))

                TextField(
                    text = username.value,
                    placeholder = "ISOD Username",
                    padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                    onValueChange = { newValue ->
                        username.value = newValue
                    }
                )

                TextField(
                    text = apiKey.value,
                    placeholder = "ISOD API Key",
                    padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                    onValueChange = { newValue ->
                        apiKey.value = newValue
                    }
                )
            }

            WideButton(
                "Link account",
                onClick = {
                    isLoading = true

                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            isLoading = false
                            return@addOnCompleteListener
                        }
                        val token = task.result

                        sendRequest(
                            context,
                            httpClient,
                            "link_isod_account",
                            mapOf(
                                "token_fcm" to token,
                                "isod_username" to username.value.trim(),
                                "isod_api_key" to apiKey.value.trim(),
                                "app_version" to version,
                                "device_language" to Locale.getDefault().language,
                                "news_filter" to "15"
                            ),
                            onSuccess = { response ->
                                val responseBodyString = response.body?.string()

                                val userToken = extractFieldFromResponse(responseBodyString, "user_token").toString()
                                val firstname = extractFieldFromResponse(responseBodyString, "firstname").toString()

                                PreferencesManager.saveString(context, "USER_ID", userToken)
                                PreferencesManager.saveString(context, "FIRSTNAME", firstname)

                                scope.launch {
                                    navController.popBackStack()
                                }
                                isLoading = false
                            },
                            onError = { response ->
                                val responseBodyString = response.body?.string()

                                val message = extractFieldFromResponse(responseBodyString, "message")
                                context.showToast(message ?: "Error")

                                isLoading = false
                            },
                            onFailure = { _ ->
                                scope.launch {
                                    navController.navigate("connection_error")
                                }

                                isLoading = false
                            }
                        )
                    }
                },
                padding = PaddingValues(
                    UiConstants.COMPOSABLE_PADDING,
                    0.dp,
                    UiConstants.COMPOSABLE_PADDING,
                    UiConstants.COMPOSABLE_PADDING
                )
            )
        }

        if (isLoading) {
            LoadingAnimation(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun UnlinkScreenContent(navController: NavController, httpClient: OkHttpClient,  innerPadding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(UiConstants.BIG_SPACE * 2),
            modifier = Modifier
                .padding(
                    0.dp,
                    innerPadding.calculateTopPadding() + (UiConstants.BIG_SPACE * 2),
                    0.dp,
                    0.dp
                )
        ) {
            Column {
                InfoBar(
                    "Your ISOD account is currently linked. You can disconnect it from the app. Please note that you will need to have a USOS account linked to continue using the app after disconnecting.",
                    icon = Icons.Filled.Info,
                    paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
                )
            }

            WideButton(
                "Unlink account",
                onClick = {
                    val userToken = PreferencesManager.getString(context, "USER_ID", "")

                    isLoading = true
                    sendRequest(
                        context,
                        httpClient,
                        "unlink_isod_account",
                        mapOf(
                            "user_token" to userToken,
                            "device_language" to Locale.getDefault().language
                        ),
                        onSuccess = { _ ->
                            scope.launch {
                                navController.navigate("first_time_link_screen") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            }
                            isLoading = false
                        },
                        onError = { response ->
                            val responseBodyString = response.body?.string()

                            val message = extractFieldFromResponse(responseBodyString, "message")
                            context.showToast(message ?: "Error")

                            isLoading = false
                        },
                        onFailure = { _ ->
                            scope.launch {
                                navController.navigate("connection_error")
                            }

                            isLoading = false
                        }
                    )
                },
                padding = PaddingValues(
                    UiConstants.COMPOSABLE_PADDING,
                    0.dp,
                    UiConstants.COMPOSABLE_PADDING,
                    UiConstants.COMPOSABLE_PADDING
                )
            )
        }

        if (isLoading) {
            LoadingAnimation(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
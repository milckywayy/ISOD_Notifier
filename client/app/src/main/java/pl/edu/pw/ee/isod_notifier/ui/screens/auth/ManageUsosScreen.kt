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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import pl.edu.pw.ee.isod_notifier.R
import pl.edu.pw.ee.isod_notifier.http.getOkHttpClient
import pl.edu.pw.ee.isod_notifier.http.sendRequest
import pl.edu.pw.ee.isod_notifier.repository.UsosStatusRepository
import pl.edu.pw.ee.isod_notifier.ui.UiConstants
import pl.edu.pw.ee.isod_notifier.ui.common.*
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager
import pl.edu.pw.ee.isod_notifier.utils.extractFieldFromResponse
import pl.edu.pw.ee.isod_notifier.utils.openURL
import pl.edu.pw.ee.isod_notifier.utils.showToast
import java.util.*

@Composable
fun LinkUsosScreen(navController: NavController) {
    val context = LocalContext.current
    val httpClient = getOkHttpClient(context)
    val scope = rememberCoroutineScope()
    val repository = UsosStatusRepository(context, httpClient)

    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }
    var isAccountLinked by remember { mutableStateOf(false) }

    val userId = PreferencesManager.getString(context, "USER_ID", "")
    if (userId != "") {
        LaunchedEffect(true) {
            isLoading = true

            repository.fetchUsosLinkStatus(
                onSuccess = { isLinked ->
                    isAccountLinked = isLinked
                    isLoading = false
                },
                onError = { message ->
                    context.showToast(message ?: "Error")
                    isLoading = false
                },
                onFailure = {
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
        "Manage USOS Account"
    ) { innerPadding ->
        if (isLoading) {
            LoadingAnimation()
        }
        else {
            Column(
                verticalArrangement = Arrangement.Top,
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

    val authPin = remember { mutableStateOf("") }
    val requestToken = remember { mutableStateOf("") }

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
                .fillMaxSize()
        ) {
            Column {
                InfoBar(
                    "Get USOS auth pin. Click button below, log in to USOS and accept app privileges in order to get the auth pin.",
                    icon = Icons.Filled.LooksOne,
                    paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
                )

                Spacer(modifier = Modifier.height(UiConstants.SMALL_SPACE))

                WideButton(
                    text = "Authorize in USOS",
                    padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                    enabled = !isLoading,
                    onClick = {
                        isLoading = true
                        sendRequest(
                            context,
                            httpClient,
                            "get_usos_auth_url",
                            mapOf(
                                "language" to Locale.getDefault().language
                            ),
                            onSuccess = { response ->
                                val responseBodyString = response.body?.string()

                                requestToken.value = extractFieldFromResponse(responseBodyString, "request_token").toString()
                                val requestUrl = extractFieldFromResponse(responseBodyString, "request_url").toString()

                                openURL(context, requestUrl)

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
                )
            }

            Column {
                InfoBar(
                    "Fill field below with auth pin from previous step.",
                    icon = Icons.Filled.LooksTwo,
                    paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
                )

                Spacer(modifier = Modifier.height(UiConstants.SMALL_SPACE))

                TextField(
                    text = authPin.value,
                    placeholder = "USOS Pin",
                    enabled = !isLoading,
                    padding = PaddingValues(horizontal = UiConstants.COMPOSABLE_PADDING),
                    onValueChange = { newValue ->
                        authPin.value = newValue
                    }
                )
            }

            WideButton(
                "Link account",
                onClick = {
                    if (requestToken.value != "") {
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
                                "link_usos_account",
                                mapOf(
                                    "token_fcm" to token,
                                    "request_token" to requestToken.value,
                                    "request_pin" to authPin.value.trim(),
                                    "app_version" to version,
                                    "device_language" to Locale.getDefault().language,
                                    "news_filter" to PreferencesManager.getInteger(context, "NEWS_FILTER", 15).toString()
                                ),
                                onSuccess = { response ->
                                    val responseBodyString = response.body?.string()

                                    val userToken =
                                        extractFieldFromResponse(responseBodyString, "user_token").toString()
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
                    }
                    else {
                        context.showToast("Token not authorized is USOS")
                    }
                },
                enabled = !isLoading,
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
private fun UnlinkScreenContent(navController: NavController, httpClient: OkHttpClient, innerPadding: PaddingValues) {
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
                    "Your USOS account is currently linked. You can disconnect it from the app. Please note that you will need to have a ISOD account linked to continue using the app after disconnecting.",
                    icon = Icons.Filled.Info,
                    paddingValues = PaddingValues(horizontal = UiConstants.NARROW_PADDING)
                )
            }

            WideButton(
                "Unlink account",
                onClick = {
                    val userToken = PreferencesManager.getString(context, "USER_ID", "")
                    sendRequest(
                        context,
                        httpClient,
                        "unlink_usos_account",
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
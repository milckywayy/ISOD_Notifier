package pl.edu.pw.ee.isod_notifier

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.firebase.messaging.FirebaseMessaging
import pl.edu.pw.ee.isod_notifier.ui.theme.ISOD_NotifierTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras != null) {
            val url = intent.extras!!.getString("url")

            if (!url.isNullOrEmpty()) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        }

        setContent {
            ISOD_NotifierTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainContent()
                }
            }
        }
    }
}

@Preview
@Composable
fun MainContent() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isRefreshing by remember { mutableStateOf(false) }
    var showAppInfo by remember { mutableStateOf(false) }
    var showChangelog by remember { mutableStateOf(true) }
    var showPrivilegesDialog by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled().not()) }

    var isRunning by remember {
        mutableStateOf(PreferencesManager.getPreference(context, "IS_RUNNING") == "1")
    }

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val version = packageInfo.versionName

    if (showPrivilegesDialog) {
        PrivilegesPopup(
            context,
            onDismiss = {
                showPrivilegesDialog = false
            }
        )
    }

    if (showAppInfo) {
        InfoPopup(
            onDismiss = {
                showAppInfo = false
            },
            context.getString(R.string.app_info_title),
            arrayOf(
                context.getString(R.string.app_info_line1),
                context.getString(R.string.app_info_line2) + " $version"
            ),
            context.getString(R.string.app_info_dismiss_button_text),
            buttons = arrayOf(
                Pair(context.getString(R.string.app_info_visit_github_button_text)) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(context.getString(R.string.github_url))
                    context.startActivity(intent)
                }
            )
        )
    }

    if (PreferencesManager.getPreference(context, "APP_VERSION") != version && showChangelog) {
        if (PreferencesManager.getPreference(context, "APP_VERSION") == "") {
            // User's first installation. No need to show popup.
            showChangelog = false
            PreferencesManager.setPreference(context, "APP_VERSION", version)
        }
        else {
            // App has been updated, let's show popup.
            InfoPopup(
                onDismiss = {
                    showChangelog = false
                    PreferencesManager.setPreference(context, "APP_VERSION", version)
                },
                context.getString(R.string.whats_new_title),
                arrayOf(
                    "- " + context.getString(R.string.whats_new_line1),
                    "- " + context.getString(R.string.whats_new_line2),
                    "- " + context.getString(R.string.whats_new_line3),
                    "- " + context.getString(R.string.whats_new_line4)
                ),
                context.getString(R.string.whats_new_dismiss_button_text)
            )
        }
    }

    fun refreshApp() {
        registrationStatusCheck( context, version,
            onLaunch = {
                isRefreshing = true
            },
            onStateRunning = {
                isRunning = true
                PreferencesManager.setPreference(context, "IS_RUNNING", "1")
            },
            onStateStopped = {
                isRunning = false
                PreferencesManager.setPreference(context, "IS_RUNNING", "")
            },
            onFinish =  {
                isRefreshing = false
            }
        )
    }

    SwipeRefresh(
        swipeEnabled = true,
        state = SwipeRefreshState(isRefreshing),
        onRefresh = { refreshApp() }
    ) {
        LaunchedEffect(key1 = "refreshOnStart") { refreshApp() }

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))
            AppLogo(
                isRunning,
                if (isSystemInDarkTheme()) R.drawable.logo_sunny else R.drawable.logo_white,
                if (isSystemInDarkTheme()) R.drawable.logo_sunny_filled else R.drawable.logo_white_filled
            )
            Spacer(modifier = Modifier.height(52.dp))

            TextField(context, context.getString(R.string.username_field_text), "USERNAME", !isRunning)
            Spacer(modifier = Modifier.height(4.dp))
            TextField(context, context.getString(R.string.api_key_field_text), "API_KEY", !isRunning)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(context.getString(R.string.isod_api_url))
                        context.startActivity(intent)
                    },
                    context.getString(R.string.get_api_key_button_text)
                )
                Button(
                    onClick = { showAppInfo = true },
                    context.getString(R.string.see_app_info_button_text)
                )
            }
        }

        FloatingButton(
            onClick = {
                isRefreshing = true

                if (!isRunning) {
                    // Get token
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@addOnCompleteListener
                        }
                        val token = task.result
                        PreferencesManager.setPreference(context, "TOKEN", token)

                        // Register on server
                        registerRequest(
                            context,
                            token,
                            PreferencesManager.getPreference(context, "USERNAME").trim(),
                            PreferencesManager.getPreference(context, "API_KEY").trim(),
                            version,
                            onSuccess = {
                                isRunning = true
                                PreferencesManager.setPreference(context, "IS_RUNNING", "1")

                                isRefreshing = false
                            },
                            onFailure = {
                                isRefreshing = false
                            }
                        )
                    }
                }
                else {
                    unregisterRequest(
                        context,
                        PreferencesManager.getPreference(context, "TOKEN"),
                        PreferencesManager.getPreference(context, "USERNAME"),
                        onSuccess = {
                            isRunning = false
                            PreferencesManager.setPreference(context, "IS_RUNNING", "")
                            PreferencesManager.setPreference(context, "TOKEN", "")

                            isRefreshing = false
                        },
                        onFailure = {
                            isRefreshing = false
                        }
                    )
                }
            },
            if (isRunning) context.getString(R.string.service_button_running) else context.getString(R.string.service_button_stopped),
            enabled = !isRefreshing,
        )
    }
}

fun registrationStatusCheck(context: Context, version: String, onLaunch: () -> Unit, onStateRunning: () -> Unit, onStateStopped: () -> Unit, onFinish: () -> Unit) {
    val token = PreferencesManager.getPreference(context, "TOKEN")

    if (token == "") {
        return
    }

    onLaunch()

    registrationStatusRequest(context, token, version,
        onSuccess = {
            if (it.body.toString() == "User is unregistered." || it.code == 251) {
                onStateStopped()
            }
            else {
                onStateRunning()
            }
            onFinish()
        },
        onFailure = {
            onFinish()
        }
    )
}

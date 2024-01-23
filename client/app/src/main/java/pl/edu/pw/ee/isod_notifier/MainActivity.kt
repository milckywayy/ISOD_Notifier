package pl.edu.pw.ee.isod_notifier

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pl.edu.pw.ee.isod_notifier.ui.theme.ISOD_NotifierTheme
import java.util.*


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
    var showFilters by remember { mutableStateOf(false) }
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

    if (showFilters) {
        var filterClasses by remember {
            val prefValue = PreferencesManager.getPreference(context, "FILTER_CLASSES")
            mutableStateOf(prefValue == "1" || prefValue == "")
        }
        var filterAnnouncements by remember {
            val prefValue = PreferencesManager.getPreference(context, "FILTER_ANNOUNCEMENTS")
            mutableStateOf(prefValue == "1" || prefValue == "")
        }
        var filterWRS by remember {
            val prefValue = PreferencesManager.getPreference(context, "FILTER_WRS")
            mutableStateOf(prefValue == "1" || prefValue == "")
        }
        var filterOther by remember {
            val prefValue = PreferencesManager.getPreference(context, "FILTER_OTHER")
            mutableStateOf(prefValue == "1" || prefValue == "")
        }

        FilterPopup(
            onDismiss = {
                showFilters = false
                PreferencesManager.setPreference(context, "FILTER_CLASSES", if (filterClasses) "1" else "0")
                PreferencesManager.setPreference(context, "FILTER_ANNOUNCEMENTS", if (filterAnnouncements) "1" else "0")
                PreferencesManager.setPreference(context, "FILTER_WRS", if (filterWRS) "1" else "0")
                PreferencesManager.setPreference(context, "FILTER_OTHER", if (filterOther) "1" else "0")
            },
            context.getString(R.string.filters_title),
            content = {
                Text(context.getString(R.string.filters_info))

                Spacer(modifier = Modifier.height(16.dp))

                FilterCheckbox(filterClasses, context.getString(R.string.filters_checkbox_classes)) {
                    newValue -> filterClasses = newValue
                }
                FilterCheckbox(filterAnnouncements, context.getString(R.string.filters_checkbox_announcements)) {
                    newValue -> filterAnnouncements = newValue
                }
                FilterCheckbox(filterWRS, context.getString(R.string.filters_checkbox_wrs)) {
                    newValue -> filterWRS = newValue
                }
                FilterCheckbox(filterOther, context.getString(R.string.filters_checkbox_other)) {
                    newValue -> filterOther = newValue
                }
            },
            context.getString(R.string.filters_dismiss_button_text)
        )
    }

    if (PreferencesManager.getPreference(context, "APP_VERSION") != version && showChangelog) {
        if (PreferencesManager.getPreference(context, "APP_VERSION") == "") {
            // User's first installation. No need to show popup.
            showChangelog = false
            PreferencesManager.setPreference(context, "APP_VERSION", version)

            PreferencesManager.setPreference(context, "FILTER_CLASSES", "1")
            PreferencesManager.setPreference(context, "FILTER_ANNOUNCEMENTS", "1")
            PreferencesManager.setPreference(context, "FILTER_WRS", "1")
            PreferencesManager.setPreference(context, "FILTER_OTHER", "1")
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
                if (isSystemInDarkTheme()) R.drawable.logo_sunny else R.drawable.logo_graphite,
                if (isSystemInDarkTheme()) R.drawable.logo_sunny_filled else R.drawable.logo_graphite_filled
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

        StartAndFilterButtons(
            onClickService = {
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
                            Locale.getDefault().language,
                            encodeFilter(context),
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
                } else {
                    unregisterRequest(
                        context,
                        PreferencesManager.getPreference(context, "TOKEN"),
                        PreferencesManager.getPreference(context, "USERNAME").trim(),
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
            onClickFilter = {
                if (!isRunning && !isRefreshing) {
                    showFilters = true
                }
                else {
                    Toast.makeText(context, context.getString(R.string.filters_button_inactive_toast), Toast.LENGTH_SHORT).show()
                }
            },
            if (isRunning) context.getString(R.string.service_button_running) else context.getString(R.string.service_button_stopped),
            serviceButtonEnabled = !isRefreshing,
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
            if (it.code == 251) {
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

fun encodeFilter(context: Context) : Int {
    val filterClasses = if(PreferencesManager.getPreference(context, "FILTER_CLASSES") == "1") 1 else 0
    val filterAnnouncements = if(PreferencesManager.getPreference(context, "FILTER_ANNOUNCEMENTS") == "1") 1 else 0
    val filterWRS = if(PreferencesManager.getPreference(context, "FILTER_WRS") == "1") 1 else 0
    val filterOther = if(PreferencesManager.getPreference(context, "FILTER_OTHER") == "1") 1 else 0

    // Combines the individual filters into a single integer, where:
    // - filterClasses is bit 0 (the least significant bit)
    // - filterAnnouncements is bit 1
    // - filterWRS is bit 2
    // - filterOther is bit 3 (the most significant bit)

    var result = filterClasses
    result = result or (filterAnnouncements shl 1)
    result = result or (filterWRS shl 2)
    result = result or (filterOther shl 3)

    return result
}

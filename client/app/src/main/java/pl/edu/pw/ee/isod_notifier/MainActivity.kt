package pl.edu.pw.ee.isod_notifier

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pw.ee.isod_notifier.ui.theme.ISOD_NotifierTheme
import androidx.compose.runtime.*
import androidx.core.app.NotificationManagerCompat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    var showAppInfo by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showPrivilagesDialog by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled().not()) }

    var isRunning by remember {
        mutableStateOf(PreferencesManager.getPreference(context, "IS_RUNNING") == "1")
    }

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val version = packageInfo.versionName

    if (showPrivilagesDialog) {
        PrivilegesPopup(
            context,
            onDismiss = {
                showPrivilagesDialog = false
            }
        )
    }

    if (showAppInfo) {
        InfoPopup(
            onDismiss = {
                showAppInfo = false
            },
            "ISOD Notifier",
            arrayOf("Created by Mikołaj Frączek", "Version: $version")
        )
    }

    SwipeRefresh(
        swipeEnabled = false,
        state = SwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true

            kotlinx.coroutines.MainScope().launch {

                delay(2000)

                isRefreshing = false
            }
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))
            MainScreenLogo(
                if (isRunning) R.drawable.logo_sunny_filled else R.drawable.logo_sunny,
                if (isRunning) R.drawable.logo_white_filled else R.drawable.logo_white
            )
            Spacer(modifier = Modifier.height(52.dp))

            MainScreenTextField(context, "ISOD Username", "USERNAME", !isRunning)
            Spacer(modifier = Modifier.height(4.dp))
            MainScreenTextField(context, "ISOD API key", "API_KEY", !isRunning)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainScreenButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://isod.ee.pw.edu.pl/isod-stud/person")
                        context.startActivity(intent)
                    },
                    "Get API key"
                )
                MainScreenButton(
                    onClick = { showAppInfo = true },
                    "See app info"
                )
            }
        }

        MainScreenFloatingButton(
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
                    }

                    // Register on server
                    registerRequest(
                        PreferencesManager.getPreference(context, "TOKEN"),
                        PreferencesManager.getPreference(context, "USERNAME"),
                        PreferencesManager.getPreference(context, "API_KEY"),
                        version
                    ) { result ->
                        val (statusCode, exception) = result

                        // Handle success or failure
                        MainScope().launch(Dispatchers.Main) {
                            if (statusCode != 200) {
                                if (exception != null) {
                                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else {
                                isRunning = true
                                PreferencesManager.setPreference(context, "IS_RUNNING", "1")
                            }
                        }
                        isRefreshing = false
                    }
                }
                else {
                    unregisterRequest(
                        PreferencesManager.getPreference(context, "TOKEN"),
                    ) { result ->
                        val (statusCode, exception) = result

                        // Handle success or failure
                        MainScope().launch(Dispatchers.Main) {
                            if (statusCode != 200) {
                                if (exception != null) {
                                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else {
                                isRunning = false
                                PreferencesManager.setPreference(context, "IS_RUNNING", "")
                            }
                        }
                        isRefreshing = false
                    }
                }
            },
            if (isRunning) "Stop service" else "Start service",
            enabled = !isRefreshing,
        )
    }
}

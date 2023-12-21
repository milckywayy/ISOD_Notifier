package pl.edu.pw.ee.isod_notifier

import android.os.Bundle
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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Array


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
    var isRefreshing by remember { mutableStateOf(false) }
    var showAppInfo by remember { mutableStateOf(false) }

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val version = packageInfo.versionName

    if (showAppInfo) {
        AppInfoScreen(
            onDismiss = {
                showAppInfo = false
            },
            "ISOD Notifier info",
            arrayOf("Created by Mikołaj Frączek", "Version: $version")
        )
    }

    SwipeRefresh(
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
            MainScreenLogo()
            Spacer(modifier = Modifier.height(52.dp))

            MainScreenTextField(context, "ISOD Username", "USERNAME")
            Spacer(modifier = Modifier.height(4.dp))
            MainScreenTextField(context, "ISOD API key", "API_KEY")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainScreenButton(
                    onClick = { },
                    "Get API key"
                )
                MainScreenButton(
                    onClick = { showAppInfo = true },
                    "See app info"
                )
            }
        }

        MainScreenFloatingButton(
            onClick = { },
            "Start service"
        )
    }
}
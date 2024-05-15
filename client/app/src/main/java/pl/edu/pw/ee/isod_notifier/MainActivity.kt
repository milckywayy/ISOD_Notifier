package pl.edu.pw.ee.isod_notifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import pl.edu.pw.ee.isod_notifier.ui.screens.AppNavHost
import pl.edu.pw.ee.isod_notifier.ui.theme.ISOD_NotifierTheme
import pl.edu.pw.ee.isod_notifier.utils.PreferencesManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISOD_NotifierTheme {
                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current

                    PreferencesManager.saveBoolean(context, "STATUS_CHECKED", false)
                    PreferencesManager.saveBoolean(context, "LET_IN", false)

                    AppNavHost(navController)
                }
            }
        }
    }
}

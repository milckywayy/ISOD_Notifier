package pl.edu.pw.ee.isod_notifier

import android.content.Context
import android.health.connect.datatypes.AppInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun MainScreenLogo() {
    if (isSystemInDarkTheme()) {
        Image(
            painter = painterResource(id = R.drawable.logo_sunny),
            contentDescription = "ISOD Notifier logo"
        )
    }
    else {
        Image(
            painter = painterResource(id = R.drawable.logo_white),
            contentDescription = "ISOD Notifier logo"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenTextField(context: Context, placeholder: String, key: String) {
    val username = remember { mutableStateOf(PreferencesManager.getPreference(context, key)) }

    OutlinedTextField(
        value = username.value,
        onValueChange = { newValue ->
            username.value = newValue
            PreferencesManager.setPreference(context, key, username.value)
        },
        label = { Text(placeholder, color = MaterialTheme.colorScheme.primary) },
        maxLines = 1,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

@Composable
fun MainScreenButton(onClick: () -> Unit, text: String) {
    OutlinedButton(
        onClick = onClick,
    ) {
        Text(text = text)
    }
}

@Composable
fun MainScreenFloatingButton(onClick: () -> Unit, text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = onClick
        ) {
            Text(text = text, color = MaterialTheme.colorScheme.background)
        }
    }
}

@Composable
fun AppInfoScreen(onDismiss: () -> Unit, title: String, textLines: Array<String>) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(title) },
        text = {
            Column {
                for (line in textLines) {
                    Text(line)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.primary
    )
}
package pl.edu.pw.ee.isod_notifier

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList


@Composable
fun AppLogo(isRunning: Boolean, logoEmpty: Int, logoFilled: Int) {
    Crossfade(
        targetState = isRunning,
        label = "LogoTransition",
        animationSpec = tween(durationMillis = 450)
    ) { isRunningNow ->
        Image(
            painter = painterResource(if (isRunningNow) logoFilled else logoEmpty),
            contentDescription = "ISOD Notifier logo"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(context: Context, placeholder: String, key: String, enabled: Boolean) {
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        enabled = enabled
    )
}

@Composable
fun Button(onClick: () -> Unit, text: String) {
    OutlinedButton(
        onClick = onClick,
    ) {
        Text(text = text)
    }
}

@Composable
fun StartAndFilterButtons(onClickService: () -> Unit, onClickFilter: () -> Unit, floatingText: String, filterButtonEnabled: Boolean, serviceButtonEnabled: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row {
            IconButton(
                onClick = onClickFilter,
                enabled = filterButtonEnabled
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filter icon",
                )
            }

            Button(
                enabled = serviceButtonEnabled,
                onClick = onClickService,
                modifier = Modifier.width(160.dp)
            ) {
                Text(text = floatingText, color = MaterialTheme.colorScheme.background)
            }
        }
    }
}

@Composable
fun InfoPopup(onDismiss: () -> Unit, title: String, textLines: Array<String>, dismissButtonText: String, buttons: Array<Pair<String, () -> Unit>> = arrayOf()) {
    AlertDialog(
        title = { Text(title) },
        text = {
            Column {
                for (line in textLines) {
                    Text(line)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(dismissButtonText)
            }
        },
        dismissButton = {
            for (button in buttons) {
                TextButton(
                    onClick = button.second
                ) {
                    Text(button.first)
                }
            }
        },
        onDismissRequest = { onDismiss() },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun PrivilegesPopup(context: Context, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Enable Notifications") },
        text = {
            Column {
                Text("Notifications are disabled for this app. Please enable them in settings for the full functionality.")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                })
            }) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun FilterPopup(onDismiss: () -> Unit, title: String, content: @Composable () -> Unit, dismissButtonText: String) {
    AlertDialog(
        title = { Text(title) },
        text = {
            Column {
                content()
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(dismissButtonText)
            }
        },
        onDismissRequest = { onDismiss() },
        containerColor = MaterialTheme.colorScheme.background,
        textContentColor = MaterialTheme.colorScheme.secondary,
        titleContentColor = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun FilterCheckbox(state: Boolean, text: String, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = state, onCheckedChange = onCheckedChange)
        Text(text)
    }
}
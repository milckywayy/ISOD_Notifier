package pl.edu.pw.ee.isod_notifier

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay


@Composable
fun AppLogo(isRunning: Boolean,
            logoEmpty: Int, logoAnimationEmpty: Drawable?,
            logoFilled: Int, logoAnimationFilled: Drawable?,
            onLongPress: (Boolean) -> Unit) {

    val logoAnimationEmptyDrawable = remember { logoAnimationEmpty as AnimationDrawable }
    val logoAnimationFilledDrawable = remember { logoAnimationFilled as AnimationDrawable }
    var isGlitchEffect by remember { mutableStateOf(false) }
    var glitchCount by remember { mutableIntStateOf(1) }

    LaunchedEffect(isGlitchEffect, isRunning) {
        val animationDrawable = if (isRunning) logoAnimationFilledDrawable else logoAnimationEmptyDrawable

        if (isGlitchEffect) {
            animationDrawable.start()
            delay(650)
            animationDrawable.stop()
            isGlitchEffect = false
        }
    }

    Crossfade(
        targetState = isRunning,
        label = "LogoTransition",
        animationSpec = tween(durationMillis = 450),
    ) { isRunningNow ->
        Image(
            painter = if (isGlitchEffect) {
                rememberDrawablePainter(drawable = if (isRunningNow) logoAnimationEmptyDrawable else logoAnimationFilledDrawable)
            } else {
                painterResource(id = if (isRunningNow) logoFilled else logoEmpty)
            },
            contentDescription = "ISOD Notifier logo",
            modifier = Modifier.height(160.69.dp).pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (glitchCount < 3) {
                            glitchCount++
                            isGlitchEffect = true
                            onLongPress(false)
                        }
                        else if (glitchCount == 3) {
                            glitchCount++
                            onLongPress(true)
                            isGlitchEffect = true
                        }
                    }
                )
            }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
fun StartAndFilterButtons(onClickService: () -> Unit, onClickFilter: () -> Unit, floatingText: String, serviceButtonEnabled: Boolean, filterButtonEnabled: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row {
            IconButton(
                onClick = onClickFilter,
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filter icon",
                    tint =  if (filterButtonEnabled) MaterialTheme.colorScheme.onSurface // Kolor dla aktywnego przycisku
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // Kolor dla nieaktywnego przycisku
                )
            }

            Button(
                enabled = serviceButtonEnabled,
                onClick = onClickService,
                modifier = Modifier.width(170.dp)
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
        title = { Text(context.getString(R.string.privileges_info_title)) },
        text = {
            Column {
                Text(context.getString(R.string.privileges_info_text))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                })
            }) {
                Text(context.getString(R.string.privileges_info_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(context.getString(R.string.privileges_info_dismiss))
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

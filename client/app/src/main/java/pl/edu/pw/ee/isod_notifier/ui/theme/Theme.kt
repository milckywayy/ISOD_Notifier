package pl.edu.pw.ee.isod_notifier.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val DarkColorScheme = darkColorScheme(
    primary = Sunny,
    background = Graphite,
    tertiary = GraphiteDark,
    primaryContainer = Sunny
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = SamuraiBrightRed,
    secondary = SamuraiNeonBlue,
    background = SamuraiDarkBlue,
    surface = SamuraiDarkBlue,
    onPrimary = SamuraiDarkBlue,
    onSecondary = SamuraiDarkBlue,
    onBackground = White,
    onSurface = White,
    tertiary = SamuraiElectricPink,
    onTertiary = SamuraiDarkBlue,
    onError = White,
    primaryContainer = SamuraiBrightRed,
    secondaryContainer = SamuraiNeonBlue,
    tertiaryContainer = SamuraiElectricPink,
    onPrimaryContainer = SamuraiDarkBlue,
    onSecondaryContainer = SamuraiDarkBlue,
    onTertiaryContainer = SamuraiDarkBlue
)

@Composable
fun ISOD_NotifierTheme(themePref: String, content: @Composable () -> Unit
) {
    val colorScheme = when(themePref) {
        "DARK" -> DarkColorScheme
        "CYBERPUNK" -> CyberpunkColorScheme
        else -> DarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalSpacer(
    thickness: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.secondary,
    padding: PaddingValues = PaddingValues(0.dp),
) {
    HorizontalDivider(
        thickness = thickness,
        color = color,
        modifier = Modifier.padding(padding)
    )
}
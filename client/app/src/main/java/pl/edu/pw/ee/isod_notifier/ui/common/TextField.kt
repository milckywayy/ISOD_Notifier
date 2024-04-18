package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextField(
    text: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit,
    padding: PaddingValues = PaddingValues(0.dp),
) {
    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        label = { Text(text = placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
    )
}
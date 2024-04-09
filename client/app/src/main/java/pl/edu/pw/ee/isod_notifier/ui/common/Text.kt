package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.edu.pw.ee.isod_notifier.ui.theme.LexendFontFamily

@Composable
fun NavigationText(text: String) {
    Text(
        text,
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 8.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun BigTitleText(text: String) {
    Box {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            textAlign = TextAlign.Left,
        )
    }
}

@Composable
fun TitleText(text: String) {
    Box {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            textAlign = TextAlign.Left,
        )
    }
}

@Composable
fun ActivityTileText(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        textAlign = TextAlign.Left,
    )
}

@Composable
fun SectionText(text: String) {
    Box(
        modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Left
        )
    }
}

@Composable
fun SubsectionText(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        textAlign = TextAlign.Left,
    )
}

@Composable
fun ContentText(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        textAlign = TextAlign.Left,
    )
}

package pl.edu.pw.ee.isod_notifier.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationText(
    text: String,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier.padding(padding),
    ) {
        Text(
            text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            textAlign = TextAlign.Left
        )
    }
}

@Composable
fun BigTitleText(
    text: String,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier.padding(padding)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TitleText(
    text: String,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier.padding(padding)
    ) {
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
fun ActivityTileText(
    text: String,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier.padding(padding)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Left,
        )
    }
}

@Composable
fun SectionText(
    text: String,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier.padding(padding)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Left
        )
    }
}

@Composable
fun SubsectionText(
    text: String,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier.padding(padding)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Left
        )
    }
}

@Composable
fun ContentText(
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier.padding(padding)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            textAlign = TextAlign.Justify,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}

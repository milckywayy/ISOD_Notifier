package pl.edu.pw.ee.isod_notifier.ui.common

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import pl.edu.pw.ee.isod_notifier.utils.openURL

fun generateHtmlContent(htmlContent: String, textColorHex: String): String {
    return """
        <html>
        <head>
            <style>
                * {
                    background-color: transparent !important; 
                    color: $textColorHex !important; 
                    font-size: 0.9rem !important; 
                }
                body {
                    margin-left: 0;
                    margin-right: 0;
                }
                a {
                    color: lightblue !important;
                }
                img {
                    max-width: 100%;
                    height: auto;
                }
            </style>
        </head>
        <body>
            $htmlContent
        </body>
        </html>
    """.trimIndent()
}


@Composable
fun HtmlView(htmlContent: String) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val textColorHex = String.format("#%06X", 0xFFFFFF and textColor.toArgb())

    val styledHtmlContent = generateHtmlContent(htmlContent, textColorHex)

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        url?.let {
                            openURL(context, url)
                            return true
                        }
                        return false
                    }
                }

                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                loadDataWithBaseURL(null, styledHtmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, styledHtmlContent, "text/html", "UTF-8", null)
        }
    )
}

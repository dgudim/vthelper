package noorg.kloud.vthelper.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.HtmlStyle
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString

@Composable
fun HtmlCard(modifier: Modifier, html: String, borderColor: Color) {

    val linkColor = MaterialTheme.colorScheme.primary
    val convertedText = remember(html, linkColor) {
        htmlToAnnotatedString(
            html,
            style = HtmlStyle(
                textLinkStyles = TextLinkStyles(
                    style = SpanStyle(color = linkColor)
                )
            )
        )
    }

    Card(
        modifier = modifier,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = convertedText
            )
        }
    }
}
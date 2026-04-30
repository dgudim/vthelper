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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.HtmlStyle
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import noorg.kloud.vthelper.findFirstGroup
import noorg.kloud.vthelper.titleCase

val titleFindRegex = Regex("(<h[0-9]>.*?</h[0-9]>)")

@Composable
fun HtmlCard(
    modifier: Modifier,
    html: String,
    borderColor: Color
) {

    val titleHtml = titleFindRegex.findFirstGroup(html) ?: ""
    val contentHtml = html.replace(titleHtml, "")

    val linkColor = MaterialTheme.colorScheme.primary
    val convertedContentText = remember(contentHtml, linkColor) {
        htmlToAnnotatedString(
            contentHtml,
            style = HtmlStyle(
                textLinkStyles = TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        )
    }

    val convertedTitleText = remember(titleHtml, linkColor) {
        htmlToAnnotatedString(titleHtml)
    }

    ExpandableCard(
        modifier = modifier,
        border = BorderStroke(1.dp, borderColor),
        collapsedContent = {
            Text(
                text = convertedTitleText.text.titleCase(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1F)
                    .padding(8.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .weight(1F)
                .padding(8.dp)
        ) {
            Text(
                text = convertedContentText
            )
        }
    }
}
package noorg.kloud.vthelper.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun InfoField(
    icon: DrawableResource,
    topText: String,
    bottomText: String?,
    onClick: (() -> Unit)? = null,
    coloredIcon: Boolean = false
) {

    var bottomTextNn = bottomText ?: "-"
    if (bottomTextNn.isBlank()) {
        bottomTextNn = "-"
    }

    // https://youtrack.jetbrains.com/issue/CMP-7624
    @Suppress("DEPRECATION") val clipboard = LocalClipboardManager.current
    val onClickFinal = remember {
        {
            if (onClick != null) {
                onClick()
            } else {
                clipboard.setText(AnnotatedString(bottomTextNn))
            }
        }
    }

    Surface(
        onClick = onClickFinal,
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (coloredIcon) Color.Unspecified else LocalContentColor.current,
                modifier = Modifier.size(width = 60.dp, height = 24.dp)
            )
            Column {
                Text(
                    fontWeight = FontWeight.Bold,
                    text = topText
                )
                Text(
                    color = MaterialTheme.colorScheme.outline,
                    text = bottomTextNn
                )
            }
        }
    }

}
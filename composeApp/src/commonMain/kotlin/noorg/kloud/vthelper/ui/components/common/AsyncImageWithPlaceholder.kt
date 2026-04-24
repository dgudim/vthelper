package noorg.kloud.vthelper.ui.components.common

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.getValue


@Composable
fun AsyncImageWithPlaceholder(
    path: String?,
    placeholder: DrawableResource,
    shouldBeDisplayed: Boolean, size: Dp
) {

    val platformContext = LocalPlatformContext.current
    val imageLoader by remember(path) {
        lazy {
            ImageRequest.Builder(platformContext)
                .data(path)
                .crossfade(true)
                .build()
        }
    }

    if (shouldBeDisplayed && path?.isNotBlank() == true) {
        AsyncImage(
            model = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .height(size)
                .clip(RoundedCornerShape(10))
        )
    } else {
        Icon(
            painter = painterResource(placeholder),
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    }
}
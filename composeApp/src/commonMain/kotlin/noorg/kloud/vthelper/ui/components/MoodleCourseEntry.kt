package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.allowConversionToBitmap
import coil3.request.crossfade
import io.ktor.http.Url
import noorg.kloud.vthelper.mixWith
import kotlin.getValue

@Composable
fun CourseEntry(
    courseName: String,
    courseDescription: String,
    courseColor: Color,
    viewUrl: String,
    courseCoverImagePath: String
) {

    val uriHandler = LocalUriHandler.current
    val platformContext = LocalPlatformContext.current

    val mixedColor = courseColor.mixWith(MaterialTheme.colorScheme.primary, 0.3F)

    val courseImageLoader by remember(courseCoverImagePath) {
        lazy {
            ImageRequest.Builder(platformContext)
                .data(courseCoverImagePath)
                .crossfade(true)
                .build()
        }
    }

    // https://proandroiddev.com/coil-my-favorite-image-loading-library-for-jetpack-compose-877fa0b818fe

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .padding(top = 8.dp)
            .clip(CardDefaults.shape)
            .clickable {
                uriHandler.openUri(viewUrl)
            },
        border = BorderStroke(1.dp, mixedColor)
    ) {
        Row {
            AsyncImage(
                model = courseImageLoader,
                modifier = Modifier.width(164.dp).fillMaxHeight(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            VerticalDivider(
                thickness = 2.dp,
                color = mixedColor
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                Text(
                    text = courseName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalDivider(modifier = Modifier.padding(4.dp))
                Text(courseDescription)
            }
        }
    }
}
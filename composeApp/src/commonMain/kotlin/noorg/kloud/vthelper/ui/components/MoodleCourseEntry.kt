package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.allowConversionToBitmap
import coil3.request.crossfade
import io.ktor.http.Url
import kotlinx.datetime.Month
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import noorg.kloud.vthelper.mixWith
import noorg.kloud.vthelper.mixedWithPrimary
import noorg.kloud.vthelper.ui.theme.customColors
import kotlin.getValue

@Composable
fun CourseEntry(
    course: ProvidedMoodleCourseEntity
) {

    val uriHandler = LocalUriHandler.current
    val platformContext = LocalPlatformContext.current

    val mixedColor = course.color.mixedWithPrimary()

    val courseImageLoader by remember(course.coverImagePath) {
        lazy {
            ImageRequest.Builder(platformContext)
                .data(course.coverImagePath)
                .crossfade(true)
                .build()
        }
    }

    // https://proandroiddev.com/coil-my-favorite-image-loading-library-for-jetpack-compose-877fa0b818fe

    val indicatorColor =
        if (course.isFromCurrentSemester)
            MaterialTheme.customColors.goodResult
        else
            MaterialTheme.customColors.okResult

    val indicatorText =
        if (course.isFromCurrentSemester)
            "Current"
        else
            "Past"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .padding(top = 8.dp)
            .clip(CardDefaults.shape)
            .clickable {
                uriHandler.openUri(course.viewUrl) // TODO: Spawn a confirmation dialog
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
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalDivider(modifier = Modifier.padding(4.dp))
                Text(course.description)
                Spacer(modifier = Modifier.weight(1F))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = indicatorText,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1F).padding(end = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )
                }
            }
        }
    }
}
package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Month

@Composable
fun CourseEntry(
    courseName: String,
    courseDescription: String,
    courseLecturer: String,
    courseColor: Color,
    courseCoverImage: Painter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .padding(top = 8.dp),
        border = BorderStroke(1.dp, courseColor)
    ) {
        Row {
            Image(
                painter = courseCoverImage,
                modifier = Modifier.width(164.dp).fillMaxHeight(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            VerticalDivider(
                modifier = Modifier.padding(end = 8.dp),
                thickness = 3.dp,
                color = courseColor
            )
            Column(modifier = Modifier.fillMaxHeight().padding(top = 4.dp)) {
                Text(
                    text = courseName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalDivider(modifier = Modifier.padding(8.dp))
                Text(courseDescription)
            }
        }
    }
}
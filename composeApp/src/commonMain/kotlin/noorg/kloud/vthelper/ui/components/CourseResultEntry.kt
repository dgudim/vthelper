package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import noorg.kloud.vthelper.platform_specific.formatLocalDate
import noorg.kloud.vthelper.ui.theme.customColors
import kotlin.time.Instant

@Composable
@Stable
private fun resultToColor(result: Long): Color {
    if (result < 6) {
        return MaterialTheme.customColors.badResult
    }

    if (result < 8) {
        return MaterialTheme.customColors.okResult
    }

    return MaterialTheme.customColors.goodResult
}

@Composable
fun CourseResultEntry(
    subjectName: String,
    subjectColor: Color,
    workName: String,
    completedOn: Instant,
    result: Long // TODO: Include course points
) {
    val formattedDate = remember {
        completedOn.formatLocalDate()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {

                Text(
                    fontWeight = FontWeight.Bold,
                    text = "$workName ($formattedDate)",
                )
                Text(
                    color = MaterialTheme.colorScheme.outline,
                    text = subjectName
                )
            }

            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                VerticalDivider(thickness = 6.dp, color = subjectColor)
                Text(
                    text = "$result",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.displaySmall,
                    color = resultToColor(result)
                )
            }
        }
    }


}
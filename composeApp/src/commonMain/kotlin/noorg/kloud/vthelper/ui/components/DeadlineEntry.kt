package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Month
import noorg.kloud.vthelper.ui.theme.customColors
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.assignment_late_24px
import vthelper.composeapp.generated.resources.check_24px
import vthelper.composeapp.generated.resources.circle_24px
import vthelper.composeapp.generated.resources.circle_filled_24px
import kotlin.time.Instant

@Composable
@Stable
private fun daysToColor(days: Int): Color {
    if (days < 2) {
        return MaterialTheme.customColors.badResult
    }

    if (days < 7) {
        return MaterialTheme.customColors.okResult
    }

    return MaterialTheme.customColors.goodResult
}

// TODO: Put inside a lazy column
@Composable
fun DeadlineEntry(
    subjectName: String,
    subjectColor: Color,
    deadlineName: String,
    daysTillDeadline: Int,
    isDone: Boolean
) {

    val strikethroughIfDone =
        if (isDone)
            LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough)
        else
            LocalTextStyle.current

    Card(
        modifier = Modifier.padding(top = 8.dp),
        border = BorderStroke(1.dp, subjectColor),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Icon(
                painter = painterResource(if (isDone) Res.drawable.check_24px else Res.drawable.assignment_late_24px),
                contentDescription = null,
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    Text(
                        text = "In $daysTillDeadline day(s)",
                        style = strikethroughIfDone
                    )
                    Text(
                        text = if (isDone) " Done" else ""
                    )
                    Icon(
                        painter = painterResource(if (isDone) Res.drawable.circle_24px else Res.drawable.circle_filled_24px),
                        contentDescription = null,
                        tint = daysToColor(daysTillDeadline),
                        modifier = Modifier.padding(
                            end = 2.dp,
                            top = 2.dp,
                            bottom = 2.dp,
                            start = 4.dp
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                ) {
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = deadlineName,
                        style = strikethroughIfDone
                    )
                    Text(
                        color = MaterialTheme.colorScheme.outline,
                        text = subjectName
                    )
                }
            }
        }
    }
}
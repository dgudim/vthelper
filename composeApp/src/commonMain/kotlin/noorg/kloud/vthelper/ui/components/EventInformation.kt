package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import noorg.kloud.vthelper.data.local_models.LocalCalendarEvent
import noorg.kloud.vthelper.data.local_models.LocalCalendarEventType
import noorg.kloud.vthelper.mixedWithPrimary
import noorg.kloud.vthelper.platform_specific.formatLocalTime
import noorg.kloud.vthelper.ui.theme.customColors
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.assignment_late_24px
import vthelper.composeapp.generated.resources.book_24px
import vthelper.composeapp.generated.resources.circle_24px
import vthelper.composeapp.generated.resources.circle_filled_24px
import vthelper.composeapp.generated.resources.grading_24px
import vthelper.composeapp.generated.resources.info_24px
import vthelper.composeapp.generated.resources.person_add_24px
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

enum class EventInformationDisplayMode {
    ABSOLUTE, RELATIVE
}

@Composable
@Stable
private fun daysToColor(days: Long): Color {
    if (days < 2) {
        return MaterialTheme.customColors.badResult
    }

    if (days < 7) {
        return MaterialTheme.customColors.okResult
    }

    return MaterialTheme.customColors.goodResult
}

@Composable
fun EventInformation(
    event: LocalCalendarEvent,
    displayMode: EventInformationDisplayMode
) {

    val subtext = event.getSubtext()

    val nowSecUtc = remember { Clock.System.now().toEpochMilliseconds() / 1000 }

    val formattedStart = remember { event.startLocalDt.formatLocalTime() }

    val formattedTimeSpan = remember(event.startTime, event.endTime) {
        if (displayMode == EventInformationDisplayMode.RELATIVE) {
            val diffDays = (event.startTime.epochSeconds - nowSecUtc).seconds.inWholeDays.coerceIn(0, 365)
            if (diffDays == 0L) {
                return@remember "Tomorrow at $formattedStart"
            }
            if (diffDays == 1L) {
                return@remember "In a day at $formattedStart"
            }
            return@remember "In $diffDays days at $formattedStart"
        }
        if (event.startLocalDt == event.endLocalDt) {
            return@remember formattedStart
        }
        return@remember "$formattedStart - ${event.endLocalDt.formatLocalTime()}"
    }

    val indicatorColor = when (displayMode) {
        EventInformationDisplayMode.ABSOLUTE -> Color.Transparent
        EventInformationDisplayMode.RELATIVE -> daysToColor((event.startTime.epochSeconds - nowSecUtc).seconds.inWholeDays)
    }

    // https://developer.android.com/develop/ui/compose/layouts/intrinsic-measurements
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = 8.dp, start = 8.dp, end = 8.dp),
        border = BorderStroke(1.dp, event.getColor().mixedWithPrimary()),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1F)
                    .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(
                        when (event.eventType) {
                            LocalCalendarEventType.TIMETABLE -> Res.drawable.book_24px
                            LocalCalendarEventType.ANNOUNCEMENT -> Res.drawable.info_24px
                            LocalCalendarEventType.ASSIGNMENT -> Res.drawable.assignment_late_24px
                            LocalCalendarEventType.EXAM -> Res.drawable.grading_24px
                            LocalCalendarEventType.ATTENDANCE -> Res.drawable.person_add_24px
                            LocalCalendarEventType.OTHER -> Res.drawable.circle_24px
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1F)
                ) {
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = event.title
                    )
                    Text(
                        color = MaterialTheme.colorScheme.outline,
                        text = event.description
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Top)
                ) {
                    Text(
                        text = formattedTimeSpan
                    )
                    if (displayMode == EventInformationDisplayMode.RELATIVE) {
                        Icon(
                            painter = painterResource(Res.drawable.circle_24px),
                            contentDescription = null,
                            tint = indicatorColor,
                            modifier = Modifier
                                .padding(
                                    top = 2.dp,
                                    start = 4.dp
                                )
                        )
                    }
                }
            }

            if (!subtext.isBlank()) {
                HorizontalDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.info_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        color = MaterialTheme.colorScheme.outline,
                        text = subtext,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }

}
package noorg.kloud.vthelper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.now
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import noorg.kloud.vthelper.data.local_models.LocalCalendarEvent
import noorg.kloud.vthelper.mixedWithPrimary
import noorg.kloud.vthelper.next
import noorg.kloud.vthelper.platform_specific.displayText
import noorg.kloud.vthelper.previous
import noorg.kloud.vthelper.rememberFirstMostVisibleMonth
import noorg.kloud.vthelper.setAlpha
import noorg.kloud.vthelper.ui.theme.ColorVariants
import noorg.kloud.vthelper.ui.theme.calendarColors
import noorg.kloud.vthelper.ui.view_models.CalendarViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.keyboard_arrow_left_24px
import vthelper.composeapp.generated.resources.keyboard_arrow_right_24px

fun getEventsOnDay(day: LocalDate, events: Sequence<LocalCalendarEvent>): List<LocalCalendarEvent> {
    return events.filter { it.isVisibleOnDate(day) }.toList()
}

@Composable
fun Calendar(
    calendarViewModel: CalendarViewModel,
    isLoading: Boolean
) {

    val currentDate = remember { LocalDate.now() }

    val startMonth = remember { currentDate.yearMonth.minusMonths(500) }
    val endMonth = remember { currentDate.yearMonth.plusMonths(500) }
    var selection by remember { mutableStateOf(currentDate) }
    val daysOfWeek = remember { daysOfWeek() }

    val events by calendarViewModel.events.collectAsStateWithLifecycle()

    val eventsInSelectedDate by remember {
        derivedStateOf { getEventsOnDay(selection, events) }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val state = rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentDate.yearMonth,
            firstDayOfWeek = daysOfWeek.first(),
            outDateStyle = OutDateStyle.EndOfGrid,
        )
        val localScope = rememberCoroutineScope()

        val visibleMonth = rememberFirstMostVisibleMonth(state)

        CalendarTitle(
            modifier = Modifier
                .background(MaterialTheme.calendarColors.toolbarColor)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            currentMonth = visibleMonth.yearMonth,
            goToPrevious = {
                localScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previous)
                }
            },
            goToNext = {
                localScope.launch {
                    state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.next)
                }
            },
            isLoading = isLoading
        )
        HorizontalCalendar(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            dayContent = { day ->
                val colors = getEventsOnDay(day.date, events).map {
                    it.getColor()
                        .mixedWithPrimary()
                        .setAlpha(if (day.position == DayPosition.MonthDate) 1.0F else 0.25F)
                }
                DayView(
                    day = day,
                    isSelected = selection == day.date,
                    todayDate = currentDate,
                    eventColors = colors,
                ) { clicked ->
                    selection = clicked.date
                }
            },
            monthHeader = {
                MonthHeader(
                    modifier = Modifier.padding(vertical = 8.dp),
                    daysOfWeek = daysOfWeek,
                )
            },
        )
        HorizontalDivider(color = MaterialTheme.calendarColors.calendarDividerColor)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 12.dp, end = 12.dp)
        ) {
            items(items = eventsInSelectedDate) { event ->
                EventInformation(
                    modifier = Modifier.padding(top = 8.dp),
                    event,
                    EventInformationDisplayMode.ABSOLUTE
                )
            }
        }
    }
}

@Composable
private fun getColorByType(
    colorVariants: ColorVariants,
    day: CalendarDay,
    todayDate: LocalDate,
    isSelected: Boolean
): Color {
    return when (day.position) {
        DayPosition.MonthDate -> {
            if (isSelected) {
                return colorVariants.selected
            }

            if (day.date == todayDate) {
                return colorVariants.today
            }

            colorVariants.deselected
        }

        DayPosition.InDate, DayPosition.OutDate -> {
            if (isSelected) {
                return colorVariants.selected
            }

            colorVariants.disabled
        }
    }
}

@Composable
private fun DayView(
    day: CalendarDay,
    isSelected: Boolean,
    todayDate: LocalDate,
    eventColors: List<Color>,
    onClick: (CalendarDay) -> Unit,
) {
    val rounding = RoundedCornerShape(10)
    val bgColor = getColorByType(
        MaterialTheme.calendarColors.bgColor,
        day,
        todayDate,
        isSelected
    )
    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square-sizing!
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = getColorByType(
                    MaterialTheme.calendarColors.borderColor,
                    day,
                    todayDate,
                    isSelected
                ),
                shape = rounding
            )
            .padding(1.dp)
            .background(
                color = bgColor,
                shape = rounding
            )
            .clickable(
                onClick = { onClick(day) },
            ),
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            for (color in eventColors.take(6)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(start = 1.dp, end = 1.dp)
                        .background(
                            color = color,
                            shape = CircleShape
                        )
                )
            }
        }
        Text(
            modifier = Modifier.align(Alignment.TopCenter),
            text = "${day.date.day}",
            color = getColorByType(
                MaterialTheme.calendarColors.textColor,
                day,
                todayDate,
                isSelected
            ),
            fontWeight = if (isSelected || day.date == todayDate) FontWeight.Bold else null,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MonthHeader(
    modifier: Modifier = Modifier,
    daysOfWeek: List<DayOfWeek> = emptyList(),
) {
    Row(modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.calendarColors.monthHeaderTextColor,
                text = dayOfWeek.displayText(uppercase = true),
                fontWeight = FontWeight.Light,
            )
        }
    }
}


@Composable
fun CalendarTitle(
    modifier: Modifier,
    currentMonth: YearMonth,
    goToPrevious: () -> Unit,
    goToNext: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarNavigationIcon(
            res = Res.drawable.keyboard_arrow_left_24px,
            contentDescription = "Previous",
            onClick = goToPrevious
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .testTag("MonthTitle"),
            text = currentMonth.displayText(),
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
        CalendarNavigationIcon(
            res = Res.drawable.keyboard_arrow_right_24px,
            contentDescription = "Next",
            onClick = goToNext
        )
    }
    if (isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun CalendarNavigationIcon(
    res: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(1f)
        .clip(shape = CircleShape)
        .clickable(onClick = onClick),
) {
    Icon(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .align(Alignment.Center),
        painter = painterResource(res),
        contentDescription = contentDescription,
    )
}

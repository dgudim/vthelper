package noorg.kloud.vthelper.ui.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.derivedStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import com.kizitonwose.calendar.compose.rememberCalendarState
//import com.kizitonwose.calendar.core.CalendarDay
//import com.kizitonwose.calendar.core.daysOfWeek
//import com.kizitonwose.calendar.core.minusMonths
//import com.kizitonwose.calendar.core.now
//import com.kizitonwose.calendar.core.plusMonths
//import kotlinx.datetime.YearMonth
//
//@Composable
//fun Calendar(close: () -> Unit = {}) {
//    val currentMonth = remember { YearMonth.now() }
//    val startMonth = remember { currentMonth.minusMonths(500) }
//    val endMonth = remember { currentMonth.plusMonths(500) }
//    var selection by remember { mutableStateOf<CalendarDay?>(null) }
//    val daysOfWeek = remember { daysOfWeek() }
//    val flightsInSelectedDate = remember {
//        derivedStateOf {
//            val date = selection?.date
//            if (date == null) emptyList() else flights[date].orEmpty()
//        }
//    }
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(pageBackgroundColor)
//            .applyScaffoldHorizontalPaddings()
//            .applyScaffoldBottomPadding(),
//    ) {
//        val state = rememberCalendarState(
//            startMonth = startMonth,
//            endMonth = endMonth,
//            firstVisibleMonth = currentMonth,
//            firstDayOfWeek = daysOfWeek.first(),
//            outDateStyle = OutDateStyle.EndOfGrid,
//        )
//        val coroutineScope = rememberCoroutineScope()
//        val visibleMonth = rememberFirstCompletelyVisibleMonth(state)
//        LaunchedEffect(visibleMonth) {
//            // Clear selection if we scroll to a new month.
//            selection = null
//        }
//
//        // Draw light content on dark background.
//        CompositionLocalProvider(LocalContentColor provides Color.White) {
//            SimpleCalendarTitle(
//                modifier = Modifier
//                    .background(toolbarColor)
//                    .padding(horizontal = 8.dp, vertical = 12.dp)
//                    .applyScaffoldTopPadding(),
//                currentMonth = visibleMonth.yearMonth,
//                goToPrevious = {
//                    coroutineScope.launch {
//                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previous)
//                    }
//                },
//                goToNext = {
//                    coroutineScope.launch {
//                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.next)
//                    }
//                },
//            )
//            HorizontalCalendar(
//                modifier = Modifier.wrapContentWidth(),
//                state = state,
//                dayContent = { day ->
////                    CompositionLocalProvider(LocalRippleConfiguration provides Example3RippleConfiguration) {
//                    val colors = if (day.position == DayPosition.MonthDate) {
//                        flights[day.date].orEmpty().map { it.color }
//                    } else {
//                        emptyList()
//                    }
//                    Day(
//                        day = day,
//                        isSelected = selection == day,
//                        colors = colors,
//                    ) { clicked ->
//                        selection = clicked
//                    }
////                     }
//                },
//                monthHeader = {
//                    MonthHeader(
//                        modifier = Modifier.padding(vertical = 8.dp),
//                        daysOfWeek = daysOfWeek,
//                    )
//                },
//            )
//            HorizontalDivider(color = pageBackgroundColor)
//            LazyColumn(modifier = Modifier.fillMaxWidth()) {
//                items(items = flightsInSelectedDate.value) { flight ->
//                    FlightInformation(flight)
//                }
//            }
//            if (!isMobile()) {
//                Spacer(Modifier.height(28.dp))
//                Button(
//                    onClick = close,
//                    colors = ButtonDefaults.buttonColors().copy(containerColor = toolbarColor),
//                    modifier = Modifier.align(Alignment.CenterHorizontally),
//                ) {
//                    Text("Close")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun Day(
//    day: CalendarDay,
//    isSelected: Boolean = false,
//    colors: List<Color> = emptyList(),
//    onClick: (CalendarDay) -> Unit = {},
//) {
//    Box(
//        modifier = Modifier
//            .aspectRatio(1f) // This is important for square-sizing!
//            .border(
//                width = if (isSelected) 1.dp else 0.dp,
//                color = if (isSelected) selectedItemColor else Color.Transparent,
//            )
//            .padding(1.dp)
//            .background(color = itemBackgroundColor)
//            // Disable clicks on inDates/outDates
//            .clickable(
//                enabled = day.position == DayPosition.MonthDate,
//                onClick = { onClick(day) },
//            ),
//    ) {
//        val textColor = when (day.position) {
//            DayPosition.MonthDate -> Color.Unspecified
//            DayPosition.InDate, DayPosition.OutDate -> inActiveTextColor
//        }
//        Text(
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(top = 3.dp, end = 4.dp),
//            text = day.date.dayOfMonth.toString(),
//            color = textColor,
//            fontSize = 12.sp,
//        )
//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .fillMaxWidth()
//                .padding(bottom = 8.dp),
//            verticalArrangement = Arrangement.spacedBy(6.dp),
//        ) {
//            for (color in colors) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(5.dp)
//                        .background(color),
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun MonthHeader(
//    modifier: Modifier = Modifier,
//    daysOfWeek: List<DayOfWeek> = emptyList(),
//) {
//    Row(modifier.fillMaxWidth()) {
//        for (dayOfWeek in daysOfWeek) {
//            Text(
//                modifier = Modifier.weight(1f),
//                textAlign = TextAlign.Center,
//                fontSize = 12.sp,
//                color = Color.White,
//                text = dayOfWeek.displayText(uppercase = true),
//                fontWeight = FontWeight.Light,
//            )
//        }
//    }
//}
//
//@Composable
//private fun LazyItemScope.FlightInformation(flight: Flight) {
//    Row(
//        modifier = Modifier
//            .fillParentMaxWidth()
//            .height(IntrinsicSize.Max),
//        horizontalArrangement = Arrangement.spacedBy(2.dp),
//    ) {
//        Box(
//            modifier = Modifier
//                .background(color = flight.color)
//                .fillParentMaxWidth(1 / 7f)
//                .aspectRatio(1f),
//            contentAlignment = Alignment.Center,
//        ) {
//            Text(
//                text = flightDateTimeFormatter.format(flight.time).uppercase(),
//                textAlign = TextAlign.Center,
//                lineHeight = 17.sp,
//                fontSize = 12.sp,
//            )
//        }
//        Box(
//            modifier = Modifier
//                .background(color = itemBackgroundColor)
//                .weight(1f)
//                .fillMaxHeight(),
//        ) {
//            AirportInformation(flight.departure, isDeparture = true)
//        }
//        Box(
//            modifier = Modifier
//                .background(color = itemBackgroundColor)
//                .weight(1f)
//                .fillMaxHeight(),
//        ) {
//            AirportInformation(flight.destination, isDeparture = false)
//        }
//    }
//    HorizontalDivider(thickness = 2.dp, color = pageBackgroundColor)
//}
//
//@Composable
//private fun AirportInformation(airport: Flight.Airport, isDeparture: Boolean) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .fillMaxHeight(),
//    ) {
//        val vector = if (isDeparture) Icons.AirplaneTakeoff else Icons.AirplaneLanding
//        Box(
//            modifier = Modifier
//                .weight(0.3f)
//                .fillMaxHeight()
//                .fillMaxHeight(),
//            contentAlignment = Alignment.CenterEnd,
//        ) {
//            Image(imageVector = vector, contentDescription = null)
//        }
//        Column(
//            modifier = Modifier
//                .weight(0.7f)
//                .fillMaxHeight()
//                .fillMaxWidth(),
//            verticalArrangement = Arrangement.Center,
//        ) {
//            Text(
//                modifier = Modifier.fillMaxWidth(),
//                text = airport.code,
//                textAlign = TextAlign.Center,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Black,
//            )
//            Text(
//                modifier = Modifier.fillMaxWidth(),
//                text = airport.city,
//                textAlign = TextAlign.Center,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Light,
//            )
//        }
//    }
//}
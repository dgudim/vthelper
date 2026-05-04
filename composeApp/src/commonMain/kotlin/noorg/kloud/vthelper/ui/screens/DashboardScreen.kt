package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.map
import kotlinx.datetime.daysUntil
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.api.models.combine
import noorg.kloud.vthelper.data.local_models.LocalCalendarEventType
import noorg.kloud.vthelper.platform_specific.toSystemLocalDt
import noorg.kloud.vthelper.ui.components.EventInformation
import noorg.kloud.vthelper.ui.components.EventInformationDisplayMode
import noorg.kloud.vthelper.ui.components.common.HtmlCard
import noorg.kloud.vthelper.ui.components.common.ListSectionWithHeader
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderTextWithLoader
import noorg.kloud.vthelper.ui.components.common.SmartFetcher
import noorg.kloud.vthelper.ui.theme.customColors
import noorg.kloud.vthelper.ui.view_models.CalendarViewModel
import noorg.kloud.vthelper.ui.view_models.LoggedInUserAndInternetViewModel
import noorg.kloud.vthelper.ui.view_models.ManoCalloutsViewModel
import noorg.kloud.vthelper.ui.view_models.MoodleCoursesViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

// https://stackoverflow.com/questions/78323263/how-to-display-bars-in-different-colors-in-vico-bar-charts

@Composable
fun DashboardScreen(
    manoCalloutsViewModel: ManoCalloutsViewModel,
    loggedInUserAndInternetViewModel: LoggedInUserAndInternetViewModel,
    calendarViewModel: CalendarViewModel,
    moodleCoursesViewModel: MoodleCoursesViewModel,
    showSnack: SnackbarFun
) {

    val modelProducer = remember { CartesianChartModelProducer() }

    val userState by loggedInUserAndInternetViewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(userState.isSessionValid) {
        modelProducer.runTransaction {
            lineSeries { series(8, 9, 8, 10, 9, 7, 10, 9, 8, 8, 9, 10) }
        }
    }

    val calendarLoadingState = remember { mutableStateOf(false) }
    val isCalendarLoading by calendarLoadingState

    SmartFetcher(
        loggedInUserAndInternetViewModel,
        calendarLoadingState
    ) {
        listOf(
            calendarViewModel.fetchMoodleEvents(showSnack),
            calendarViewModel.fetchManoExams(showSnack),
            moodleCoursesViewModel.fetchCourses(showSnack),
        ).awaitAll().combine()
    }

//    val colorScheme = MaterialTheme.colorScheme
//    val vico = vicoTheme

    val callouts by manoCalloutsViewModel.callouts.collectAsStateWithLifecycle()

    val now = remember { Clock.System.now().toSystemLocalDt() }
    val deadlinesAndExams by calendarViewModel.events
        .map {
            it.filter { event ->
                event.eventType in listOf(
                    LocalCalendarEventType.ASSIGNMENT,
                    LocalCalendarEventType.EXAM
                ) && now.date.daysUntil(event.startLocalDt.date) in 0..7
            }
        }.collectAsStateWithLifecycle(listOf())

    val timetableEvents by calendarViewModel.events
        .map {
            it.filter { event ->
                event.eventType in listOf(
                    LocalCalendarEventType.TIMETABLE
                ) && now.date.daysUntil(event.startLocalDt.date) in 0..1
            }
        }.collectAsStateWithLifecycle(listOf())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LoadableListSection(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            loggedInUserAndInternetViewModel = loggedInUserAndInternetViewModel,
            items = callouts,
            fetchFunction = {
                manoCalloutsViewModel.fetchAllCallouts(showSnack).await()
            },
            header = { isLoading ->
                ScreenHeaderTextWithLoader("Announcements", isLoading)
            },
            displayDirectly = true,
            scroll = false
        ) { callout ->
            val color = when {
                callout.type.contains("info") -> {
                    MaterialTheme.colorScheme.primary
                }

                callout.type.contains("warn") -> {
                    MaterialTheme.customColors.okResult
                }

                callout.type.contains("success") -> {
                    MaterialTheme.customColors.goodResult
                }

                else -> MaterialTheme.colorScheme.outline
            }

            HtmlCard(
                Modifier.padding(top = 6.dp),
                html = callout.contents,
                borderColor = color
            )
        }

//        Text(
//            text = "Your performance over time",
//            style = MaterialTheme.typography.titleLarge
//        )
//        ProvideVicoTheme(
//            remember {
//                vico.copy(
//                    lineColor = colorScheme.outline,
//                    textColor = colorScheme.onSurface,
//                    lineCartesianLayerColors = listOf(colorScheme.primary),
//                )
//            }
//        ) {
//            CartesianChartHost(
//                chart = rememberCartesianChart(
//                    rememberLineCartesianLayer(),
//                    startAxis = VerticalAxis.rememberStart(),
//                    bottomAxis = HorizontalAxis.rememberBottom(),
//                ),
//                modelProducer = modelProducer,
//                zoomState = VicoZoomState(
//                    zoomEnabled = false,
//                    initialZoom = Zoom.fixed(1f),
//                    minZoom = Zoom.fixed(1f),
//                    maxZoom = Zoom.fixed(1f),
//                )
//            )
//        }

        ListSectionWithHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 8.dp),
            items = timetableEvents,
            isLoading = isCalendarLoading,
            displayDirectly = true,
            scroll = false,
            header = { isLoading ->
                ScreenHeaderTextWithLoader("Timetable for today and tomorrow", isLoading)
            }
        ) {
            EventInformation(
                modifier = Modifier.padding(top = 8.dp),
                it,
                EventInformationDisplayMode.RELATIVE
            )
        }

        ListSectionWithHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 8.dp),
            items = deadlinesAndExams,
            isLoading = isCalendarLoading,
            displayDirectly = true,
            scroll = false,
            header = { isLoading ->
                ScreenHeaderTextWithLoader("Upcoming deadlines and exams", isLoading)
            }
        ) {
            EventInformation(
                modifier = Modifier.padding(top = 8.dp),
                it,
                EventInformationDisplayMode.RELATIVE
            )
        }
    }
}
package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme.CandlestickCartesianLayerColors
import com.patrykandpatrick.vico.compose.common.vicoTheme
import kotlinx.coroutines.joinAll
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.DeadlineEntry
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import noorg.kloud.vthelper.ui.components.common.HtmlCard
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderTextWithLoader
import noorg.kloud.vthelper.ui.theme.customColors
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel
import noorg.kloud.vthelper.ui.view_models.ManoCalloutsViewModel
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

// https://stackoverflow.com/questions/78323263/how-to-display-bars-in-different-colors-in-vico-bar-charts

@Composable
fun DashboardScreen(
    manoCalloutsViewModel: ManoCalloutsViewModel,
    loggedInUserViewModel: LoggedInUserViewModel,
    showSnack: SnackbarFun
) {

    val modelProducer = remember { CartesianChartModelProducer() }

    val userState by loggedInUserViewModel.userState.collectAsStateWithLifecycle()

    LaunchedEffect(userState.isSessionValid) {
        modelProducer.runTransaction {
            lineSeries { series(8, 9, 8, 10, 9, 7, 10, 9, 8, 8, 9, 10) }
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val vico = vicoTheme

    val callouts by manoCalloutsViewModel.callouts.collectAsStateWithLifecycle()

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
            loggedInUserViewModel = loggedInUserViewModel,
            items = callouts,
            fetchFunction = {
                manoCalloutsViewModel.fetchAllCallouts(showSnack).join()
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

        HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp))
        Text(
            text = "Your performance over time",
            style = MaterialTheme.typography.titleLarge
        )
        ProvideVicoTheme(
            remember {
                vico.copy(
                    lineColor = colorScheme.outline,
                    textColor = colorScheme.onSurface,
                    lineCartesianLayerColors = listOf(colorScheme.primary),
                )
            }
        ) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(),
                ),
                modelProducer = modelProducer,
                zoomState = VicoZoomState(
                    zoomEnabled = false,
                    initialZoom = Zoom.fixed(1f),
                    minZoom = Zoom.fixed(1f),
                    maxZoom = Zoom.fixed(1f),
                )
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp))
        Text(
            text = "Timetable for today and tomorrow",
            style = MaterialTheme.typography.titleLarge
        )
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp))
        Text(
            text = "Upcoming deadlines",
            style = MaterialTheme.typography.titleLarge
        )
        Column {
            DeadlineEntry(
                "Fundamentals of data mining", Color(0xff7dc9ff), "Lab 5",
                1,
                false
            )
            DeadlineEntry(
                "Datacenters", Color(0xFFA57DFF), "Midterm 1",
                2,
                false
            )
            DeadlineEntry(
                "Fundamentals of data mining", Color(0xff7dc9ff), "Midterm 1",
                3,
                false
            )
            DeadlineEntry(
                "Information security management", Color(0xffffbe7d), "Homework 2",
                4,
                true
            )
        }
    }
}
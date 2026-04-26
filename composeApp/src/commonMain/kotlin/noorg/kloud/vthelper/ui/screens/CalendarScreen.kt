package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.joinAll
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.Calendar
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import noorg.kloud.vthelper.ui.theme.CalendarColorPalette
import noorg.kloud.vthelper.ui.theme.ColorVariants
import noorg.kloud.vthelper.ui.theme.LocalCalendarColorPalette
import noorg.kloud.vthelper.ui.view_models.CalendarViewModel
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel
import noorg.kloud.vthelper.ui.view_models.MoodleCoursesViewModel

@Composable
fun CalendarScreen(
    loggedInUserViewModel: LoggedInUserViewModel,
    calendarViewModel: CalendarViewModel,
    moodleCoursesViewModel: MoodleCoursesViewModel,
    showSnack: SnackbarFun,
) {

    val userState by loggedInUserViewModel.userState.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(userState.isSessionValid) {
        if (!userState.isSessionValid) {
            return@LaunchedEffect
        }
        isLoading = true
        listOf(
            calendarViewModel.fetchMoodleEvents(showSnack),
            moodleCoursesViewModel.fetchLatestCourseListFromApi(showSnack)
        ).joinAll()
        isLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val calendarColors = CalendarColorPalette(
            toolbarColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            listItemBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,

            calendarDividerColor = MaterialTheme.colorScheme.outline,
            monthHeaderTextColor = MaterialTheme.colorScheme.secondary,

            textColor = ColorVariants(
                selected = MaterialTheme.colorScheme.primary,
                deselected = MaterialTheme.colorScheme.onBackground,
                disabled = MaterialTheme.colorScheme.outline,
                today = MaterialTheme.colorScheme.tertiary
            ),

            borderColor = ColorVariants(
                selected = MaterialTheme.colorScheme.primary,
                deselected = Color.Transparent,
                disabled = Color.Transparent,
                today = MaterialTheme.colorScheme.outlineVariant
            ),

            bgColor = ColorVariants(
                selected = MaterialTheme.colorScheme.surfaceContainerHighest,
                deselected = Color.Transparent,
                disabled = Color.Transparent,
                today = Color.Transparent
            ),
        )

        CompositionLocalProvider(
            LocalCalendarColorPalette provides calendarColors
        ) {
            Calendar(calendarViewModel, isLoading)
        }
    }
}
package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.joinAll
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.api.models.combine
import noorg.kloud.vthelper.ui.components.CourseEntry
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderTextWithLoader
import noorg.kloud.vthelper.ui.view_models.LoggedInUserAndInternetViewModel
import noorg.kloud.vthelper.ui.view_models.ManoSemesterAndSubjectViewModel
import noorg.kloud.vthelper.ui.view_models.MoodleCoursesViewModel

@Composable
fun MoodleCoursesScreen(
    loggedInUserAndInternetViewModel: LoggedInUserAndInternetViewModel,
    moodleCoursesViewModel: MoodleCoursesViewModel,
    manoSemesterAndSubjectViewModel: ManoSemesterAndSubjectViewModel,
    showSnack: SnackbarFun
) {

    val courses by moodleCoursesViewModel.courses.collectAsStateWithLifecycle()

    LoadableListSection(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp),
        loggedInUserAndInternetViewModel = loggedInUserAndInternetViewModel,
        items = courses,
        fetchFunction = {
            listOf(
                moodleCoursesViewModel.fetchLatestCourseListFromApi(showSnack).await(),
                manoSemesterAndSubjectViewModel.fetchCurrentSemesterFromApi(showSnack).await()
            ).combine()
        },
        header = { isLoading ->
            ScreenHeaderTextWithLoader("Moodle courses", isLoading)
        },
        displayDirectly = false,
        scroll = true
    ) { course ->
        CourseEntry(course)
    }
}
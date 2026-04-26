package noorg.kloud.vthelper.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.CourseEntry
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderTextWithLoader
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel
import noorg.kloud.vthelper.ui.view_models.ManoSemesterAndSubjectViewModel
import noorg.kloud.vthelper.ui.view_models.MoodleCoursesViewModel

@Composable
fun MoodleCoursesScreen(
    loggedInUserViewModel: LoggedInUserViewModel,
    moodleCoursesViewModel: MoodleCoursesViewModel,
    manoSemesterAndSubjectViewModel: ManoSemesterAndSubjectViewModel,
    showSnack: SnackbarFun
) {

    val courses by moodleCoursesViewModel.courses.collectAsStateWithLifecycle()

    LoadableListSection(
        loggedInUserViewModel = loggedInUserViewModel,
        items = courses,
        fetchFunction = {
            listOf(
                moodleCoursesViewModel.fetchLatestCourseListFromApi(showSnack),
                manoSemesterAndSubjectViewModel.fetchCurrentSemesterFromApi(showSnack)
            ).joinAll()
        },
        header = { isLoading ->
            ScreenHeaderTextWithLoader("Moodle courses", isLoading)
        }
    ) { course ->
        CourseEntry(course)
    }
}
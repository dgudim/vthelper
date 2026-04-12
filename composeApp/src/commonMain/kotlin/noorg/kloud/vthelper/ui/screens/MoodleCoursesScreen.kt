package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.ui.components.CourseEntry
import noorg.kloud.vthelper.ui.components.common.EmptyListPlaceholderText
import noorg.kloud.vthelper.ui.components.common.LoadableListSection
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderTextWithLoader
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel
import noorg.kloud.vthelper.ui.view_models.MoodleCoursesViewModel

@Composable
fun MoodleCoursesScreen(
    loggedInUserViewModel: LoggedInUserViewModel,
    moodleCoursesViewModel: MoodleCoursesViewModel,
    showSnack: SnackbarFun
) {

    val courses by moodleCoursesViewModel.courses.collectAsStateWithLifecycle()

    LoadableListSection(
        loggedInUserViewModel = loggedInUserViewModel,
        items = courses,
        fetchFunction = {
            moodleCoursesViewModel.fetchLatestCourseListFromApi(showSnack)
        },
        header = { isLoading ->
            ScreenHeaderTextWithLoader("Moodle courses", isLoading)
        }
    ) { course ->
        CourseEntry(
            course.title,
            course.description,
            course.color,
            course.viewUrl,
            course.coverImagePath
        )
    }
}
package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.ui.components.CourseEntry
import noorg.kloud.vthelper.ui.components.common.EmptyListPlaceholderText
import noorg.kloud.vthelper.ui.components.common.ScreenHeaderText
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel
import noorg.kloud.vthelper.ui.view_models.MoodleCoursesViewModel

@Composable
fun CoursesScreen(
    loggedInUserViewModel: LoggedInUserViewModel,
    moodleCoursesViewModel: MoodleCoursesViewModel,
    showSnack: (String) -> Unit = {}
) {

    val userState by loggedInUserViewModel.userState.collectAsStateWithLifecycle()
    val courses by moodleCoursesViewModel.courses.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        moodleCoursesViewModel
            .fetchLatestCourseListFromApi(showSnack)
            .invokeOnCompletion { isLoading = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {

        ScreenHeaderText("Moodle courses")

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CircularProgressIndicator(
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            return
        }

        if (!userState.isSessionValid) {
            EmptyListPlaceholderText(
                text = "Please log in to view your courses",
            )
            return
        }

        if (courses.isEmpty()) {
            EmptyListPlaceholderText(
                text = "",
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = courses) { course ->
                    CourseEntry(
                        course.title,
                        course.description,
                        "<TODO: extract lecturer>",
                        course.color,
                        course.coverImagePath
                    )
                }
            }
        }
    }
}
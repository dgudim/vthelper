package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.LocalDb
import noorg.kloud.vthelper.data.data_providers.MoodleCoursesProvider
import noorg.kloud.vthelper.ui.components.CourseEntry
import noorg.kloud.vthelper.ui.view_models.MoodleCoursesViewModel
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.dc

@Composable
fun CoursesScreen(showSnack: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Moodle courses",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )

        val db = LocalDb.current!!
        val coursesViewModel =
            remember { MoodleCoursesViewModel(MoodleCoursesProvider(db.moodleCourseDao())) }

        val courses by coursesViewModel.courses.collectAsStateWithLifecycle()

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items = courses) { course ->
                CourseEntry(
                    course.title,
                    course.description,
                    "",
                    course.color,
                    painterResource(Res.drawable.dc)
                )
            }
        }

    }
}
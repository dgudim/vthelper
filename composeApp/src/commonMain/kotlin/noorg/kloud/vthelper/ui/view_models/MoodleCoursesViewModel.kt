package noorg.kloud.vthelper.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import noorg.kloud.vthelper.data.data_providers.MoodleCoursesProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import kotlin.collections.emptyList
import kotlin.time.Duration.Companion.seconds

// https://stackoverflow.com/questions/79814739/only-first-dao-flow-of-room-database-returns-values

class MoodleCoursesViewModel(moodleCoursesProvider: MoodleCoursesProvider) : ViewModel() {
    val courses: StateFlow<List<ProvidedMoodleCourseEntity>> =
        moodleCoursesProvider
            .getAllCourses()
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = emptyList(),
            )
}
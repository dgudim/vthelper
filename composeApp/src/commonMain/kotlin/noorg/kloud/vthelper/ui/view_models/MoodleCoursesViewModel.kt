package noorg.kloud.vthelper.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.data.data_providers.MoodleCoursesProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import kotlin.collections.emptyList
import kotlin.time.Duration.Companion.seconds

// https://stackoverflow.com/questions/79814739/only-first-dao-flow-of-room-database-returns-values

class MoodleCoursesViewModel(private val moodleCoursesProvider: MoodleCoursesProvider) :
    ViewModel() {


    // TODO: Add 'pending result' data type to display loading animation in the ui (combine a separate flow into this one?)

    val courses: StateFlow<List<ProvidedMoodleCourseEntity>> =
        moodleCoursesProvider
            .getAllCourses()
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = emptyList(),
            )

    fun fetchLatestCourseListFromApi(showSnack: (String) -> Unit): Job {
        return viewModelScope.launch {
            moodleCoursesProvider.fetchCoursesFromApi()
                .onFailure({
                    showSnack(it.message ?: "")
                })
        }
    }
}
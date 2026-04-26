package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.data.data_providers.CalendarProvider
import noorg.kloud.vthelper.data.data_providers.MoodleCoursesProvider
import noorg.kloud.vthelper.data.local_models.LocalManoCalendarEvent
import noorg.kloud.vthelper.data.local_models.LocalMoodleCalendarEvent
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import kotlin.collections.listOf
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@Stable
class CalendarViewModel(
    private val calendarProvider: CalendarProvider,
    moodleCoursesProvider: MoodleCoursesProvider
) : ViewModel() {

    // https://www.baeldung.com/kotlin/join-two-lists
    // https://www.baeldung.com/kotlin/flows-sequential-concatenation

    private var moodleCoursesMap = moodleCoursesProvider.getAllCourses()
        .map { list ->
            list.associateBy { it.courseModCode }
        }

    private var _manoEvents = MutableStateFlow(listOf<LocalManoCalendarEvent>())
    private var _moodleEvents = MutableStateFlow(listOf<LocalMoodleCalendarEvent>())

    val moodleEvents = _moodleEvents.combine(moodleCoursesMap) { events, courses ->
        for (event in events) {
            event.setLinkedMoodleCourse(courses[event.courseModCode])
        }
        return@combine events
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        listOf()
    )

    val manoEvents = _manoEvents.asStateFlow()

    fun fetchMoodleEvents(showSnack: SnackbarFun): Job {
        return viewModelScope.launch {
            // Don't wait for the API, load immediately if present
            calendarProvider.loadFromFileIfAvailable()
                .onSuccess {
                    _moodleEvents.value = it
                }
            calendarProvider.fetchMoodleEventsIfNeeded(6.hours)
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
                .onSuccess {
                    _moodleEvents.value = it
                }

        }
    }

}
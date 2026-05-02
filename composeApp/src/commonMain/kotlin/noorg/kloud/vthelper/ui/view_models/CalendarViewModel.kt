package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.api.models.toResultFail
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.data_providers.CalendarProvider
import noorg.kloud.vthelper.data.data_providers.ManoSemesterAndSubjectProvider
import noorg.kloud.vthelper.data.data_providers.MoodleCoursesProvider
import noorg.kloud.vthelper.data.local_models.LocalManoCalendarEvent
import noorg.kloud.vthelper.data.local_models.LocalManoExamEvent
import noorg.kloud.vthelper.data.local_models.LocalMoodleCalendarEvent
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import kotlin.collections.listOf
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@Stable
class CalendarViewModel(
    private val calendarProvider: CalendarProvider,
    moodleCoursesProvider: MoodleCoursesProvider,
    private val manoSemesterAndSubjectProvider: ManoSemesterAndSubjectProvider
) : ViewModel() {

    // https://www.baeldung.com/kotlin/join-two-lists
    // https://www.baeldung.com/kotlin/flows-sequential-concatenation


    private var _manoEvents = MutableStateFlow(listOf<LocalManoCalendarEvent>())
    private var _moodleEvents = MutableStateFlow(listOf<LocalMoodleCalendarEvent>())

    val events = combine(
        _moodleEvents,
        moodleCoursesProvider.getAllCourses()
            .map { list ->
                list.associateBy { it.courseModCode }
            },
        manoSemesterAndSubjectProvider
            .getExamTimetableEvents()
            .map { events ->
                events.map { LocalManoExamEvent(it) }
            }
    ) { moodleEvents, courses, manoExams ->
        for (event in moodleEvents) {
            event.setLinkedMoodleCourse(courses[event.courseModCode])
        }
        return@combine moodleEvents.asSequence() + manoExams.asSequence()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        sequenceOf()
    )

    fun fetchMoodleEvents(showSnack: SnackbarFun): Deferred<Result<String>> {
        // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/async.html
        return viewModelScope.async {
            // Don't wait for the API, load immediately if present
            calendarProvider.loadFromFileIfAvailable()
                .onSuccess {
                    _moodleEvents.value = it
                }
            calendarProvider.fetchMoodleEventsIfNeeded(6.hours)
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                    return@async it.toResultFail()
                }
                .onSuccess {
                    _moodleEvents.value = it
                }

            return@async "OK".toResultOk()
        }
    }

    fun fetchManoExams(showSnack: SnackbarFun): Deferred<Result<String>> {
        return viewModelScope.async {
            manoSemesterAndSubjectProvider
                .fetchExamTimetable()
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
        }
    }

}
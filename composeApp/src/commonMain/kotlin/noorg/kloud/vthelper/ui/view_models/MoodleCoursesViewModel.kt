package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.timeout
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.data.data_providers.ManoSemesterAndSubjectProvider
import noorg.kloud.vthelper.data.data_providers.MoodleCoursesProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import kotlin.collections.emptyList
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// https://stackoverflow.com/questions/79814739/only-first-dao-flow-of-room-database-returns-values

@Stable
class MoodleCoursesViewModel(
    private val moodleCoursesProvider: MoodleCoursesProvider,
    private val manoSemesterAndSubjectProvider: ManoSemesterAndSubjectProvider
) :
    ViewModel() {

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val currentSemesterSubjectModCodes = manoSemesterAndSubjectProvider.getCurrentSemester()
        // Map the last value, cancel the previous mapping function
        .mapLatest {
            if (it == null) {
                return@mapLatest setOf()
            }
            val subjects = manoSemesterAndSubjectProvider
                .getSubjectsForSemester(it.absoluteSequenceNum)
                .filter { subjectList -> !subjectList.isEmpty() } // Basically wait until we get the subjects
                .timeout(1.minutes) // If there was an error in the API or something, don't block indefinetely
                .catch { emit(listOf()) }
                .take(1)
                .first()
                .map { subject -> subject.modCode }
                .toSet()

            return@mapLatest subjects
        }

    val courses: StateFlow<List<ProvidedMoodleCourseEntity>> =
        moodleCoursesProvider
            .getAllCourses()
            .combine(currentSemesterSubjectModCodes) { courses, subjects ->
                return@combine courses.map {
                    if (subjects.contains(it.courseModCode)) {
                        return@map it.copy(isFromCurrentSemester = true)
                    }
                    return@map it
                }.sortedByDescending { it.isFromCurrentSemester }
            }
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = emptyList(),
            )

    fun fetchCourses(showSnack: SnackbarFun): Deferred<Result<String>> {
        return viewModelScope.async {
            moodleCoursesProvider
                .fetchCoursesFromApi()
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
        }
    }
}
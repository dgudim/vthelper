package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.data.data_providers.ManoSemesterProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import kotlin.time.Duration.Companion.seconds

@Stable
class ManoSemesterViewModel(
    private val manoSemesterProvider: ManoSemesterProvider
) : ViewModel() {

    val semesters: StateFlow<List<ProvidedManoSemesterEntity>> =
        manoSemesterProvider
            .getAllSemesters()
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = emptyList(),
            )

    fun fetchSemestersFromApi(showSnack: SnackbarFun): Job {
        return viewModelScope.launch {
            manoSemesterProvider.fetchSemestersFromApi()
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
        }
    }
}
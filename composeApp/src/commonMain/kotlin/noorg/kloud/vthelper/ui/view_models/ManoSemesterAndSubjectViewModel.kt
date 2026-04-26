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
import noorg.kloud.vthelper.data.data_providers.ManoSemesterAndSubjectProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSemesterEntity
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSettlementGroup
import noorg.kloud.vthelper.data.provider_models.ProvidedManoSubjectEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import kotlin.time.Duration.Companion.seconds

@Stable
class ManoSemesterAndSubjectViewModel(
    private val manoSemesterAndSubjectProvider: ManoSemesterAndSubjectProvider
) : ViewModel() {

    val semesters: StateFlow<List<ProvidedManoSemesterEntity>> =
        manoSemesterAndSubjectProvider
            .getAllSemesters()
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = emptyList(),
            )

    val currentSemester = manoSemesterAndSubjectProvider
        .getCurrentSemester()
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = null,
        )

    fun fetchAllSemestersFromApi(showSnack: SnackbarFun): Job {
        return viewModelScope.launch {
            manoSemesterAndSubjectProvider.fetchAllSemestersAndSubjectsFromApiIfNeeded()
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
        }
    }

    fun fetchCurrentSemesterFromApi(showSnack: SnackbarFun): Job {
        return viewModelScope.launch {
            manoSemesterAndSubjectProvider
                .fetchCurrentSemesterAndSubjectsFromApi()
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
        }
    }

    fun fetchSettlementGroupsFromApi(
        semesterAbsoluteSequenceNum: Int,
        subjectModId: Int,
        showSnack: SnackbarFun
    ): Job {
        return viewModelScope.launch {
            manoSemesterAndSubjectProvider.fetchSettlementGroupsForSubjectInSemester(semesterAbsoluteSequenceNum, subjectModId)
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
        }
    }

    fun getSubjectsForSemesterAsStateFlow(semesterAbsoluteSequence: Int): StateFlow<List<ProvidedManoSubjectEntity>> {
        return manoSemesterAndSubjectProvider
            .getSubjectsForSemester(semesterAbsoluteSequence)
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = emptyList(),
            )
    }

    fun getSettlementGroupsForSubjectAsStateFlow(
        semAbsoluteSequenceNum: Int,
        subjectModId: Int
    ): StateFlow<List<ProvidedManoSettlementGroup>> {
        return manoSemesterAndSubjectProvider
            .getSettlementGroupsForSubjectInSemester(semAbsoluteSequenceNum, subjectModId)
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = emptyList(),
            )
    }
}
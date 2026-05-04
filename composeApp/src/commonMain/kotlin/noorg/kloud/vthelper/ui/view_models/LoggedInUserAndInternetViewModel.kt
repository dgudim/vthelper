package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jordond.connectivity.Connectivity
import dev.jordond.connectivity.Connectivity.Status
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.data.data_providers.LoggedInUserProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedLoggedInUserEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import kotlin.time.Duration.Companion.seconds

// https://developer.android.com/develop/ui/compose/quick-guides/content/validate-input
// https://www.reddit.com/r/androiddev/comments/1dkyzbg/confused_about_when_to_use_mutablestateflow_vs/

@Stable
class LoggedInUserAndInternetViewModel(
    private val loggedInUserProvider: LoggedInUserProvider,
    connectivityProvider: Connectivity
) : ViewModel() {
    val userState = loggedInUserProvider
        .getCurrentUserInfo()
        .onEach { initLocalValuesFromFlowIfNeeded(it) }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = ProvidedLoggedInUserEntity(),
        )

    val internetState = connectivityProvider.statusUpdates
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = Status.Disconnected,
        )

    private var _mfaCode = MutableStateFlow("")
    val mfaCode = _mfaCode.asStateFlow()

    private var _studentId = MutableStateFlow("")
    val studentId = _studentId.asStateFlow()

    private var _plainPassword = MutableStateFlow("")
    val plainPassword = _plainPassword.asStateFlow()

    fun logout(showSnack: SnackbarFun): Job {
        return viewModelScope.launch {
            loggedInUserProvider.logout()
            showSnack("Logged out", SnackBarSeverityLevel.NEUTRAL, SnackbarDuration.Short)
        }
    }

    fun login(
        studentId: String,
        plainPassword: String,
        mfaCode: String,
        showSnack: SnackbarFun
    ): Job {
        return viewModelScope.launch {
            loggedInUserProvider.login(studentId, plainPassword, mfaCode)
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
                .onSuccess {
                    showSnack(
                        "Logged in successfully",
                        SnackBarSeverityLevel.SUCCESS,
                        SnackbarDuration.Short
                    )
                }
        }
    }

    fun initLocalValuesFromFlowIfNeeded(loggedInUser: ProvidedLoggedInUserEntity) {
        if (_studentId.value.isEmpty()) {
            updateStudentId(loggedInUser.studentId ?: "")
            updatePassword(loggedInUser.plainPassword ?: "")
        }
    }

    fun updateStudentId(newStudentId: String) {
        _studentId.update { newStudentId.filter(Char::isDigit) }
    }

    fun updatePassword(newPassword: String) {
        _plainPassword.update { newPassword }
    }

    fun updateMfa(newMfa: String) {
        _mfaCode.update { newMfa.filter(Char::isDigit) }
    }
}
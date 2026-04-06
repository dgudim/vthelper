package noorg.kloud.vthelper.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import noorg.kloud.vthelper.data.data_providers.LoggedInUserProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedLoggedInUserEntity
import kotlin.time.Duration.Companion.seconds

// https://developer.android.com/develop/ui/compose/quick-guides/content/validate-input
// https://www.reddit.com/r/androiddev/comments/1dkyzbg/confused_about_when_to_use_mutablestateflow_vs/

class LoggedInUserViewModel(private val loggedInUserProvider: LoggedInUserProvider) : ViewModel() {
    val userState = loggedInUserProvider
        .getCurrentUserInfo()
        .onEach { initLocalValuesFromFlowIfNeeded(it) }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = ProvidedLoggedInUserEntity(),
        )

    private var _mfaCode = MutableStateFlow("")
    val mfaCode = _mfaCode.asStateFlow()

    private var _studentId = MutableStateFlow("")
    val studentId = _studentId.asStateFlow()

    private var _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    suspend fun logout() {
        loggedInUserProvider.logout()
    }

    suspend fun login(
        studentId: String,
        password: String,
        mfaCode: String,
        showSnack: (String) -> Unit
    ) {
        val result = loggedInUserProvider.login(studentId, password, mfaCode)
        if (result.isFailure) {
            showSnack(result.exceptionOrNull()?.message ?: "")
        }
    }

    fun initLocalValuesFromFlowIfNeeded(loggedInUser: ProvidedLoggedInUserEntity) {
        if (_studentId.value.isEmpty()) {
            updateStudentId(loggedInUser.studentId ?: "")
            updatePassword(loggedInUser.password ?: "")
        }
    }

    fun updateStudentId(newStudentId: String) {
        _studentId.update { newStudentId.filter(Char::isDigit) }
    }

    fun updatePassword(newPassword: String) {
        _password.update { newPassword }
    }

    fun updateMfa(newMfa: String) {
        _mfaCode.update { newMfa.filter(Char::isDigit) }
    }
}
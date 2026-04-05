package noorg.kloud.vthelper.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.data.data_providers.LoggedInUserProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedLoggedInUserEntity

// https://developer.android.com/develop/ui/compose/quick-guides/content/validate-input
// https://www.reddit.com/r/androiddev/comments/1dkyzbg/confused_about_when_to_use_mutablestateflow_vs/

class LoggedInUserViewModel(private val loggedInUserProvider: LoggedInUserProvider) : ViewModel() {
    private val _userState = MutableStateFlow(ProvidedLoggedInUserEntity())
    val userState get() = _userState.asStateFlow()

    private var _mfaCode = MutableStateFlow("")
    val mfaCode = _mfaCode.asStateFlow()

    fun fetchUserDataFromDb() {
        // Fetch in view model scope to avoid cancelling and refetching on state change / recomposition
        viewModelScope.launch {
            _userState.value = loggedInUserProvider.getCurrentUserInfo()
        }
    }

    fun updateStudentId(newStudentId: String) {
        _userState.update { it.copy(studentId = newStudentId.filter(Char::isDigit)) }
    }

    fun updatePassword(newPassword: String) {
        _userState.update { it.copy(password = newPassword) }
    }

    fun updateMfa(newMfa: String) {
        _mfaCode.update { newMfa.filter(Char::isDigit) }
    }
}
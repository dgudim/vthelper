package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.data.data_providers.ManoEmployeeProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedManoEmployeeEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel

@Stable
class ManoEmployeeViewModel(
    private val manoEmployeeProvider: ManoEmployeeProvider
) : ViewModel() {

    private var _selectedEmployee = MutableStateFlow<ProvidedManoEmployeeEntity?>(null)
    val selectedEmployee = _selectedEmployee.asStateFlow()

    fun selectEmployeeById(showSnack: SnackbarFun, employeeId: Long): Job {
        return viewModelScope.launch {
            _selectedEmployee.update { manoEmployeeProvider.getEmployeeById(employeeId) }
            manoEmployeeProvider.fetchEmployeeDetailsFromApi(employeeId)
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }.onSuccess {
                    _selectedEmployee.update { manoEmployeeProvider.getEmployeeById(employeeId) }
                }
        }
    }

    fun deselectEmployee() {
        _selectedEmployee.update { null }
    }
}
package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.api.ManoApi
import noorg.kloud.vthelper.data.data_providers.ManoEmployeeProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedManoEmployeeEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel

@Stable
class ManoEmployeeViewModel(
    private val manoEmployeeProvider: ManoEmployeeProvider
) : ViewModel() {

    private var _selectedEmployee = MutableStateFlow(null)
    val selectedEmployee = _selectedEmployee.asStateFlow()

    fun fetchEmployeeDataById(showSnack: SnackbarFun, employeeId: Long) {
        viewModelScope.launch {
            manoEmployeeProvider.fetchEmployeeDetailsFromApi(employeeId)
                .onFailure {
                    showSnack(it.message ?: "", SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                }
        }
    }


}
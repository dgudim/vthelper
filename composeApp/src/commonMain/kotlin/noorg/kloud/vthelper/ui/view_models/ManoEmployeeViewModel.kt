package noorg.kloud.vthelper.ui.view_models

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import noorg.kloud.vthelper.data.data_providers.ManoEmployeeProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedManoEmployeeEntity

sealed class SelectedEmployeeLoadingState {
    data class Success(val value: ProvidedManoEmployeeEntity): SelectedEmployeeLoadingState()
    data class Loading(val v: String? = null): SelectedEmployeeLoadingState()
}

@Stable
class ManoEmployeeViewModel(
    private val manoEmployeeProvider: ManoEmployeeProvider
) : ViewModel() {

    private var _selectedEmployee = MutableStateFlow(SelectedEmployeeLoadingState.Loading())
    val selectedEmployee = _selectedEmployee.asStateFlow()

    fun fetchEmployeeDataById(employeeId: Long) {

    }

}
package noorg.kloud.vthelper.ui.view_models

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import noorg.kloud.vthelper.data.data_providers.ManoEmployeeProvider

@Stable
class ManoEmployeeViewModel(
    private val manoEmployeeProvider: ManoEmployeeProvider
) : ViewModel() {

    private var _selectedEmployee = MutableStateFlow(null)
    val selectedEmployee = _selectedEmployee.asStateFlow()

}
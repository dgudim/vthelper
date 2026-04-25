package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.data.data_providers.ManoEmployeeProvider
import noorg.kloud.vthelper.data.provider_models.ProvidedManoEmployeeEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel

data class SelectedEmployeeData(
    val isLoading: Boolean = true,
    val data: ProvidedManoEmployeeEntity? = null
)

@Stable
class ManoEmployeeViewModel(
    private val manoEmployeeProvider: ManoEmployeeProvider
) : ViewModel() {

    private var currentFetchJob: Job? = null
    private var _selectedEmployee = MutableStateFlow(SelectedEmployeeData())
    val selectedEmployee = _selectedEmployee.asStateFlow()

    fun selectEmployeeById(showSnack: SnackbarFun, employeeId: Long) {
        deselectEmployee()
        currentFetchJob = viewModelScope.launch {
            val baseData =
                SelectedEmployeeData(
                    isLoading = true,
                    data = manoEmployeeProvider.getEmployeeById(employeeId)
                )
            _selectedEmployee.update { baseData }
            manoEmployeeProvider.fetchEmployeeDetailsFromApi(employeeId)
                .onFailure {
                    val msg = it.message ?: ""
                    if (msg.contains("JobCancellationException")) {
                        return@launch
                    }
                    showSnack(msg, SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                    _selectedEmployee.update { baseData.copy(isLoading = false) }
                }.onSuccess {
                    _selectedEmployee.update {
                        SelectedEmployeeData(
                            isLoading = false,
                            data = manoEmployeeProvider.getEmployeeById(employeeId)
                        )
                    }
                }
        }
    }

    fun deselectEmployee() {
        currentFetchJob?.cancel()
        currentFetchJob = null
        _selectedEmployee.update { SelectedEmployeeData() }
    }
}
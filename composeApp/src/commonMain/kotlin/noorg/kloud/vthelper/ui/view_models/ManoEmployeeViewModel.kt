package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.util.reflect.instanceOf
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
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

    private val currentFetchJob: AtomicRef<Job?> = atomic(null)
    private var _selectedEmployee = MutableStateFlow(SelectedEmployeeData())
    val selectedEmployee = _selectedEmployee.asStateFlow()

    fun selectEmployeeById(showSnack: SnackbarFun, employeeId: Long) {
        deselectEmployee()
        currentFetchJob.value = viewModelScope.launch {
            _selectedEmployee.update {
                SelectedEmployeeData(
                    isLoading = true,
                    data = manoEmployeeProvider.getEmployeeById(employeeId)
                )
            }
            manoEmployeeProvider.fetchEmployeeDetailsFromApi(employeeId)
                .onFailure { ex ->
                    val msg = ex.message ?: ""
                    if (msg.contains("CancellationException")) {
                        return@launch
                    }
                    showSnack(msg, SnackBarSeverityLevel.ERROR, SnackbarDuration.Long)
                    _selectedEmployee.update { it.copy(isLoading = false) }
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
        currentFetchJob.getAndSet(null)?.cancel()
        _selectedEmployee.update { SelectedEmployeeData() }
    }
}
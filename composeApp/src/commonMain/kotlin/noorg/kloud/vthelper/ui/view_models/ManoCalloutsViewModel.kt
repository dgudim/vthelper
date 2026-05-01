package noorg.kloud.vthelper.ui.view_models

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.SnackbarFun
import noorg.kloud.vthelper.data.data_providers.ManoCalloutsProvider
import noorg.kloud.vthelper.data.dbdaos.mano.ManoCalloutsDao
import noorg.kloud.vthelper.data.provider_models.ProvidedLoggedInUserEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import kotlin.time.Duration.Companion.seconds

@Stable
class ManoCalloutsViewModel(private val manoCalloutsProvider: ManoCalloutsProvider) : ViewModel() {

    val callouts = manoCalloutsProvider.getAllCalloutsAsFlow()
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = listOf(),
        )

    fun fetchAllCallouts(showSnack: SnackbarFun): Deferred<Result<String>> {
        return viewModelScope.async {
            return@async manoCalloutsProvider.fetchAllCallouts()
                .onFailure {
                    showSnack(
                        it.message ?: "",
                        SnackBarSeverityLevel.ERROR,
                        SnackbarDuration.Long
                    )
                }
        }
    }

}
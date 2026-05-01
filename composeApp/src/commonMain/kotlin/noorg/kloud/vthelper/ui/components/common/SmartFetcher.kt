package noorg.kloud.vthelper.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.ui.view_models.LoggedInUserAndInternetViewModel

@Composable
fun SmartFetcher(
    loggedInUserAndInternetViewModel: LoggedInUserAndInternetViewModel,
    isLoadingState: MutableState<Boolean>,
    fetchFunction: suspend () -> Result<String>,
) {

    val userState by loggedInUserAndInternetViewModel.userState.collectAsStateWithLifecycle()
    val internetState by loggedInUserAndInternetViewModel.internetState.collectAsStateWithLifecycle()

    var fetchedSuccessfully by remember { mutableStateOf(false) }

    LaunchedEffect(userState.isSessionValid, internetState) {
        if (!userState.isSessionValid
            || internetState.isDisconnected
            || fetchedSuccessfully
        ) {
            return@LaunchedEffect
        }
        isLoadingState.value = true
        fetchFunction().onSuccess { fetchedSuccessfully = true }
        isLoadingState.value = false
    }
}
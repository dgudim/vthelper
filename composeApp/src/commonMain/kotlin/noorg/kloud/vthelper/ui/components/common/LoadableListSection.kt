package noorg.kloud.vthelper.ui.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noorg.kloud.vthelper.ui.view_models.LoggedInUserAndInternetViewModel

@Composable
fun <T> LoadableListSection(
    modifier: Modifier,
    loggedInUserAndInternetViewModel: LoggedInUserAndInternetViewModel,
    items: List<T>,
    displayDirectly: Boolean = false,
    scroll: Boolean,
    fetchFunction: suspend () -> Result<String>,
    header: @Composable (Boolean) -> Unit,
    item: @Composable (T) -> Unit
) {

    val isLoadingState = remember { mutableStateOf(false) }
    val isLoading by isLoadingState

    SmartFetcher(
        loggedInUserAndInternetViewModel,
        isLoadingState,
        fetchFunction
    )

    var columnModifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()

    if (scroll) {
        columnModifier = columnModifier.verticalScroll(rememberScrollState())
    }

    Column(
        modifier = modifier
    ) {

        header(isLoading)

        if (items.isEmpty()) {
            Text(
                text = "Nothing to show ¯ \\_(ツ)_/¯",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp)
            )
        } else {
            if (displayDirectly) {
                Column(
                    modifier = columnModifier
                ) {
                    for (data in items) {
                        item(data)
                    }
                }

            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    items(items = items) { course -> item(course) }
                }
            }
        }
    }
}
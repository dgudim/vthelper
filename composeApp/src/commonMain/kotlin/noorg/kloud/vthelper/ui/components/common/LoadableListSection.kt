package noorg.kloud.vthelper.ui.components.common

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import noorg.kloud.vthelper.ui.view_models.LoggedInUserViewModel

@Composable
fun <T> LoadableListSection(
    modifier: Modifier,
    loggedInUserViewModel: LoggedInUserViewModel,
    items: List<T>,
    displayDirectly: Boolean = false,
    scroll: Boolean,
    fetchFunction: suspend () -> Unit,
    header: @Composable (Boolean) -> Unit,
    item: @Composable (T) -> Unit
) {
    // TODO: Check network connectivity and don't fetch if not available
    val userState by loggedInUserViewModel.userState.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(userState.isSessionValid) {
        if (!userState.isSessionValid) {
            return@LaunchedEffect
        }
        isLoading = true
        fetchFunction()
        isLoading = false
    }

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

        if (!userState.isSessionValid) {
            EmptyListPlaceholderText(
                text = "Please log in",
            )
            return
        }

        if (items.isEmpty()) {
            EmptyListPlaceholderText(
                text = "",
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
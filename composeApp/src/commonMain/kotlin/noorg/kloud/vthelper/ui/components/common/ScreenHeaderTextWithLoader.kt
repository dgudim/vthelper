package noorg.kloud.vthelper.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ScreenHeaderTextWithLoader(text: String, isLoading: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge
        )
        if (isLoading) CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.CenterEnd),
            color = MaterialTheme.colorScheme.primary
        )
    }

    HorizontalDivider(
        thickness = 2.dp,
        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp)
    )
}
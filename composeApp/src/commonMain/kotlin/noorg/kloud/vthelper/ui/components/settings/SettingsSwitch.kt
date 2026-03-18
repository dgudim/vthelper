package noorg.kloud.vthelper.ui.components.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSwitch(text: String) {

    var state by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 48.dp)
        )
        Switch(
            checked = state,
            onCheckedChange = { newState -> state = newState },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end=16.dp)
        )
    }
}

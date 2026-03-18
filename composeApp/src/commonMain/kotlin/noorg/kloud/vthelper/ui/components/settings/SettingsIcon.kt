package noorg.kloud.vthelper.ui.components.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingsIcon(text: String, icon: DrawableResource) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 48.dp)
        )
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(48.dp)
                .padding(end = 16.dp)
        )
    }
}
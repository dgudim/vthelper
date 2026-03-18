package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import noorg.kloud.vthelper.ui.components.settings.SettingsHeader
import noorg.kloud.vthelper.ui.components.settings.SettingsIcon
import noorg.kloud.vthelper.ui.components.settings.SettingsSwitch
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.edit_24px

@Composable
fun SettingsScreen(showSnack: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SettingsHeader("Behaviour")
        SettingsSwitch("Stay in background")
        SettingsSwitch("Developer mode")
        SettingsSwitch("Fetch on startup")

        SettingsHeader("Theme")
        SettingsSwitch("Warm colors")
        SettingsIcon("Edit course colors", Res.drawable.edit_24px)
    }
}
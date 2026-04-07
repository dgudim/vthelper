package noorg.kloud.vthelper

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import noorg.kloud.vthelper.platform_specific.getDatabaseBuilder

fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "VTHelper",
    ) {
        App()
    }
}
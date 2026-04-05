package noorg.kloud.vthelper

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {

    val database = remember { getDatabaseBuilder() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "VTHelper",
    ) {
        App(database)
    }
}
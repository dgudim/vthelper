package noorg.kloud.vthelper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.RoomDatabase
import noorg.kloud.vthelper.data.AppDatabase
import noorg.kloud.vthelper.data.getRoomDatabase
import noorg.kloud.vthelper.ui.theme.LocalCustomColorPalette
import noorg.kloud.vthelper.ui.theme.VTTheme

val LocalDb = staticCompositionLocalOf<AppDatabase?> { null }

@Composable
fun App(dbBuilder: RoomDatabase.Builder<AppDatabase>) {

    val dbInstance = remember { getRoomDatabase(dbBuilder) }

    CompositionLocalProvider(
        LocalDb provides dbInstance
    ) {
        VTTheme {
            Router()
        }
    }
}
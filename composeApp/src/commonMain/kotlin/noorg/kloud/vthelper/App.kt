package noorg.kloud.vthelper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.api.VTBaseApi
import noorg.kloud.vthelper.data.AppDatabase
import noorg.kloud.vthelper.data.getRoomDatabase
import noorg.kloud.vthelper.ui.theme.LocalCustomColorPalette
import noorg.kloud.vthelper.ui.theme.VTTheme

val LocalDb = staticCompositionLocalOf<AppDatabase?> { null }

@Composable
fun App(dbBuilder: RoomDatabase.Builder<AppDatabase>) {

    val dbInstance = remember { getRoomDatabase(dbBuilder) }

    // Init cookies from background
    rememberCoroutineScope().launch {
        val userData = dbInstance.loggedInUserDao().get()
        println("Currently logged in as ${userData?.fullName}")
        VTBaseApi.cookieStorage.loadAllFromJson(userData?.cookiesJson)
    }

    CompositionLocalProvider(
        LocalDb provides dbInstance
    ) {
        VTTheme {
            Router()
        }
    }
}
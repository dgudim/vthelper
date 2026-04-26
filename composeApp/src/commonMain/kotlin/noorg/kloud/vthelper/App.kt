package noorg.kloud.vthelper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.launch
import noorg.kloud.vthelper.api.VTBaseApi
import noorg.kloud.vthelper.platform_specific.AppDatabase
import noorg.kloud.vthelper.platform_specific.getRoomDatabase
import noorg.kloud.vthelper.ui.theme.VTTheme

val LocalDb = staticCompositionLocalOf<AppDatabase?> { null }

@Composable
fun App() {

    val dbInstance = remember { getRoomDatabase("main.db") }
    val appScope = rememberCoroutineScope()

    // Init cookies from background
    appScope.launch {
        val userData = dbInstance.loggedInUserDao().getAll().firstOrNull()
        println("Currently logged in as ${userData?.fullName}")
        VTBaseApi.cookieStorage.loadAllFromJson(userData?.cookiesJson)
    }

    CompositionLocalProvider(
        LocalDb provides dbInstance
    ) {
        VTTheme {
            Router(appScope)
        }
    }
}
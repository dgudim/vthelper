package noorg.kloud.vthelper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

// https://stackoverflow.com/questions/77026273/android-create-custom-colors-in-compose-with-material-3
// TODO: Use this? https://github.com/zacharee/MultiplatformMaterialYou

@Composable
fun VTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    // logic for which custom palette to use
    val customColorPalette =
        if (darkTheme) DarkCustomColorPalette
        else LightCustomColorPalette

    val colorScheme = when {
        darkTheme -> DarkColors
        else -> LightColors
    }
    CompositionLocalProvider(
        LocalCustomColorPalette provides customColorPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

val MaterialTheme.customColors: CustomColorPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColorPalette.current
package noorg.kloud.vthelper.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val md_theme_light_primary = Color(0xFF405AA8) // Shifted from Purple/Pink to Blue
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFDCE1FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001549)
val md_theme_light_secondary = Color(0xFF565E71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFDAE2F9)
val md_theme_light_onSecondaryContainer = Color(0xFF131C2C)
val md_theme_light_tertiary = Color(0xFF7C5635)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFDCC2)
val md_theme_light_onTertiaryContainer = Color(0xFF2E1500)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFFFBFF)
val md_theme_light_onBackground = Color(0xFF1B1B1F)
val md_theme_light_surface = Color(0xFFFFFBFF)
val md_theme_light_onSurface = Color(0xFF1B1B1F)
val md_theme_light_surfaceVariant = Color(0xFFE1E2EC)
val md_theme_light_onSurfaceVariant = Color(0xFF44474F)
val md_theme_light_outline = Color(0xFF74777F)
val md_theme_light_inverseOnSurface = Color(0xFFF2F0F4)
val md_theme_light_inverseSurface = Color(0xFF303033)
val md_theme_light_inversePrimary = Color(0xFFB5C4FF)
val md_theme_light_surfaceTint = Color(0xFF405AA8)
val md_theme_light_outlineVariant = Color(0xFFC4C6D0)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFFB5C4FF)
val md_theme_dark_onPrimary = Color(0xFF052978)
val md_theme_dark_primaryContainer = Color(0xFF26418F)
val md_theme_dark_onPrimaryContainer = Color(0xFFDCE1FF)
val md_theme_dark_secondary = Color(0xFFBFC6DC)
val md_theme_dark_onSecondary = Color(0xFF283041)
val md_theme_dark_secondaryContainer = Color(0xFF3F4759)
val md_theme_dark_onSecondaryContainer = Color(0xFFDAE2F9)
val md_theme_dark_tertiary = Color(0xFFEFBD94)
val md_theme_dark_onTertiary = Color(0xFF48290C)
val md_theme_dark_tertiaryContainer = Color(0xFF623F20)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFDCC2)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF1B1B1F)
val md_theme_dark_onBackground = Color(0xFFE3E2E6)
val md_theme_dark_surface = Color(0xFF1B1B1F)
val md_theme_dark_onSurface = Color(0xFFE3E2E6)
val md_theme_dark_surfaceVariant = Color(0xFF44474F)
val md_theme_dark_onSurfaceVariant = Color(0xFFC4C6D0)
val md_theme_dark_outline = Color(0xFF8E9099)
val md_theme_dark_inverseOnSurface = Color(0xFF1B1B1F)
val md_theme_dark_inverseSurface = Color(0xFFE3E2E6)
val md_theme_dark_inversePrimary = Color(0xFF405AA8)
val md_theme_dark_surfaceTint = Color(0xFFB5C4FF)
val md_theme_dark_outlineVariant = Color(0xFF44474F)
val md_theme_dark_scrim = Color(0xFF000000)


val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

data class ColorVariants(
    val selected: Color = Color.Unspecified,
    val deselected: Color = Color.Unspecified,
    val disabled: Color = Color.Unspecified,
    val today: Color = Color.Unspecified,
)

@Immutable
data class CalendarColorPalette(
    val toolbarColor: Color = Color.Unspecified,
    val listItemBackgroundColor: Color = Color.Unspecified,

    val calendarDividerColor: Color = Color.Unspecified,
    val monthHeaderTextColor: Color = Color.Unspecified,

    val textColor: ColorVariants = ColorVariants(),
    val borderColor: ColorVariants = ColorVariants(),
    val bgColor: ColorVariants = ColorVariants()
)

@Immutable
data class CustomColorPalette(
    val goodResult: Color = Color.Unspecified,
    val okResult: Color = Color.Unspecified,
    val badResult: Color = Color.Unspecified,
)

val LightGoodResult = Color(color = 0xff3db33b)
val LightOkResult = Color(color = 0xffb39b3b)
val LightBadResult = Color(color = 0xffcc4643)

val DarkGoodResult = Color(color = 0xff4ee64c)
val DarkOkResult = Color(color = 0xffe6ba4c)
val DarkBadResult = Color(color = 0xfff25350)

val LightCustomColorPalette = CustomColorPalette(
    goodResult = LightGoodResult,
    okResult = LightOkResult,
    badResult = LightBadResult,
)

val DarkCustomColorPalette = CustomColorPalette(
    goodResult = DarkGoodResult,
    okResult = DarkOkResult,
    badResult = DarkBadResult,
)

val LocalCustomColorPalette = staticCompositionLocalOf { CustomColorPalette() }
val LocalCalendarColorPalette = staticCompositionLocalOf { CalendarColorPalette() }

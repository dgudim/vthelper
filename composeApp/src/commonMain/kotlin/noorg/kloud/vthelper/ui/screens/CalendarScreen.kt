package noorg.kloud.vthelper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import noorg.kloud.vthelper.ui.components.Calendar
import noorg.kloud.vthelper.ui.theme.CalendarColorPalette
import noorg.kloud.vthelper.ui.theme.ColorVariants
import noorg.kloud.vthelper.ui.theme.LocalCalendarColorPalette

@Composable
fun CalendarScreen(showSnack: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val calendarColors = CalendarColorPalette(
            toolbarColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            listItemBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow,

            calendarDividerColor = MaterialTheme.colorScheme.outline,
            monthHeaderTextColor = MaterialTheme.colorScheme.secondary,

            textColor = ColorVariants(
                selected = MaterialTheme.colorScheme.primary,
                deselected = MaterialTheme.colorScheme.onBackground,
                disabled = MaterialTheme.colorScheme.outline,
                today = MaterialTheme.colorScheme.tertiary
            ),

            borderColor = ColorVariants(
                selected = MaterialTheme.colorScheme.primary,
                deselected = Color.Transparent,
                disabled = Color.Transparent,
                today = MaterialTheme.colorScheme.outlineVariant
            ),

            bgColor = ColorVariants(
                selected = MaterialTheme.colorScheme.surfaceContainerHighest,
                deselected = Color.Transparent,
                disabled = Color.Transparent,
                today = Color.Transparent
            ),
        )

        CompositionLocalProvider(
            LocalCalendarColorPalette provides calendarColors
        ) {
            Calendar()
        }
    }
}
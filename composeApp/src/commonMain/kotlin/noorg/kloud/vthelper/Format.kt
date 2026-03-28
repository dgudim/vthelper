package noorg.kloud.vthelper

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import com.kizitonwose.calendar.core.Week
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import kotlin.time.Instant

private val localDateFormat = LocalDateTime.Format {
    date(LocalDate.Formats.ISO)
}

private val localTimeFormat = LocalDateTime.Format {
    hour()
    char(':')
    minute()
}

fun YearMonth.displayText(short: Boolean = false): String {
    return "${month.displayText(short = short)} $year"
}

fun Month.displayText(short: Boolean = true): String {
    return getDisplayName(short, Locale.current)
}

fun DayOfWeek.displayText(uppercase: Boolean = false, narrow: Boolean = false): String {
    return getDisplayName(narrow, Locale.current).let { value ->
        if (uppercase) value.toUpperCase(Locale.current) else value
    }
}

expect fun Month.getDisplayName(short: Boolean, locale: Locale): String

expect fun DayOfWeek.getDisplayName(narrow: Boolean = false, locale: Locale): String

fun Instant.formatLocalDate(): String {
    return toLocalDateTime(TimeZone.currentSystemDefault()).format(localDateFormat)
}

fun Instant.formatLocalTime(): String {
    return toLocalDateTime(TimeZone.currentSystemDefault()).format(localTimeFormat)
}

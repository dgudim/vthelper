package noorg.kloud.vthelper.ui

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale as JavaLocale

actual fun Month.getDisplayName(short: Boolean, locale: Locale): String {
    val style = if (short) JavaTextStyle.SHORT_STANDALONE else JavaTextStyle.FULL_STANDALONE
    return toJavaMonth().getDisplayName(style, JavaLocale.forLanguageTag(locale.toLanguageTag()))
}

actual fun DayOfWeek.getDisplayName(narrow: Boolean, locale: Locale): String {
    val style = if (narrow) JavaTextStyle.NARROW_STANDALONE else JavaTextStyle.SHORT_STANDALONE
    return toJavaDayOfWeek().getDisplayName(style, JavaLocale.forLanguageTag(locale.toLanguageTag()))
}

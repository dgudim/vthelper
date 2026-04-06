package noorg.kloud.vthelper

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import com.kizitonwose.calendar.compose.CalendarItemInfo
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

val YearMonth.next: YearMonth get() = this.plus(1, DateTimeUnit.MONTH)
val YearMonth.previous: YearMonth get() = this.minus(1, DateTimeUnit.MONTH)

/**
 * Find the first month on the calendar visible up to the given [viewportPercent] size.
 */
@Composable
fun rememberFirstMostVisibleMonth(
    state: CalendarState,
    viewportPercent: Float = 50f,
): CalendarMonth {
    var visibleMonth by remember(state) { mutableStateOf(state.firstVisibleMonth) }
    // This launches once per calendar config and updates the values from BG
    LaunchedEffect(state) {
        // https://efeejemudaro.medium.com/firing-side-effects-from-compose-using-snapshotflow-e3581c624adb
        // https://freedium-mirror.cfd/https://medium.com/@ramadan123sayed/understanding-snapshotflow-in-jetpack-compose-converting-state-to-flow-69b961282694
        snapshotFlow { state.layoutInfo.firstMostVisibleMonth(viewportPercent) }
            .filterNotNull()
            .collect { month -> visibleMonth = month }
    }
    return visibleMonth
}

private fun CalendarLayoutInfo.firstMostVisibleMonth(viewportPercent: Float): CalendarMonth? {
    return if (visibleMonthsInfo.isEmpty()) {
        null
    } else {
        val viewportSize = (viewportEndOffset + viewportStartOffset) * viewportPercent / 100f
        visibleMonthsInfo.firstOrNull { itemInfo ->
            if (itemInfo.offset < 0) {
                itemInfo.offset + itemInfo.size >= viewportSize
            } else {
                itemInfo.size - itemInfo.offset >= viewportSize
            }
        }?.month
    }
}

fun Color.setAlpha(newAlpha: Float): Color {
    return copy(
        alpha = newAlpha
    )
}

fun Regex.findFirstGroup(str: String): String? {
    return find(str)?.groupValues?.get(1)?.trim()
}

fun Throwable.fullMessage(): String {
    var currentThrowable: Throwable? = this
    var message = ""
    while (currentThrowable != null) {
        val msgPart = "${currentThrowable::class} (${currentThrowable.message})"
        if (message.isEmpty()) {
            message = msgPart
        } else {
            message += " caused by $msgPart"
        }
        currentThrowable = currentThrowable.cause
    }
    return message;
}
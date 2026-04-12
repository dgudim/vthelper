package noorg.kloud.vthelper

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import io.ktor.util.cio.use
import io.ktor.utils.io.asByteWriteChannel
import io.ktor.utils.io.writeByteArray
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import noorg.kloud.vthelper.ui.theme.CustomColorPalette
import kotlin.io.encoding.Base64
import kotlin.random.Random

typealias SnackbarFun = (String, SnackBarSeverityLevel, SnackbarDuration) -> Unit

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

fun getHashedColor(num: Long): Color {
    val randomGenerator = Random(num)
    val resolution = 1024
    return Color(
        red = randomGenerator.nextInt(0, resolution * 255) / resolution,
        green = randomGenerator.nextInt(0, resolution * 255) / resolution,
        blue = randomGenerator.nextInt(0, resolution * 255) / resolution,
        alpha = 1
    )
}

fun Color.mixWith(other: Color, ratioRaw: Float): Color {
    val ratio = ratioRaw.coerceIn(0F, 1F)
    return Color(
        red = red * (1 - ratio) + other.red * ratio,
        green = green * (1 - ratio) + other.green * ratio,
        blue = blue * (1 - ratio) + other.blue * ratio,
    )
}

fun CustomColorPalette.getColorFromGrade(grade: Float?): Color {
    return badResult.mixWith(goodResult, ((grade ?: 0F) - 5F) / 5F)
}

fun Regex.findFirstGroup(str: String): String? {
    return find(str)?.groupValues?.get(1)?.trim()
}

fun String.toIntNotNull(): Int {
    return toIntDashAsNull() ?: 0
}

fun String.toIntDashAsNull(): Int? {
    if (trim() == "-") {
        return null
    }
    try {
        return toInt()
    } catch (_: NumberFormatException) {
        println("Error converting $this to int")
    }
    return 0
}

fun String.toFloatNotNull(): Float {
    return toFloatDashAsNull() ?: 0F
}

fun String.toFloatDashAsNull(): Float? {
    if (trim() == "-") {
        return null
    }
    try {
        return replace(",", ".").toFloat()
    } catch (_: NumberFormatException) {
        println("Error converting $this to float")
    }
    return 0F
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
    // Some messages get replaced with this, remove
    return message.replace("(Kotlin reflection is not available)", "")
}

suspend fun String.decodeBase64ToFile(targetPath: Path) {
    try {
        val rawdata = split(",").last() // Remove the data:<datatype>, prefix
        SystemFileSystem.sink(targetPath, false)
            .asByteWriteChannel()
            .use {
                writeByteArray(Base64.decode(rawdata))
            }
    } catch (e: Exception) {
        println("Error decoding base64 to image: $e")
    }
}
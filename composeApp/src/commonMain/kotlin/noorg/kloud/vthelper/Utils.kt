package noorg.kloud.vthelper

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.compositeOver
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import io.ktor.util.cio.use
import io.ktor.utils.io.asByteWriteChannel
import io.ktor.utils.io.core.writeText
import io.ktor.utils.io.readText
import io.ktor.utils.io.writeByteArray
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearsUntil
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import noorg.kloud.vthelper.data.dbentities.mano.DBManoBareEmployeeData
import noorg.kloud.vthelper.data.dbentities.mano.DBManoEmployeeEntity
import noorg.kloud.vthelper.ui.components.SnackBarSeverityLevel
import noorg.kloud.vthelper.ui.theme.CustomColorPalette
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

typealias SnackbarFun = (String, SnackBarSeverityLevel, SnackbarDuration) -> Unit

// ============================= CALENDAR

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


// ============================= COLORS
fun Color.setAlpha(newAlpha: Float): Color {
    return copy(
        alpha = newAlpha
    )
}

fun getHashedColor(num: Long): Color {
    val randomGenerator = Random(num)
    return Color(
        red = randomGenerator.nextInt(0, 256),
        green = randomGenerator.nextInt(0, 256),
        blue = randomGenerator.nextInt(0, 256),
        alpha = 255
    )
}

fun Color.mixWith(other: Color, ratioRaw: Float): Color {
    val ratio = ratioRaw.coerceIn(0F, 1F)

    val other = other.convert(ColorSpaces.Bt2020).setAlpha(ratio)
    val thisCol = convert(ColorSpaces.Bt2020)

    return other.compositeOver(thisCol)
}

fun CustomColorPalette.getColorFromGrade(grade: Float?): Color {
    return badResult.mixWith(goodResult, ((grade ?: 0F) - 5F) / 5F)
}

@Composable
fun Color.mixedWithPrimary(): Color {
    return mixWith(MaterialTheme.colorScheme.primary, 0.3F)
}

// ============================= GENERAL STR

fun Regex.findFirstGroup(str: String): String? {
    return try {
        find(str)?.groupValues?.get(1)?.trim()
    } catch (_: Exception) {
        null
    }
}

fun String.nullIfDash(): String? {
    if (trim() == "-") {
        return null
    }
    return this
}

fun String.nullIfBlank(): String? {
    if (isBlank()) {
        return null
    }
    return this
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

// ============================= IO

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

fun writeFile(targetPath: Path, content: String) {
    SystemFileSystem.sink(targetPath).use { rawSink ->
        rawSink.buffered().use { it.writeText(content) }
    }
}

fun readFile(targetPath: Path): String {
    SystemFileSystem.source(targetPath).use { rawSource ->
        rawSource.buffered().use { return it.readText() }
    }
}

// ============================= DB HELPERS

fun List<DBManoBareEmployeeData>.fuzzyFindEmployee(lecturerName: String?): DBManoBareEmployeeData? {
    if (lecturerName == null) {
        return null
    }

    val cleanedLecturerName = lecturerName
        .lowercase()
        .replace("doc.", "")
        .replace("dr.", "")
        .trim()

    val matcher = Regex(
        cleanedLecturerName.replace(".", ".*?")
    )
    for (employee in this) {
        val employeeNameLowercase = employee.shortName.lowercase()
        if (cleanedLecturerName == employeeNameLowercase || matcher.containsMatchIn(
                employeeNameLowercase
            )
        ) {
            return employee
        }
    }
    return null
}

// ============================= OTHER HELPERS

fun getSemesterYearRange(currentSemesterSequenceNum: Int, targetSemesterSequenceNum: Int): String {
    // TODO: Use actual timetable here, this will fail during the winter session
    // Does mano switch to the next semester during the winter session or after it?
    val currentYear =
        Instant.fromEpochMilliseconds(0) // Account for semester ending in the next year
            .yearsUntil(Clock.System.now().minus(40.days), TimeZone.currentSystemDefault())
            .plus(1970)

    val semesterYearDifference = currentSemesterSequenceNum - targetSemesterSequenceNum
    val targetSemesterYear = currentYear - semesterYearDifference.floorDiv(2)
    return "${targetSemesterYear - 1}-${targetSemesterYear}"
}

fun getSemesterSessionSeason(semesterSequenceNum: Int): String {
    return if (semesterSequenceNum % 2 == 0) {
        "spring"
    } else {
        "winter"
    }
}

fun Int.toRelativeSemester(): Int {
    return (this + 1) % 2 + 1
}
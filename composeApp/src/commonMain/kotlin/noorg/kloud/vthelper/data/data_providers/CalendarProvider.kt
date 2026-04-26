package noorg.kloud.vthelper.data.data_providers

import io.ktor.http.Url
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.parse
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import noorg.kloud.vthelper.api.MoodleApi
import noorg.kloud.vthelper.api.downloadFile
import noorg.kloud.vthelper.api.models.toResultFail
import noorg.kloud.vthelper.api.models.toResultOk
import noorg.kloud.vthelper.data.local_models.LocalCalendarEventType
import noorg.kloud.vthelper.data.local_models.LocalMoodleCalendarEvent
import noorg.kloud.vthelper.platform_specific.appDataDirectory
import noorg.kloud.vthelper.platform_specific.div
import noorg.kloud.vthelper.readFile
import noorg.kloud.vthelper.writeFile
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class CalendarProvider {

    private val appDataDir = appDataDirectory()

    private val icsFileLocation = appDataDir / "moodle-calendar.ics"
    private val markerFileLocation = appDataDir / "moodle-calendar-marker.txt"

    // https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/kotlinx.datetime.format/parse.html
    // https://github.com/ical4j/ical4j/blob/develop/src/main/java/net/fortuna/ical4j/model/DateTime.java
    val iCalUTCFormat = DateTimeComponents.Format {
        year(); monthNumber(); day()
        char('T')
        hour(); minute(); second()
        char('Z')
    }


    fun loadFromFileIfAvailable(): Result<List<LocalMoodleCalendarEvent>> {
        if (SystemFileSystem.exists(icsFileLocation)) {
            return loadFromFile(icsFileLocation)
        }
        return "ICS is not available".toResultFail()
    }

    suspend fun fetchMoodleEventsIfNeeded(expiryDuration: Duration): Result<List<LocalMoodleCalendarEvent>> {

        var lastModTimeMs = 0L
        if (SystemFileSystem.exists(markerFileLocation)) {
            lastModTimeMs = readFile(markerFileLocation).toLongOrNull() ?: 0
        }

        if (Clock.System.now() - Instant.fromEpochMilliseconds(lastModTimeMs) >= expiryDuration) {
            val url = MoodleApi
                .getCalendarUrl()
                .onFailure { return toResultFail() }

            downloadFile(icsFileLocation, Url(url.bodyTyped ?: ""))
            writeFile(markerFileLocation, "${Clock.System.now().toEpochMilliseconds()}")
        }

        return loadFromFile(icsFileLocation)
    }

    fun getEventType(title: String, description: String): LocalCalendarEventType {
        val strToMatch = "${title.lowercase()} ${description.lowercase()}"
        if (strToMatch.contains("homework")
            || strToMatch.contains("essay")
            || strToMatch.contains("task")
        ) {
            return LocalCalendarEventType.ASSIGNMENT
        }
        if (strToMatch.contains("midterm")
            || strToMatch.contains("exam")
        ) {
            return LocalCalendarEventType.TIMETABLE
        }
        if (strToMatch.contains("attendance")) {
            return LocalCalendarEventType.ATTENDANCE
        }
        return LocalCalendarEventType.OTHER
    }

    fun loadFromFile(filepath: Path): Result<List<LocalMoodleCalendarEvent>> {
        try {
            val list = mutableListOf<LocalMoodleCalendarEvent>()

            val icsStr = readFile(filepath)

            var currentEventTitle = ""
            var currentEventDescription = ""
            var currentEventCourseModCode = ""
            var currentEventStart = Clock.System.now()
            var currentEventEnd = Clock.System.now()

            var inEvent = false

            var currentLineBuffer = ""

            val allLines = icsStr.trim().filterNot { it == '\r' }.split('\n')
            val numLines = allLines.size

            for ((index, line) in allLines.withIndex()) {
                val nextLine = if (index < numLines - 1) allLines[index + 1] else ""

                currentLineBuffer += line

                // lines starting with tabs are continuations of the previous line
                if (nextLine.startsWith("\t")) {
                    continue
                }

                currentLineBuffer = currentLineBuffer
                    .replace("\t", "")
                    .replace("\\n", "\n")
                    .replace("\\", "")
                    .replace(" ", " ")
                    .trim { it == ' ' || it == '\n' }

                if (currentLineBuffer == "BEGIN:VEVENT") {
                    inEvent = true
//                    println("EVENT START")
                }
                if (currentLineBuffer == "END:VEVENT") {
//                    println("EVENT PARSED: $currentEventTitle\n\n")
                    inEvent = false
                    list.add(
                        LocalMoodleCalendarEvent(
                            eventType = getEventType(currentEventTitle, currentEventDescription),
                            title = currentEventTitle,
                            description = currentEventDescription,
                            startTime = currentEventStart,
                            endTime = currentEventEnd,
                            courseModCode = currentEventCourseModCode
                        )
                    )
                    currentEventTitle = ""
                    currentEventDescription = ""
                    currentEventCourseModCode = ""
                    currentEventStart = Clock.System.now()
                    currentEventEnd = Clock.System.now()
                }
                if (inEvent) {
                    when {
                        currentLineBuffer.startsWith("CATEGORIES:") -> {
                            currentEventCourseModCode =
                                currentLineBuffer.removePrefix("CATEGORIES:").split(",").first()
                        }

                        currentLineBuffer.startsWith("SUMMARY:") -> {
                            currentEventTitle = currentLineBuffer.removePrefix("SUMMARY:")
                        }

                        currentLineBuffer.startsWith("DESCRIPTION:") -> {
                            currentEventDescription = currentLineBuffer.removePrefix("DESCRIPTION:")
                        }

                        currentLineBuffer.startsWith("DTSTART:") -> {
                            currentEventStart = Instant.parse(
                                currentLineBuffer.removePrefix("DTSTART:"),
                                iCalUTCFormat
                            )
//                            println("Set event start DT: $currentEventStart")
                        }

                        currentLineBuffer.startsWith("DTEND:") -> {
                            currentEventEnd = Instant.parse(
                                currentLineBuffer.removePrefix("DTEND:"),
                                iCalUTCFormat
                            )
//                            println("Set event end DT: $currentEventEnd")
                        }
                    }

                }
                currentLineBuffer = ""
            }

            println("Parsed ${list.size} events from moodle ical")

            return list.toResultOk()
        } catch (e: Exception) {
            return e.toResultFail()
        }
    }

}
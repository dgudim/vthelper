package noorg.kloud.vthelper.data.local_models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import noorg.kloud.vthelper.platform_specific.toSystemLocalDt
import kotlin.time.Instant

enum class LocalCalendarEventType {
    ASSIGNMENT, TIMETABLE, ANNOUNCEMENT, ATTENDANCE, OTHER
}

abstract class LocalCalendarEvent(
    open val startTime: Instant,
    open val endTime: Instant,

    open val eventType: LocalCalendarEventType,
    open val title: String,
    open val description: String
) {

    val startLocalDt by lazy { startTime.toSystemLocalDt() }
    val endLocalDt by lazy { endTime.toSystemLocalDt() }

    abstract fun isVisibleOnDate(date: LocalDate): Boolean

    abstract fun getColor(): Color
    abstract fun getSubtext(): String
}
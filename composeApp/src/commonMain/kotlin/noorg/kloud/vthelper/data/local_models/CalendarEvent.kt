package noorg.kloud.vthelper.data.local_models

import androidx.compose.ui.graphics.Color
import kotlin.time.Instant

data class CalendarEvent(
    val eventType: CalendarEventType,
    val title: String,
    val description: String,
    val color: Color,
    val linkedMetaCourseId: Long,
    val startTime: Instant,
    val endTime: Instant
) {
}
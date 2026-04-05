package noorg.kloud.vthelper.data.local_models

import androidx.compose.ui.graphics.Color
import kotlin.time.Instant

data class LocalCalendarEvent(
    val eventType: LocalCalendarEventType,
    val title: String,
    val description: String,
    val color: Color,
    val linkedMetaCourseId: Long,
    val startTime: Instant,
    val endTime: Instant
)
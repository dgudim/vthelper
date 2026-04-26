package noorg.kloud.vthelper.data.local_models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class LocalManoCalendarEvent(
    override val startTime: Instant,
    override val endTime: Instant,
    override val eventType: LocalCalendarEventType,
    override val title: String,
    override val description: String
) : LocalCalendarEvent(startTime, endTime, eventType, title, description) {
    override fun isVisibleOnDate(date: LocalDate): Boolean {
        TODO("Not yet implemented")
    }

    override fun getColor(): Color {
        TODO("Not yet implemented")
    }

    override fun getSubtext(): String {
        TODO("Not yet implemented")
    }
}
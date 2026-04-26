package noorg.kloud.vthelper.data.local_models

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import noorg.kloud.vthelper.data.provider_models.ProvidedMoodleCourseEntity
import noorg.kloud.vthelper.mixWith
import kotlin.time.Instant

data class LocalMoodleCalendarEvent(
    val courseModCode: String? = null,
    private var moodleCourse: ProvidedMoodleCourseEntity? = null,

    override val startTime: Instant,
    override val endTime: Instant,

    override val eventType: LocalCalendarEventType,
    override val title: String,
    override val description: String
) : LocalCalendarEvent(startTime, endTime, eventType, title, description) {

    fun setLinkedMoodleCourse(course: ProvidedMoodleCourseEntity?) {
        moodleCourse = course
    }

    override fun isVisibleOnDate(date: LocalDate): Boolean {
        return startLocalDt.date == date || endLocalDt.date == date
    }

    override fun getColor(): Color {
        moodleCourse?.let { return it.color }
        return Color.Gray
    }

    override fun getSubtext(): String {
        moodleCourse?.let { return "Moodle: ${it.title}" }
        return ""
    }
}
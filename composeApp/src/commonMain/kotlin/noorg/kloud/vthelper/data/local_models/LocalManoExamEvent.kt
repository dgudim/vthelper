package noorg.kloud.vthelper.data.local_models

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import noorg.kloud.vthelper.data.provider_models.ProvidedManoExamTimetableEvent

class LocalManoExamEvent(
    val backingField: ProvidedManoExamTimetableEvent
) : LocalCalendarEvent(
    backingField.examDateTime,
    backingField.examDateTime,
    LocalCalendarEventType.EXAM,
    "${backingField.subjectName} exam",
    backingField.examClassroom
) {

    override fun isVisibleOnDate(date: LocalDate): Boolean {
        return startLocalDt.date == date
    }

    override fun getColor(): Color {
        return backingField.color
    }

    override fun getSubtext(): String {
        return "Type: ${backingField.examType}, ${backingField.examCredits} credits. Lecturer: ${backingField.examLecturerFullName}"
    }
}
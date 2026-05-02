package noorg.kloud.vthelper.data.provider_models

import androidx.compose.ui.graphics.Color
import kotlin.time.Instant

data class ProvidedManoExamTimetableEvent(
    val subjectName: String,
    val subjectModCode: String,

    val examType: String,
    val examDateTime: Instant,
    val examClassroom: String,
    val examLecturerFullName: String,
    val examCredits: Int,

    val color: Color,

    val consultationDateTime: Instant?,
    val consultationClassroom: String?
)

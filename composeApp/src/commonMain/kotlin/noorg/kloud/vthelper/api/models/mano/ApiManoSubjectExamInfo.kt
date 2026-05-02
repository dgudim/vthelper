package noorg.kloud.vthelper.api.models.mano

import kotlin.time.Instant

data class ApiManoSubjectExamInfo(
    val subjectName: String,
    val subjectModCode: String,

    val examType: String, // TODO: Map this,
    val examDateTime: Instant,
    val examClassroom: String,
    val examLecturerFullName: String,
    val examCredits: Int,

    val consultationDateTime: Instant?,
    val consultationClassroom: String?
)

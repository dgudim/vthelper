package noorg.kloud.vthelper.api.models.mano

data class ApiManoCourseEntity (
    val subjectModId: String,
    val subjectModCode: String,
    val subjectLink: String,
    val subjectName: String,
    val subjectLecturerFullName: String,
    val subjectEvaluationCode: String, // TODO: Enum and/or display meaning
    val subjectCredits: Int
)
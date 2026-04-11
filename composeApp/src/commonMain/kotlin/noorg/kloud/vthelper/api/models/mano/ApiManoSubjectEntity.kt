package noorg.kloud.vthelper.api.models.mano

enum class ApiManoSubjectEvaluationType {
}

data class ApiManoSubjectEntity(
    val modId: String,
    val modCode: String,
    val link: String,
    val name: String,
    val lecturerFullName: String,
    val evaluationType: String, // TODO: Enum and/or display meaning
    val credits: Int
)
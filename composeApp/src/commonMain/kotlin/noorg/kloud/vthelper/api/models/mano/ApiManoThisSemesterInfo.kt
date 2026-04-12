package noorg.kloud.vthelper.api.models.mano


data class ApiManoThisSemesterSubjectEntity(
    val modId: String,
    val modCode: String,
    val link: String,
    val name: String,
    val lecturerFullName: String,
    val evaluationType: String, // TODO: Enum and/or display meaning
    val credits: Int
)

data class ApiManoThisSemesterInfo(
    val absoluteSequenceNum: Int,
    val studyProgram: String,
    val group: String,
    val subjects: List<ApiManoThisSemesterSubjectEntity>
)

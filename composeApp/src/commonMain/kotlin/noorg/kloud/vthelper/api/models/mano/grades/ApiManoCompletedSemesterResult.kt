package noorg.kloud.vthelper.api.models.mano.grades


// https://mano.vilniustech.lt/results/site
enum class ApiManoSubjectEvaluationVerdict(v: String) {
    PASS("T"),
    FAIL("R"),
    FAILED_TO_APPEAR("P"), // P - failed to disappear
    FAILED_DISHONESTY("N"), // N - not certified due to dishonesty
    FAILED_UNMETHODICAL("G"), // G - not certified due to unmethodical work
}

data class ApiManoSubjectFinalResult(
    val modId: String,
    val modCode: String,
    val link: String,

    val name: String,
    val lecturerFullName: String,

    val credits: Int,
    val hours: Int,
    val tries: Int,

    val completionDate: String,
    val evaluationVerdict: ApiManoSubjectEvaluationVerdict,
)

data class ApiManoCompletedSemesterResult(
    val semesterAbsoluteSequenceNum: Int,
    val sessionName: String,
    val yearTimeSpan: String,

    val finalResults: MutableList<ApiManoSubjectFinalResult>,
    val finalTotalCredits: Int,
    val finalWeightedGrade: Float
)

package noorg.kloud.vthelper.api.models.mano.grades


// https://mano.vilniustech.lt/results/site
enum class ApiManoSubjectEvaluationVerdict(val internal: String) {
    PASS("T"),
    FAIL("R"),
    FAILED_TO_APPEAR("P"), // P - failed to disappear
    FAILED_DISHONESTY("N"), // N - not certified due to dishonesty
    FAILED_UNMETHODICAL("G"), // G - not certified due to unmethodical work
}

val EvaluationVerdictLookup = mapOf(
    "T" to ApiManoSubjectEvaluationVerdict.PASS,
    "R" to ApiManoSubjectEvaluationVerdict.FAIL,
    "P" to ApiManoSubjectEvaluationVerdict.FAILED_TO_APPEAR,
    "N" to ApiManoSubjectEvaluationVerdict.FAILED_DISHONESTY,
    "G" to ApiManoSubjectEvaluationVerdict.FAILED_UNMETHODICAL,
)

data class ApiManoSubjectFinalResult(
    val modId: String,
    val modCode: String,
    val link: String,

    val name: String,
    val lecturerShortName: String,

    val credits: Int,
    val hours: Int,
    val tries: Int,
    val grade: Int,

    val completionDate: String,
    val evaluationVerdict: ApiManoSubjectEvaluationVerdict,
)

data class ApiManoCompletedSemesterResult(
    val absoluteSequenceNum: Int,
    val sessionSeason: String,
    val group: String,
    val yearTimeSpan: String,

    val finalResults: MutableList<ApiManoSubjectFinalResult>,
    val finalTotalCredits: Int,
    val finalWeightedGrade: Float
)

package noorg.kloud.vthelper.data.provider_models

enum class ProvidedManoSubjectEvaluationVerdict(v: String) {
    PASS("T"),
    FAIL("R"),
    FAILED_TO_APPEAR("P"), // P - failed to disappear
    FAILED_DISHONESTY("N"), // N - not certified due to dishonesty
    FAILED_UNMETHODICAL("G"), // G - not certified due to unmethodical work
}

data class ProvidedManoSubjectEntity(
    val modId: String,
    val modCode: String,
    val link: String,

    // Inlined from the db so the ui doesn't have to join lists
    val lecturerName: String,
    val lecturerId: Long,

    val name: String,

    // Available only after fetching mediate results
    val taGaSplitPercentage: String? = null,

    // After completing this subject
    val tries: Int? = null,
    val hours: Int? = null,
    val credits: Int? = null,

    val finalCompletionDate: String? = null,
    val finalCompletionGrade: Int? = null,
    val finalCompletionCumulativeScore: String? = null,
    val finalCompletionCreditScore: String? = null,
    val finalEvaluationVerdict: ProvidedManoSubjectEvaluationVerdict? = null,
)

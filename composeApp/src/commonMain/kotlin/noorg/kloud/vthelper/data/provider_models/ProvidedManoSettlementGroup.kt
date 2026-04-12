package noorg.kloud.vthelper.data.provider_models

data class ProvidedManoSettlementGrade (
    val name: String,
    val value: Int,
    val date: String,

    // Inlined from the db so the ui doesn't have to join lists
    val graderShortName: Long,
    // For the 'employee details' dialog
    val graderId: Long
)

data class ProvidedManoSettlementGroup (
    val internalId: Long,

    val settlementType: String,

    val completedRatio: String,

    val grades: List<ProvidedManoSettlementGrade>,

    val percentageOfFinalAssessment: Int,
    val finalGrade: Float?,
    val finalCumulativeScore: Float?,
    val lastUpdatedDate: String?
)

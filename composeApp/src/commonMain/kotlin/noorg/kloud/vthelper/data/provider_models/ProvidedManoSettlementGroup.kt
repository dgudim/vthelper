package noorg.kloud.vthelper.data.provider_models

data class ProvidedManoSettlementGroup (
    val internalId: Long,

    val settlementType: String,

    val completedRatio: String,

    val percentageOfFinalAssessment: Int,
    val finalGrade: Float?,
    val finalCumulativeScore: Float?,
    val lastUpdatedDate: String?
)

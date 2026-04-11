package noorg.kloud.vthelper.api.models.mano.grades

// https://mano.vilniustech.lt/results/site#/mediate-modules-list-cont
// Overview for Homeworks, Labs, Test, etc
data class ApiManoSubjectSettlementOverview(
    val settlementType: String,

    val semesterYearRange: String,
    val semesterRelativeSequenceNum: Int,

    val subjectModId: String,
    val subjectKmdId: String, // This and destVart are for requesting all grades
    val subjectDestVart: String,
    val subjectName: String,

    val lecturerName: String,
    val completedRatio: String, // 1/2 or 2/3, etc.
    val percentageOfFinalAssessment: Int,
    val finalGrade: Float?,
    val cumulativeScore: Float?,
    val lastUpdatedDate: String?
)
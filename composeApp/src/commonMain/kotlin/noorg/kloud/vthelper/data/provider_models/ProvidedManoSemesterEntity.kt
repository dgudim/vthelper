package noorg.kloud.vthelper.data.provider_models

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class ProvidedManoSemesterEntity(
    val absoluteSequenceNum: Int,

    val isCurrent: Boolean,

    val group: String,
    val studyProgram: String,
    val season: String,
    val yearRange: String,

    // After completing the semester
    val finalTotalCredits: Int?,
    val finalWeightedGrade: Float?
)
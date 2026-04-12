package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "mano_semesters"
)
data class DBManoSemesterEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "seq")
    val absoluteSequenceNum: Int = 0, // 1 - to 8 or to 10 usually

    @ColumnInfo(name = "is_current")
    val isCurrent: Boolean,

    @ColumnInfo(name = "group")
    val group: String,
    // No available directly, not filled for completed semester
    @ColumnInfo(name = "study_program")
    val studyProgram: String? = null,

    // No available directly, not filled for current semester
    @ColumnInfo(name = "season")
    val season: String? = null,
    @ColumnInfo(name = "year_range")
    val yearRange: String? = null,

    // After completing the semester
    @ColumnInfo(name = "final_total_credits")
    val finalTotalCredits: Int? = null,
    @ColumnInfo(name = "final_weighted_grade")
    val finalWeightedGrade: Float? = null
)
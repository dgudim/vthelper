package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class DBManoSubjectEvaluationVerdict(v: String) {
    PASS("T"),
    FAIL("R"),
    FAILED_TO_APPEAR("P"), // P - failed to disappear
    FAILED_DISHONESTY("N"), // N - not certified due to dishonesty
    FAILED_UNMETHODICAL("G"), // G - not certified due to unmethodical work
}

@Entity(
    tableName = "mano_subjects",
    foreignKeys = [
        ForeignKey(
            entity = DBManoSemesterEntity::class,
            parentColumns = arrayOf("seq"),
            childColumns = arrayOf("semester_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DBManoEmployeeEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("lecturer_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DBManoSubjectEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "mod_id")
    val modId: String,
    @ColumnInfo(name = "mod_code")
    val modCode: String,
    @ColumnInfo(name = "link")
    val link: String,

    // FK to mano_semesters
    @ColumnInfo(name = "semester_id")
    val semesterId: Long,

    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "lecturer_id")
    val lecturerId: Long,

    @ColumnInfo(name = "credits")
    val credits: Int,
    @ColumnInfo(name = "hours")
    val hours: Int,
    @ColumnInfo(name = "ta_ga_split_percentage")
    val taGaSplitPercentage: String,

    // After completing this subject
    @ColumnInfo(name = "tries")
    val tries: Int?,
    @ColumnInfo(name = "final_completion_date")
    val finalCompletionDate: String?,
    @ColumnInfo(name = "final_completion_grade")
    val finalCompletionGrade: String?,
    @ColumnInfo(name = "final_completion_cumulative_score")
    val finalCompletionCumulativeScore: String?,
    @ColumnInfo(name = "final_completion_credit_score")
    val finalCompletionCreditScore: String?,
    @ColumnInfo(name = "final_evaluation_verdict")
    val finalEvaluationVerdict: DBManoSubjectEvaluationVerdict?,
)
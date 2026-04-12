package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

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
            childColumns = arrayOf("semester_seq"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DBManoEmployeeEntity::class,
            parentColumns = arrayOf("mano_id"),
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
    @ColumnInfo(name = "semester_seq")
    val semesterSequence: Int,

    // FK to mano_employees
    @ColumnInfo(name = "lecturer_id")
    val lecturerId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    // Available only after fetching mediate results
    @ColumnInfo(name = "ta_ga_split_percentage")
    val taGaSplitPercentage: String? = null,

    // After completing this subject
    @ColumnInfo(name = "tries")
    val tries: Int? = null,
    @ColumnInfo(name = "hours")
    val hours: Int? = null,
    // Fetchable from some other section, but right now only after completing the subject
    // TODO: Fetch this and make not null
    @ColumnInfo(name = "credits")
    val credits: Int? = null,

    @ColumnInfo(name = "final_completion_date")
    val finalCompletionDate: String? = null,
    @ColumnInfo(name = "final_completion_grade")
    val finalCompletionGrade: Int? = null,
    @ColumnInfo(name = "final_completion_cumulative_score")
    val finalCompletionCumulativeScore: String? = null,
    @ColumnInfo(name = "final_completion_credit_score")
    val finalCompletionCreditScore: String? = null,
    @ColumnInfo(name = "final_evaluation_verdict")
    val finalEvaluationVerdict: DBManoSubjectEvaluationVerdict? = null,
)

// https://developer.android.com/training/data-storage/room/relationships/one-to-one
// https://medium.com/androiddevelopers/7-pro-tips-for-room-fbadea4bfbd1
// https://proandroiddev.com/room-database-lessons-learnt-from-working-with-multiple-tables-d499c9be94ce

data class DBManoSubjectEntityWithEmployee(
    @Embedded
    val subject: DBManoSubjectEntity,
    @Relation(
        parentColumn = "lecturer_id",
        entityColumn = "mano_id"
    )
    val employee: DBManoEmployeeEntity
)
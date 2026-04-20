package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "mano_settlement_groups",
    foreignKeys = [
        ForeignKey(
            entity = DBManoSubjectEntity::class,
            parentColumns = arrayOf("mod_id"),
            childColumns = arrayOf("subject_mod_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class DBManoSettlementGroup (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val syntheticId: Long = 0,

    @ColumnInfo(name = "settlement_type")
    val settlementType: String,

    @ColumnInfo(name = "subject_mod_id")
    val subjectModId: Long,

    @ColumnInfo(name = "completed_ratio")
    val completedRatio: String,

    @ColumnInfo(name = "final_assessment_percentage")
    val percentageOfFinalAssessment: Int,
    @ColumnInfo(name = "final_grade")
    val finalGrade: Float?,
    @ColumnInfo(name = "final_cumulative_score")
    val finalCumulativeScore: Float?,

    @ColumnInfo(name = "last_updated")
    val lastUpdatedDate: String?
)

data class DBManoSettlementGroupWithGrades(
    @Embedded
    val group: DBManoSettlementGroup,
    @Relation(
        // https://developer.android.com/training/data-storage/room/relationships/nested
        // Important. Used by room to determine the underlying entity for DbManoSettlementGradeWithEmployee
        entity = DbManoSettlementGrade::class,
        parentColumn = "id",
        entityColumn = "settlement_id"
    )
    val grades: List<DbManoSettlementGradeWithEmployee>
)

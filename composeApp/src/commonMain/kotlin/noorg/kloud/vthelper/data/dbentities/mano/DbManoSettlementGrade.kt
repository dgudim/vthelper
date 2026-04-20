package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "mano_settlement_grades",
    foreignKeys = [
        ForeignKey(
            entity = DBManoSettlementGroup::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("settlement_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DBManoEmployeeEntity::class,
            parentColumns = arrayOf("mano_id"),
            childColumns = arrayOf("grader_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbManoSettlementGrade(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val syntheticId: Long = 0,

    @ColumnInfo(name = "settlement_id")
    val settlementId: Long,

    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "value")
    val value: Int,
    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "grader_id")
    val graderId: Long
)

data class DbManoSettlementGradeWithEmployee(
    @Embedded
    val grade: DbManoSettlementGrade,
    @Relation(
        parentColumn = "grader_id",
        entityColumn = "mano_id"
    )
    val employee: DBManoEmployeeEntity
)


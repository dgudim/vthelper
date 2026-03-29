package noorg.kloud.vthelper.data.dbentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class DBManoTimetableEntityType(val s: String) {
    LECTURE("lecture"), PRACTICE("practice"), LAB("lab")
}

enum class DBManoTimetableEntityWeek(val w: Int) {
    ALL(0), FIRST(1), SECOND(2)
}

@Entity(
    tableName = "mano_timetable",
    foreignKeys = [
        ForeignKey(
            entity = DBManoCourseEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("course_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DBManoCourseTimetableEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "course_id")
    val courseId: Long = 0,

    @ColumnInfo(name = "time_from_ms_utc")
    val timeFromMsUTC: Long,
    @ColumnInfo(name = "time_to_ms_utc")
    val timeToMsUTC: Long,

    @ColumnInfo(name = "type")
    val type: DBManoTimetableEntityType,

    @ColumnInfo(name = "student_group")
    val studentGroup: Int,
    @ColumnInfo(name = "week")
    val week: DBManoTimetableEntityWeek
)
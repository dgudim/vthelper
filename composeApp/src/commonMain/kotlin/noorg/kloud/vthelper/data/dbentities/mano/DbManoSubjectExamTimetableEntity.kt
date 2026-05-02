package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "mano_exam_timetable"
)
data class DbManoSubjectExamTimetableEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "absolute_seq")
    val id: Int = 0,

    @ColumnInfo(name = "subject_name")
    val subjectName: String,
    @ColumnInfo(name = "subject_mod_code")
    val subjectModCode: String,

    @ColumnInfo(name = "exam_type")
    val examType: String,
    @ColumnInfo(name = "exam_ms_utc")
    val examDateTimeMsUTC: Long,
    @ColumnInfo(name = "exam_classroom")
    val examClassroom: String,
    @ColumnInfo(name = "exam_lecturer_full_name")
    val examLecturerFullName: String,
    @ColumnInfo(name = "exam_credits")
    val examCredits: Int,

    @ColumnInfo(name = "consultation_ms_utc")
    val consultationDateTimeMsUTC: Long?,
    @ColumnInfo(name = "consultation_classroom")
    val consultationClassroom: String?
)
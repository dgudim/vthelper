package noorg.kloud.vthelper.data.dbentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moodle_courses")
data class DBMoodleCourseEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "moodle_id")
    val moodleId: Long,

    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "view_url")
    val viewUrl: String,

    @ColumnInfo(name = "cover_image_path")
    val coverImagePath: String
)
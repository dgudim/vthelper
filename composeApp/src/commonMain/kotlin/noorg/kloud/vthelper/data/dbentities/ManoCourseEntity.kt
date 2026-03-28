package noorg.kloud.vthelper.data.dbentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "mano_courses",
    foreignKeys = [
        ForeignKey(
            entity = LecturerEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("lecturer_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ManoCourseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "mano_id")
    val manoId: Long = 0,
    @ColumnInfo(name = "subject")
    val subject: String,
    @ColumnInfo(name = "lecturer_id")
    val lecturerId: Long,
    @ColumnInfo(name = "cover_image_path")
    val coverImagePath: String
)
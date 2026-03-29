package noorg.kloud.vthelper.data.dbentities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "meta_courses",
    foreignKeys = [
        ForeignKey(
            entity = DBManoCourseEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("internal_mano_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DBMoodleCourseEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("internal_moodle_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DBMetaCourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "internal_moodle_id")
    val localMoodleCourseId: Long,
    @ColumnInfo(name = "internal_mano_id")
    val localManoCourseId: Long,

    @ColumnInfo(name = "custom_name")
    val customName: String,

    @ColumnInfo(name = "color")
    val color: Long
)
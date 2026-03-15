package noorg.kloud.vthelper.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "lecturers"
)
data class LecturerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "fullname")
    val fullname: String,
    @ColumnInfo(name = "mano_id")
    val manoId: String
)
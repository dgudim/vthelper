package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "mano_employees"
)
data class DBManoEmployeeEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val manoId: Long = 0,

    @ColumnInfo(name = "fullname")
    val fullname: String,
    @ColumnInfo(name = "shortname")
    val shortname: String,

    @ColumnInfo(name = "positions")
    val positions: String,
    @ColumnInfo(name = "departments")
    val departments: String,
    @ColumnInfo(name = "phones")
    val phones: String,
    @ColumnInfo(name = "emails")
    val emails: String,
    @ColumnInfo(name = "offices")
    val offices: String,

    @ColumnInfo(name = "link")
    val link: String
)
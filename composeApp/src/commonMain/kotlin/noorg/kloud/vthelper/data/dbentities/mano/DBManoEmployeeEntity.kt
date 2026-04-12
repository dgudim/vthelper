package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "mano_employees"
)
data class DBManoEmployeeEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "mano_id")
    val manoId: Long,

    @ColumnInfo("avatar_path")
    val avatarPath: String? = null,

    @ColumnInfo(name = "fullname")
    val fullName: String? = null,
    @ColumnInfo(name = "shortname")
    val shortName: String,

    @ColumnInfo(name = "positions")
    val positions: String? = null,
    @ColumnInfo(name = "departments")
    val departments: String? = null,
    @ColumnInfo(name = "phones")
    val phones: String? = null,
    @ColumnInfo(name = "emails")
    val emails: String? = null,
    @ColumnInfo(name = "offices")
    val offices: String? = null
)
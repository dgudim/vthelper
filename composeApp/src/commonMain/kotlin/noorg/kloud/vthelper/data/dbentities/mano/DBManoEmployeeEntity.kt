package noorg.kloud.vthelper.data.dbentities.mano

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "mano_employees"
)
data class DBManoEmployeeEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "mano_id")
    val manoId: Long,

    @ColumnInfo(name = "shortname")
    val shortName: String,

    @Embedded
    val extendedData: DBManoEmployeeExtendedData
)

data class DBManoEmployeeExtendedData (
    @ColumnInfo("avatar_path")
    val avatarPath: String? = null,

    @ColumnInfo(name = "fullname")
    val fullName: String? = null,

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

data class DBManoEmployeeExtendedDataWithPk(
    @ColumnInfo(name = "mano_id")
    val manoId: Long,
    @Embedded
    val extendedData: DBManoEmployeeExtendedData
)